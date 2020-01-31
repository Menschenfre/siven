def call() {
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')

  env.PROJECT_NAME = getProjectName()
  env.SPACE = 'INT'
  env.VISIBILITY = 'True'
  env.NAMESPACE = 'bci-api'
  env.OC_VALUE = ""

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"

  def fueEjecutado = false

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def construirImagenPre = load(env.PIPELINE_HOME+'bff/stages/construirImagenPre.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'bff/stages/pruebasUnitarias.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
  def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def webapp = common.obtenerParametro("api.product.apps")
  if (apiMngt.validarDeveloperApp(webapp, regApiConnect) == "Webapp_error" || !common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "") || !validaciones.pathsDeclaradosSwagger()){
    currentBuild.result = 'UNSTABLE'
    return
  }
  nombreBranch = common.branchName()

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.obtenerPathBFF()

  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('veracode') {
    if (soloStageArray.contains('veracode') || "${soloStage}" == '') {
      fueEjecutado=true
      try {
        if(env.VERACODE != "null" ){
          veracode.call()
        }else {
          println "Se salta Inspección con Veracode."
        }
      } catch (Exception err) {
        println err
      }
    }
  }

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        construirImagenPre.generarPreinstall()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en prepararImagenesNpm. Favor revisar.')
      }
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        common.modificarNpmrcNPMInstall()
        pruebasUnitarias.invocar()
        sh (script: 'sudo chown -R jenkins:jenkins coverage/', returnStdout: false)
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en pruebasUnitarias. Favor revisar.')
      }
    }
  }

  stage ('crearImagenDocker') {
    if (soloStageArray.contains('crearImagenDocker') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        crearImagenDocker.call(common, docker, nexus)
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

  stage ('deployIngressBFF') {
    if (soloStageArray.contains('deployIngressBFF') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        common.deployIngress(deployIngress, azureIngress, common)
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en deployIngressBFF. Favor revisar.')
      }
    }
  }

  stage ('registrarApiGee') {
    if (soloStageArray.contains('registrarApiGee') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        validaciones.swaggerDesdePod(common)
        apiMngt.registrar(env.PROJECT_NAME, env.SPACE, webapp)
        bitbucketStatusNotify(buildState: 'SUCCESSFUL')
      }
      catch(Exception ex) {
        common.notificarSlack("apigee")
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en registrarApiGee. Favor revisar.')
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
