def call() {
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')
  env.PORT_NUMBER = '8080'
  env.PROJECT_NAME = getProjectName()
  env.BLUEMIX_HOME = '.bluemix'
  env.OC_VALUE = ""

  def common, buildCodigo, checkingQA, pruebasUnitarias, pruebasCobertura, login, crearImagenDocker, template, deployKubernetes, pruebasFuncionales, docker, nexus, apiConnect, deployIngress, validaciones
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    buildCodigo = fileLoader.load('mobile/stages/buildCodigo');
    checkingQA = fileLoader.load('helpers/checkingQA');
    pruebasUnitarias = fileLoader.load('mobile/stages/pruebasUnitarias');
    pruebasCobertura = fileLoader.load('mobile/stages/pruebasCobertura');
    login = fileLoader.load('helpers/login');
    crearImagenDocker = fileLoader.load('helpers/crearImagenDocker');
    template = fileLoader.load('helpers/templates');
    deployKubernetes = fileLoader.load('helpers/deployKubernetes');
    pruebasFuncionales = fileLoader.load('mobile/stages/pruebasFuncionales');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    apiConnect = fileLoader.load('helpers/api-connect');
    deployIngress = fileLoader.load('helpers/deployIngress');
    validaciones = fileLoader.load('helpers/validaciones');
  }

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def apiConnectProduct = common.obtenerParametro("api.product.name")
  def pathbase = common.obtenerParametro("context.pathbase")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, apiConnectProduct, pathbase)){
    currentBuild.result = 'UNSTABLE'
    return
  }
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch
  env.BASE_HREF=pathbase+"/"+nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.SPACE='INT'
  env.INT_NAMESPACE_VALUE='reg_ic'
  env.INT_CLUSTER_NAME='bci-api-ic01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-api'

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"

  if(!validaciones.bootstrapProp("${env.WORKSPACE}/app/src/main/resources/bootstrap.properties","API")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo') || "${soloStage}" == '') {
      buildCodigo.call()
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA') || "${soloStage}" == '') {
      env.DIR_EXTRA='/app'
      if (env.PROJECT_NAME == 'bff-mobile'){
        checkingQA.bffMobile()
      } else {
      checkingQA.call()
      }
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      pruebasUnitarias.call()
    }
  }

  stage ('pruebasCobertura') {
    if (soloStageArray.contains('pruebasCobertura') || "${soloStage}" == '') {
      pruebasCobertura.call()
    }
  }

  stage ('crearImagenDocker') {
    if (soloStageArray.contains('crearImagenDocker') || "${soloStage}" == '') {
      crearImagenDocker.call(common, docker, nexus)
    }
  }

  stage ('deployKubernetes') {
    if (soloStageArray.contains('deployKubernetes') || "${soloStage}" == '') {
      template.generar()
      template.generar(true)
      login.validacionConexion(common)
      deployKubernetes.call(common)
      common.respaldarYAML()
      common.respaldarYAML(true)
    }
  }

  if ((soloStageArray.contains('deployIngressMS') || "${soloStage}" == '') && env.REG_EUREKA) {
    stage ('deployIngressMS') {
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }

  if ((soloStageArray.contains('registrarApiConnect') || "${soloStage}" == '') && regApiConnect) {
    stage ('registrarApiConnect') {
      login.validacionConexion(common)
      apiConnect.obtenerSpecJson(login, common)
      apiConnect.registrar(apiConnectProduct)
    }
  }

  stage ('pruebasFuncionales') {
    if (soloStageArray.contains('pruebasFuncionales') || "${soloStage}" == '') {
      env.AMBIENTE_PIPE='integracion'
      env.ZUUL_SERVER_URI='bci-api-ic01.us-south.containers.mybluemix.net'
      env.MS_VERSION=env.VERSION_COMPONENTE
      env.HOST_MICROSERVICIO='https://bci-api-ic01.us-south.containers.mybluemix.net'
      env.PORT='443'
      env.CONTEXT_PATH=env.BASE_HREF
      env.AMBIENTE='ic'
      pruebasFuncionales.call()
    }
  }

}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
