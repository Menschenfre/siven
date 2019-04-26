def call() {
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')
  env.PORT_NUMBER = '80'
  env.PROJECT_NAME = getProjectName()
  env.BLUEMIX_HOME = '.bluemix'
  env.OC_VALUE = ""

  def common, build, login, crearImagenDocker, template, deployKubernetes, deployIngress, docker, nexus
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    build = fileLoader.load('front-end-angularjs/stages/build');
    login = fileLoader.load('helpers/login');
    crearImagenDocker = fileLoader.load('helpers/crearImagenDocker');
    template = fileLoader.load('helpers/templates');
    deployKubernetes = fileLoader.load('helpers/deployKubernetes');
    deployIngress = fileLoader.load('helpers/deployIngress');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
  }

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  // Tipo de pipeline
  env.FE_ANGULARJS = true

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.SPACE='INT'

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.modificacionPathFRONTEND()

  env.INT_NAMESPACE_VALUE='reg_ic'
  env.INT_CLUSTER_NAME='bci-front-ic01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-front'
  env.INT_ENVIRONMENT_IMAGE='ic'

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      common.modificarNpmrcNPMInstall()
      build.call()
    }
  }

  stage ('crearImagenDocker') {
    if (soloStageArray.contains('crearImagenDocker') || "${soloStage}" == '') {
      dir('webPublico') {
        crearImagenDocker.call(common, docker, nexus)
      }
    }
  }

  stage ('deployKubernetes') {
    if (soloStageArray.contains('deployKubernetes') || "${soloStage}" == '') {
      template.generar()
      login.validacionConexion(common)
      deployKubernetes.call(common)
      common.respaldarYAML()
    }
  }

  stage ('deployIngressFront') {
    if (soloStageArray.contains('deployIngressFront') || "${soloStage}" == '') {
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }

}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
