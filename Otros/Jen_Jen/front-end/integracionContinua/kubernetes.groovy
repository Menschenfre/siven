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

  def common, construirImagen, pruebasUnitarias, login, ngBuild, crearImagenDocker, template, deployKubernetes, deployIngress, docker, nexus
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    construirImagen = fileLoader.load('front-end/stages/construirImagen');
    pruebasUnitarias = fileLoader.load('front-end/stages/pruebasUnitarias');
    login = fileLoader.load('helpers/login');
    ngBuild = fileLoader.load('front-end/stages/ngBuild');
    crearImagenDocker = fileLoader.load('helpers/crearImagenDocker');
    template = fileLoader.load('helpers/templates');
    deployKubernetes = fileLoader.load('helpers/deployKubernetes');
    deployIngress = fileLoader.load('helpers/deployIngress');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    dtp = fileLoader.load('helpers/dtp');
  }

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

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.SPACE='INT'
  env.INT_NAMESPACE_VALUE='reg_ic'
  env.INT_CLUSTER_NAME='bci-front-ic01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-front'
  env.INT_ENVIRONMENT_IMAGE='ic'

  env.BASE_HREF = common.modificacionPathFRONTEND()

  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      env.DIR_EXTRA='/app'
      try{
        node('Jtest'){
          dtp.frontend()
        }
      }
      catch(Exception ex) {
        println "Exception: " + ex.getMessage()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      common.modificarNpmrcNPMInstall()
      construirImagen.generarPreinstall()
      construirImagen.generarNpminstall()
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      pruebasUnitarias.call()
      sh (script: 'sudo chown -R jenkins:jenkins coverage/*', returnStdout: false)
    }
  }

  stage ('crearImagenDocker') {
    if (soloStageArray.contains('crearImagenDocker') || "${soloStage}" == '') {
      ngBuild.compilar()
      crearImagenDocker.call(common, docker, nexus)
      sh (script: 'sudo chown -R jenkins:jenkins dist/*', returnStdout: false)
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
