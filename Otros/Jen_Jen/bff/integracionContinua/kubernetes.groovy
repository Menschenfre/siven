def call() {
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')

  env.PORT_NUMBER = '3000'
  env.PROJECT_NAME = getProjectName()
  env.BLUEMIX_HOME = '.bluemix'
  env.SPACE = 'INT'
  env.VISIBILITY = 'True'
  env.INT_NAMESPACE_VALUE = 'reg_ic'
  env.INT_CLUSTER_NAME = 'bci-api-ic01'
  env.INT_ORGANIZATION = 'Bci API'
  env.INT_TARGET_WORKSPACE = 'integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE = 'bci-api'
  env.OC_VALUE = ""

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"
  
  def common, construirImagenPre, pruebasUnitarias, login, crearImagenDocker, template, deployKubernetes, deployIngress, docker, nexus, apiConnect
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    construirImagenPre = fileLoader.load('bff/stages/construirImagenPre');
    pruebasUnitarias = fileLoader.load('bff/stages/pruebasUnitarias');
    login = fileLoader.load('helpers/login');
    crearImagenDocker = fileLoader.load('helpers/crearImagenDocker');
    template = fileLoader.load('helpers/templates');
    deployKubernetes = fileLoader.load('helpers/deployKubernetes');
    deployIngress = fileLoader.load('helpers/deployIngress');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    apiConnect = fileLoader.load('helpers/api-connect')
  }

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }  
  nombreBranch = common.branchName()
  
  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)
  
  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.modificacionPathBFF()

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      construirImagenPre.call()
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      common.modificarNpmrcNPMInstall()
      pruebasUnitarias.call()
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

  stage ('deployIngressBFF') {
    if (soloStageArray.contains('deployIngressBFF') || "${soloStage}" == '') {
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }

  stage ('registrarApiConnect') {
    if (soloStageArray.contains('registrarApiConnect') || "${soloStage}" == '') {
      login.validacionConexion(common)
      apiConnect.obtenerSpecJson(login, common)
      apiConnect.registrar(env.PROJECT_NAME)
    }
  }

}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
