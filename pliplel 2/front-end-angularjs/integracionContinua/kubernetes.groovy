def call() {
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')
  env.PROJECT_NAME = getProjectName()
  env.OC_VALUE = ""
  env.SPACE='INT'

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def build = load(env.PIPELINE_HOME+'front-end-angularjs/stages/build.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def ngBuild = load(env.PIPELINE_HOME+'front-end/stages/ngBuild.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
  def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!validaciones.validarArchivosTS() || !common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  // Tipo de pipeline
  env.FE_ANGULARJS = true

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.obtenerPathFE()

  env.NAMESPACE='bci-front'

  env.INT_AZURE_CLUSTER_NAME="bci-fro-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSFRO001"

  def fueEjecutado = false
  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        common.modificarNpmrcNPMInstall()
        build.call()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en prepararImagenesNpm. Favor revisar.')
      }
    }
  }

  stage ('crearImagenDocker') {
    if (soloStageArray.contains('crearImagenDocker') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        ngBuild.archivosImagenFrontEnd(common, "dist")
        dir('webPublico') {
          crearImagenDocker.call(common, docker, nexus)
        }
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en crearImagenDocker. Favor revisar.')
      }
    }
  }

  stage ('deployKubernetes') {
    if (soloStageArray.contains('deployKubernetes') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        common.listadoDespleigues(env.PROJECT_NAME, env.BRANCH_NAME, env.WORKSPACE, "IC")
        template.generar()
        template.generar(true)
        common.deployKubernetes(deployKubernetes, azureKubernetes, common)
        common.respaldarYAML()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en deployKubernetes. Favor revisar.')
      }
    }
  }

  stage ('deployIngressFront') {
    if (soloStageArray.contains('deployIngressFront') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        common.deployIngress(deployIngress, azureIngress, common)
        bitbucketStatusNotify(buildState: 'SUCCESSFUL')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en deployIngressFront. Favor revisar.')
      }
    }
  }

  if (!fueEjecutado) {
    currentBuild.result = 'ABORTED'
    error('Stage invalido')
  }
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
