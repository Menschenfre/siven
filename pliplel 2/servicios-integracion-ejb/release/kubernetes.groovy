def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.OC_VALUE = "${params.oc}"
  def branchMerge = "${params.branchMerge}"
  def registroApiMngmnt = "${params.registroApiMngmnt}"

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');

  if(!common.labelsValidos()){
    echo 'Error: Por favor verificar valores asignados a los Labels.'
    currentBuild.result = 'UNSTABLE'
    return
  }
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

  env.PROD_LOCAL_CLUSTER_NAME='bci-api-prod01-pl'

  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"
  
  env.PROD_LOCAL_AZURE_CLUSTER_NAME="bci-api-prod001"

  switch(soloStage) {
    case 'buildCodigo':
      stage ('buildCodigo') {
        def buildCodigo = load(env.PIPELINE_HOME+'servicios-integracion-ejb/stages/buildCodigo.groovy');
        buildCodigo.call()
      }
      break
    case 'checkingQA':
      stage ('checkingQA') {
        def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
        checkingQA.ejecutar(sh (script: 'find . -type d -iname *Neg | sed -e "s/\\.//g"', returnStdout: true).trim())
      }
      break
    case 'crearImagenDocker':
      stage ('crearImagenDocker') {
        def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
        crearImagenDocker.call(common, docker, nexus)
      }
      break
    case 'validarConfigFileQA':
      stage ('validarConfigFileQA') {
        def configServerPath = "/opt/kubernetes/azure/configuration-files-qa-az/"
        def configFileName = common.getValueFromBootstrapProps('spring.application.name','qa')
        common.checkIfConfigFileExists(configServerPath, configFileName, 'QA')
      }
      break
    case 'deployKubernetes':
      stage ('deployKubernetes') {
        common.listadoDespleigues(env.PROJECT_NAME, env.BRANCH_NAME, env.WORKSPACE, "QA")
        def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
        def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
        template.generar()
        template.generar(true)
        common.deployKubernetes(deployKubernetes, azureKubernetes, common)
        common.respaldarYAML()
        if(regApiConnect && registroApiMngmnt == ""){
          try {
            apiMngt.registrar(apiConnectProduct, env.SPACE, webapp)
          } catch (Exception err) {
            println err
            common.notificarSlack("apigee")
            error ("Error en deployKubernetes")
          }
        }
        if (env.REG_EUREKA) {
          def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
          def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
          common.deployIngress(deployIngress, azureIngress, common)
        }
      }
      break
    case 'registroApiGee':
      if(regApiConnect){
          try {
            apiMngt.registrar(apiConnectProduct, env.SPACE, webapp)
          } catch (Exception err) {
            println err
            common.notificarSlack("apigee")
            error ("error en registroApiGee")
          }
        }
      break
    case 'gitDiff':
      stage ('gitDiff') {
        lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
      }
      break
    case 'validarConfigFiles':
      stage ('validarConfigFiles'){
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
      break
    case 'imagenProdNexus':
      stage ('imagenProdNexus') {
        common.imagenProdANexus(despliegue, nexus, docker)
      }
      break
    case 'despliegueSitesProduccion':
      stage('despliegueSitesProduccion') {
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
          if(regApiConnect){
            login.validacionConexion(common, 'PROVIDENCIA')
            apiConnect.obtenerSpecJson(login, common)
            apiConnect.registrar(apiConnectProduct)
          }
        } catch (err) {
          println "Error: "+err
          common.notificarSlack("PAP", false)
          error ("Error al registrar en api connect")
        }
        if (env.REG_EUREKA) {
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
      break
    case 'deployProd':
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
      } catch (err) {
        println "Error: "+err
        rollback.azureSite(common, "ACONCAGUA", revAconcagua)
        rollback.azureSite(common, "LONGOVILO", revLongovilo)
        common.notificarSlack("PAP_AZURE", false)
        //error ("Rollback realizado")
      }
      if (env.REG_EUREKA) {
        try {
          def ingress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
          ingress.despliegueIngress(common)
        } catch (err) {
          println "Error: "+err
          common.notificarSlack("PAP_AZURE", false)
          //error ("Error al despliegar ingress en produccion")
        }
      }
      common.notificarSlack("PAP_AZURE", true)
      break
    case 'apigeeProd':
      if (regApiConnect) {
        try {
          apiMngt.registrar(apiConnectProduct, "ACONCAGUA", webapp, common.obtenerNombreNodo("ACONCAGUA"))
        } catch (err) {
          println "Error: "+err
          common.notificarSlack("apigee")
          currentBuild.result = 'SUCCESS'
        }
      }
      break
    case 'rollbackKubernetes':
      stage ('rollbackKubernetes') {
        rollback.produccion(login, common, regApiConnect, env.PROJECT_NAME)
      }
      break
    case 'MergeDevelop':
      stage ('MergeDevelop') {
        common.mergeRama(branchMerge, nombreBranch)
      }
      break
    case 'MergeMaster':
      stage ('MergeMaster') {
        common.mergeRama(branchMerge, nombreBranch)
      }
      break
    default :
      error ("Stage "+soloStage+" no se encuentra en el pipeline definido para XL Release o en Jenkins, favor revisar que los pipelines coincidan con el tipo de aplicativo a desplegar")
      break
  }
}

return this;
