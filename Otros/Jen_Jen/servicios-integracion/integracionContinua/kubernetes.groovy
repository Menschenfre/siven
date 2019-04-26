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

  def common, buildCodigo, checkingQA, dtp, pruebasUnitarias, pruebasCobertura, login, crearImagenDocker, template, deployKubernetes, pruebasFuncionales, docker, nexus, validaciones
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    buildCodigo = fileLoader.load('servicios-integracion/stages/buildCodigo');
    checkingQA = fileLoader.load('helpers/checkingQA');
    dtp = fileLoader.load('helpers/dtp');
    pruebasUnitarias = fileLoader.load('servicios-integracion/stages/pruebasUnitarias');
    pruebasCobertura = fileLoader.load('servicios-integracion/stages/pruebasCobertura');
    login = fileLoader.load('helpers/login');
    crearImagenDocker = fileLoader.load('helpers/crearImagenDocker');
    template = fileLoader.load('helpers/templates');
    deployKubernetes = fileLoader.load('helpers/deployKubernetes');
    pruebasFuncionales = fileLoader.load('servicios-integracion/stages/pruebasFuncionales');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    validaciones = fileLoader.load('helpers/validaciones');
  }

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }

//  def repoBranchName = env.BRANCH_NAME
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.SPACE='INT'
  env.INT_NAMESPACE_VALUE='reg_ic'
  env.INT_CLUSTER_NAME='bci-api-ic01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-integ'

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"

  def versionBuild = common.obtenerVersionBuildIG()
  println versionBuild
  if (!versionBuild.toLowerCase().contains("snapshot") && !env.BRANCH_NAME.equals("master") && !env.BRANCH_NAME.equals("develop")){
    error ("Corregir archivo build.gradle, version del aplicativo no contiene SNAPSHOT")
  } else if (env.BRANCH_NAME.equals("master") && versionBuild.toLowerCase().contains('snapshot')){
    error ("Corregir archivo build.gradle, version del aplicativo no debe contener SNAPSHOT en master")
  }

  if(!validaciones.bootstrapProp("${env.WORKSPACE}/${env.PROJECT_NAME}-server/src/main/resources/bootstrap.properties","IG")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo') || "${soloStage}" == '') {
      withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
        buildCodigo.call()
      }
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA') || "${soloStage}" == '') {
      env.DIR_EXTRA=sh (script: 'set +x;find . -type d -iname *Server | sed -e "s/\\.//g"', returnStdout: true).trim()
      checkingQA.call()
    }
  }

  stage ('dtp') {
    if (soloStageArray.contains('dtp') || "${soloStage}" == '') {
      env.TIPO_COMPONENTE='ig'
      node('Jtest'){
        dtp.call()
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
      def serverPath = sh (script: 'find . -type d -iname *Server', returnStdout: true).trim()

      dir ( serverPath ){
        crearImagenDocker.call(common, docker, nexus)
      }
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

  stage ('pruebasFuncionales') {
    if (soloStageArray.contains('pruebasFuncionales') || "${soloStage}" == '') {
      env.AMBIENTE_PIPE='integracion'
      env.ZUUL_SERVER_URI='bci-api-ic01.us-south.containers.mybluemix.net'
      env.MS_VERSION=env.VERSION_COMPONENTE
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
