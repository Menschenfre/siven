def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.OC_VALUE = "${params.oc}"
  env.VU = "${params.vu}"
  env.RAMP_UP = "${params.rampUp}"
  env.AVERAGE_TIME_LIMIT = "${params.averageTimeLimit}"
  env.PORC_ERROR = "${params.porcError}"
  def branchMerge = "${params.branchMerge}"
  def registroApiMngmnt = "${params.registroApiMngmnt}"
  env.VALIDAR_RAMA = "${params.validarRama}"

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
      try{
        validaciones.validarProyectoBci()
      } catch (Exception err){
        println err
        error ("Error en validarProyectoBci")
      }
    }
  }

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo') {
      def buildCodigo = load(env.PIPELINE_HOME+'microservicios/stages/buildCodigo.groovy');
      if(validaciones.bootstrapProp("${env.WORKSPACE}/src/main/resources/bootstrap.properties","MS")){
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
      def pruebasUnitarias = load(env.PIPELINE_HOME+'microservicios/stages/pruebasUnitarias.groovy');
      pruebasUnitarias.call()
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura') {
      def pruebasCobertura = load(env.PIPELINE_HOME+'microservicios/stages/pruebasCobertura.groovy');
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
      if(regApiConnect && !common.validarRegistroApiMngmnt(registroApiMngmnt)){
        try {
          apiMngt.registrar(apiConnectProduct, env.SPACE, webapp)
        } catch (Exception err) {
          println err
          common.notificarSlack("apigee")
          error ("Error en deployKubernetes")
        }
      }
    }
  }

  stage ('deployIngressMS') {
    if ("${soloStage}" == 'deployKubernetes' && env.REG_EUREKA) {
      def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
      common.deployIngress(deployIngress, azureIngress, common)
    }
  }

  stage ('registroApiGee') {
    if ("${soloStage}" == 'registroApiGee' && regApiConnect) {
      try {
        apiMngt.registrar(apiConnectProduct, env.SPACE, webapp)
      } catch (Exception err) {
        println err
        common.notificarSlack("apigee")
        error ("error en registroApiGee")
      }
    }
  }

  stage ('pruebasFuncionales') {
    if ("${soloStage}" == 'pruebasFuncionales') {
      def pruebasFuncionales = load(env.PIPELINE_HOME+'microservicios/stages/pruebasFuncionales.groovy');
      pruebasFuncionales.ejecucion(env.SPACE, common)
    }
  }

  stage ('pruebasRendimiento') {
    if ("${soloStage}" == 'pruebasRendimiento') {
        def pruebasRendimiento = load(env.PIPELINE_HOME+'microservicios/stages/pruebasRendimiento.groovy');
        env.THREADS=env.VU
        env.CONTEXT=pathbase
        env.VERSION=common.getApiVersion(env.VERSION_COMPONENTE)
        env.HOST='api-crt01.bci.cl'

        pruebasRendimiento.call()
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
      try {
        if(regApiConnect && !common.validarRegistroApiMngmnt(registroApiMngmnt)){
          login.validacionConexion(common, 'PROVIDENCIA')
          apiConnect.obtenerSpecJson(login, common)
          apiConnect.registrar(apiConnectProduct)
        }
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Error al registrar en api connect")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('despliegueIngressProduccion') {
    if ("${soloStage}" == 'despliegueSitesProduccion' && env.REG_EUREKA) {
      def ingress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      try {
      parallel (
        'Providencia': {
          login.validacionConexion(common, 'PROVIDENCIA')
          ingress.despliegueIngress(common, 'PROVIDENCIA')
        },
        'San-Bernardo': {
          login.validacionConexion(common, 'SAN-BERNARDO')
          ingress.despliegueIngress(common, 'SAN-BERNARDO')
        }
      )
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de Ingress en paralelo, por favor revisar de manera manual")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage ('registroApiConnectProd') {
    if ("${soloStage}" == 'registroApiConnectProd' && regApiConnect) {
      try {
        login.validacionConexion(common, 'PROVIDENCIA')
        apiConnect.obtenerSpecJson(login, common)
        apiConnect.registrar(apiConnectProduct)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Error al registrar en api connect")
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

  stage('ingressProd') {
    if ("${soloStage}" == 'deployProd' && env.REG_EUREKA) {
      try {
        def ingress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
        ingress.despliegueIngress(common)
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP_AZURE", false)
        //error ("Error al despliegar ingress en produccion")
      }
    }
  }

  stage ('apigeeProd') {
    if ("${soloStage}" == 'apigeeProd' && regApiConnect) {
      try {
        apiMngt.registrar(apiConnectProduct, "ACONCAGUA", webapp, common.obtenerNombreNodo("ACONCAGUA"))
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("apigee")
        currentBuild.result = 'SUCCESS'
      }
    }
  }

  stage ('rollbackKubernetes') {
    if ("${soloStage}" == 'rollbackKubernetes') {
      rollback.produccion(login, common, regApiConnect, env.PROJECT_NAME)
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