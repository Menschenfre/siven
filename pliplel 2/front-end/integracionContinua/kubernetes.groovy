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
  def construirImagen = load(env.PIPELINE_HOME+'front-end/stages/construirImagen.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'front-end/stages/pruebasUnitarias.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def ngBuild = load(env.PIPELINE_HOME+'front-end/stages/ngBuild.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
  def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!validaciones.validarArchivosTS() || !common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.NAMESPACE='bci-front'

  env.INT_AZURE_CLUSTER_NAME="bci-fro-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSFRO001"

  env.BASE_HREF = common.obtenerPathFE()

  def fueEjecutado = false
  bitbucketStatusNotify(buildState: 'INPROGRESS')

  def angular8 = angular8()

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

  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      env.DIR_EXTRA='/app'
      fueEjecutado=true
      try{
        node('Jtest'){
          dtp.frontend()
        }
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en dtp. Favor revisar.')
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      validaciones.valUrlInTsFiles()
      fueEjecutado=true
      try{
        common.modificarNpmrcNPMInstall()
        construirImagen.generarPreinstall(angular8)
        construirImagen.generarNpminstall()
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
        pruebasUnitarias.call()
        sh (script: 'sudo chown -R jenkins:jenkins $(pwd)/coverage/', returnStdout: false)
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
        sh (script: 'sudo chown -R jenkins:jenkins $(pwd)/dist/ || true ', returnStdout: false)
        ngBuild.compilar(common, angular8)
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

def angular8(){
  def archivoJson = env.WORKSPACE + "/package.json"
  if (fileExists(archivoJson)){
    def packageJson = readJSON file: archivoJson
    def versionAngular = packageJson.devDependencies."@angular/cli"
    //eliminar simbolo ^ en la versionAngular
    versionAngular = versionAngular.replace("^","").replace("~","").replace(".", "").toInteger()
    if (versionAngular >= 820){
      //Utiliza angular 8
      return true
    } else {
      return false
    }
  }
}

return this;
