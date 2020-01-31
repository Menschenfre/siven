def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.OC_VALUE = "${params.oc}"
  def branchMerge = "${params.branchMerge}"
  env.VALIDAR_RAMA = "${params.validarRama}"

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.NAMESPACE='bci-integ'

  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"

  env.PROD_LOCAL_AZURE_CLUSTER_NAME="bci-api-prod001"

  def versionBuild = obtenerVersionBuildIG()
  println versionBuild

  if(env.VALIDAR_RAMA != "null" && env.VALIDAR_RAMA != null && env.VALIDAR_RAMA == true){
    println "Se valida rama release respecto al master"
    if(!validaciones.ramaActualizada()){
      error("LA RAMA SE ENCUENTRA DESACTUALIZADA RESPECTO AL MASTER")
      currentBuild.result = 'FAILURE'
    }
  }else{
    println "No se valida rama release respecto al master"
  }

  stage ('validarProyectoBci') {
    if ("${soloStage}" == 'validarProyectoBci') {
      try {
        validaciones.validarProyectoBci()
      } catch (Exception err) {
        println err
      }
    }
  }

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo') {
      withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
        def componenteCorregido = componente.replace("re-v","").replace("-",".")
        if (!versionBuild.contains(componenteCorregido) || versionBuild.toLowerCase().contains("snapshot")){
          println "El numero de la version ingresada en XL Release = "+componente+", no coincide con "+versionBuild+" establecida en el archivo build.gradle"
          currentBuild.result = 'FAILURE'
          error("FAILURE")
        }
        println "Generando "+versionBuild

        def buildCodigo = load(env.PIPELINE_HOME+'servicios-integracion/stages/buildCodigo.groovy');

        if(validaciones.bootstrapProp("${env.WORKSPACE}/${env.PROJECT_NAME}-server/src/main/resources/bootstrap.properties","IG")){
          buildCodigo.call()
        }else{
          currentBuild.result = 'UNSTABLE'
          return
        }
      }
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA') {
      def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
      checkingQA.ejecutar(sh (script: 'set +x;find . -type d -iname *Server | sed -e "s/\\.//g"', returnStdout: true).trim())
    }
  }

  stage ('dtp') {
    if ("${soloStage}" == 'dtp') {
      def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
      env.TIPO_COMPONENTE='ig'
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
      def pruebasUnitarias = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasUnitarias.groovy');
      pruebasUnitarias.call()
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura') {
      def pruebasCobertura = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasCobertura.groovy');
      pruebasCobertura.call()
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
      def serverPath = sh (script: 'find . -type d -iname *Server', returnStdout: true).trim()

      dir ( serverPath ){
        crearImagenDocker.call(common, docker, nexus)
      }
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
      common.listadoDespleigues(env.PROJECT_NAME, env.BRANCH_NAME, env.WORKSPACE, "QA")
      def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      template.generar()
      template.generar(true)
      common.deployKubernetes(deployKubernetes, azureKubernetes, common)
      common.respaldarYAML()
    }
  }

  stage ('pruebasFuncionales') {
    if ("${soloStage}" == 'pruebasFuncionales') {
      def pruebasFuncionales = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasFuncionales.groovy');
      pruebasFuncionales.ejecucion(env.SPACE, common)
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
      def revisionProvidencia, revisionSanBernardo
      template.generar()
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            revisionProvidencia = common.getRevision('PROVIDENCIA')
            deployment.despliegueKubernetes(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            revisionSanBernardo = common.getRevision('SAN-BERNARDO')
            deployment.despliegueKubernetes(common, 'SAN-BERNARDO')
          }
        )
        common.respaldarYAML()
      } catch (err) {
        println "Error: "+err
        rollback.site("PROVIDENCIA", revisionProvidencia, common)
        rollback.site("SAN-BERNARDO", revisionSanBernardo, common)
        common.notificarSlack("PAP", false)
        error ("Rollback realizado")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('deployProd') {
    if ("${soloStage}" == 'deployProd') {
      def deploy = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      def revAconcagua, revLongovilo
      template.generar(true)
      stash includes: "az-"+env.DEPLOY_NAME+".yml", name: 'deployAzure'
      try {
        parallel (
          'Aconcagua': {
            node (common.obtenerNombreNodo("ACONCAGUA")){
              revAconcagua = common.getRevisionAzureProd()
              unstash 'deployAzure'
              deploy.despliegueKubernetes()
            }
          },
          'Longovilo': {
            node (common.obtenerNombreNodo("LONGOVILO")){
              revLongovilo = common.getRevisionAzureProd()
              unstash 'deployAzure'
              deploy.despliegueKubernetes()
            }
          }
        )
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        rollback.azureSite(common, "ACONCAGUA", revAconcagua)
        rollback.azureSite(common, "LONGOVILO", revLongovilo)
        common.notificarSlack("PAP_AZURE", false)
        //error ("Rollback realizado")
      }
    }
  }

  stage ('rollbackKubernetes') {
    if ("${soloStage}" == 'rollbackKubernetes') {
      rollback.produccion(login, common)
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

def obtenerVersionBuildIG(){
  println "Obteniendo version del ig a desplegar"
  def versionBuild = sh (script: '''set +x
  sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties
  proyectoApi=$(find . -type d -iname *Api | sed -e "s/\\.\\///g")
  versionBuild=$(/opt/gradle/gradle-4.1/bin/gradle ${proyectoApi}:properties | grep version)
  echo ${versionBuild}
  ''', returnStdout: true).trim()
  return versionBuild
}

return this;
