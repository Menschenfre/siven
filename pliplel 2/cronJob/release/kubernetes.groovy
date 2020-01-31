def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.PORT_NUMBER = '8080'
  env.BLUEMIX_HOME = '.bluemix'
  env.OC_VALUE = "${params.oc}"
  def branchMerge = "${params.branchMerge}"
  def registroApiMngmnt = "${params.registroApiMngmnt}"

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def apiConnect = load(env.PIPELINE_HOME+'helpers/api-connect.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');
  def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  //valores para cronJob sacados desde container_params.json
  def schedule = common.obtenerParametro("cronJob.schedule") //valor ejecucion periodica
  def jobHistoryLimit = common.obtenerParametro("cronJob.jobsHistoryLimit") //historial de ejecuciones
  env.VAR_SCHEDULE = schedule
  env.VAR_JOBSHISTORYLIMIT = jobHistoryLimit

  if(schedule != null && jobHistoryLimit != null && !schedule.equals("") && !jobHistoryLimit.equals("") ){
    println "variables para cronJob encontradas en container_params.json"
    println "schedule: "+schedule
    println "jobHistoryLimit: "+jobHistoryLimit
  }else {
    error ("no existen variables para cronJob en container_params.json, favor de verificar")
    println "schedule: "+schedule
    println "jobHistoryLimit: "+jobHistoryLimit
  }

  env.DEPLOY_NAME = common.deployName()
  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def apiConnectProduct = common.obtenerParametro("api.product.name")
  def pathbase = common.obtenerParametro("context.pathbase")
  def webapp = common.obtenerParametro("api.product.apps")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, apiConnectProduct, pathbase)){
    currentBuild.result = 'UNSTABLE'
    return
  }
  env.BASE_HREF=pathbase+"/"+common.getApiVersion(env.VERSION_COMPONENTE)

  env.NAMESPACE='bci-api'

  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"

  env.PROD_LOCAL_CLUSTER_NAME='bci-api-prod01-pl'

  env.PROD_LOCAL_AZURE_CLUSTER_NAME="bci-api-prod001"

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo') {
      def buildCodigo = load(env.PIPELINE_HOME+'cronJob/stages/buildCodigo.groovy');
      if(validaciones.bootstrapProp("${env.WORKSPACE}/src/main/resources/bootstrap.properties","CRONJOB")){
        buildCodigo.call()
      }else{
        currentBuild.result = 'UNSTABLE'
        return
      }
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA') {
      def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
      checkingQA.ejecutar()
    }
  }

  stage ('dtp') {
    if ("${soloStage}" == 'dtp') {
      def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
      env.TIPO_COMPONENTE='ms'
      node('Jtest'){
        dtp.call()
      }
    }
  }
  
  stage ('veracode') {
    if ("${soloStage}" == 'veracode') {
      def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
        try {
          //veracode.call()
        } catch (Exception err) {
          println err
        }
    }
  }
  
  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      def pruebasUnitarias = load(env.PIPELINE_HOME+'cronJob/stages/pruebasUnitarias.groovy');
      pruebasUnitarias.call()
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura') {
      def pruebasCobertura = load(env.PIPELINE_HOME+'cronJob/stages/pruebasCobertura.groovy');
      pruebasCobertura.call()
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
      crearImagenDocker.call(common, docker, nexus)
    }
  }

  stage ('validarConfigFileQA') {
    if ("${soloStage}" == 'validarConfigFileQA') {
      def configServerPath = "/opt/kubernetes/azure/configuration-files-qa-az/"
      def configFileName = common.getValueFromBootstrapProps('spring.application.name','qa')
      common.checkIfConfigFileExists(configServerPath, configFileName, 'QA')
    }
  }

  stage ('deployKubernetes') {
    if ("${soloStage}" == 'deployKubernetes') {
      validaciones.variableRegistroApiMngmnt(registroApiMngmnt, regApiConnect)
      common.listadoDespleigues(env.PROJECT_NAME, env.BRANCH_NAME, env.WORKSPACE, "QA")
      def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      template.generar()
      template.generar(true)
      common.deployKubernetes(deployKubernetes, azureKubernetes, common)
      common.respaldarYAML()
    }
  }

  stage ('gitDiff') {
    if ("${soloStage}" == 'gitDiff') {
      lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
    }
  }

  stage ('validarConfigFiles'){
    if ("${soloStage}" == 'validarConfigFiles') {
      configFileName = common.getValueFromBootstrapProps('spring.application.name','produccion')
      //Prod
      println 'Verificando config file produccion en config server.'
      println '============================================================'
      configServerPath = '/opt/kubernetes/configuration-files/produccion/'
      common.checkIfConfigFileExists(configServerPath, configFileName, 'PROD')
      //Diff QA y Prod
      println 'Comparando config files de QA y produccion.'
      println '==========================================='
      configFileQa = '/opt/kubernetes/azure/configuration-files-qa-az/' + configFileName.replace("produccion","qa")
      configFileProd = configServerPath + configFileName
      common.compareYamlFiles(configFileQa, configFileProd)
    }
  }

  stage ('imagenProdNexus') {
    if ("${soloStage}" == 'imagenProdNexus') {
      common.imagenProdANexus(despliegue, nexus, docker)
    }
  }

  stage('despliegueSitesProduccion') {
    if ("${soloStage}" == 'despliegueSitesProduccion') {
      def deployment = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      template.generar()
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            deployment.despliegueKubernetesCronJob(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            deployment.despliegueKubernetesCronJob(common, 'SAN-BERNARDO')
          }
        )
        common.respaldarYAML()
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("error al realizar despliegue de cronJob")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('deployProd') {
    if ("${soloStage}" == 'deployProd') {
      def deploy = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      template.generar(true)
      stash includes: "az-"+env.DEPLOY_NAME+".yml", name: 'deployAzure'
      try {
        parallel (
          'Aconcagua': {
            node (common.obtenerNombreNodo("ACONCAGUA")){
              unstash 'deployAzure'
              deploy.despliegueKubernetesCronJob()
            }
          },
          'Longovilo': {
            node (common.obtenerNombreNodo("LONGOVILO")){
              unstash 'deployAzure'
              deploy.despliegueKubernetesCronJob()
            }
          }
        )
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP_AZURE", false)
        error ("Error al realizar despliegue de cronJob")
      }
    }
  }

  stage ('EliminarCronJobAZURE'){
    if ("${soloStage}" == 'EliminarCronJobAZURE') {
      def deploy = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
        try {
        parallel (
          'Aconcagua': {
            node (common.obtenerNombreNodo("ACONCAGUA")){
              deploy.eliminarCronJob()
            }
          },
          'Longovilo': {
            node (common.obtenerNombreNodo("LONGOVILO")){
              deploy.eliminarCronJob()
            }
          }
        )
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP_AZURE", false)
        error ("Error al eliminar despliegue de cronJob")
      }  
    }
  }

    stage('EliminarCronJobBLUEMIX') {
    if ("${soloStage}" == 'EliminarCronJobBLUEMIX') {
      def deployment = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            deployment.eliminarCronJob(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            deployment.eliminarCronJob(common, 'SAN-BERNARDO')
          }
        )
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("error al eliminar despliegue de cronJob")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage ('MergeDevelop') {
    if ("${soloStage}" == 'MergeDevelop') {
        common.mergeRama(branchMerge, nombreBranch)
    }
  }

  stage ('MergeMaster') {
    if ("${soloStage}" == 'MergeMaster') {
        common.mergeRama(branchMerge, nombreBranch)
    }
  }

}
return this;
