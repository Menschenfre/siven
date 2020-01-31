def call() {
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')
  env.PROJECT_NAME = getProjectName()
  env.OC_VALUE = ""
  env.SPACE = 'INT'

  def fueEjecutado = false

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def buildCodigo = load(env.PIPELINE_HOME+'cronJob/stages/buildCodigo.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'cronJob/stages/pruebasUnitarias.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'cronJob/stages/pruebasCobertura.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
  def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def pathbase = common.obtenerParametro("context.pathbase")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch
  env.BASE_HREF=pathbase+"/"+nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker("", nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.NAMESPACE='bci-api'

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"

  //valores para cronJob sacados desde container_params.json
  def schedule = common.obtenerParametro("cronJob.schedule") //valor ejecucion periodica
  def jobHistoryLimit = common.obtenerParametro("cronJob.jobsHistoryLimit") //historial de ejecuciones
  env.VAR_SCHEDULE = schedule
  env.VAR_JOBSHISTORYLIMIT = jobHistoryLimit

  if(schedule != null && jobHistoryLimit != null && !schedule.equals("") && !jobHistoryLimit.equals("") ){
    println "variables para cronJob encontradas en container_params.json"
    println "schedule: "+schedule
    println "jobHistoryLimit: "+jobHistoryLimit
  }else {
    error ("no existen variables para cronJob en container_params.json, favor de verificar")
    println "schedule: "+schedule
    println "jobHistoryLimit: "+jobHistoryLimit
  }

  if(!validaciones.bootstrapProp("${env.WORKSPACE}/src/main/resources/bootstrap.properties","CRONJOB")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        buildCodigo.call()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en buildCodigo. Favor revisar.')
      }
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        checkingQA.ejecutar()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en checkingQA. Favor revisar.')
      }
    }
  }
  
  stage ('dtp') {
    if (soloStageArray.contains('dtp') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        env.TIPO_COMPONENTE='ms'
        node('Jtest'){
          dtp.call()
        }
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en dtp. Favor revisar.')
      }
    }
  }
  
  stage ('veracode') {
    if (soloStageArray.contains('veracode') || "${soloStage}" == '') {
      fueEjecutado=true
      try {
          //veracode.call()
        } catch (Exception err) {
          println err
        }
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        pruebasUnitarias.call()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en pruebasUnitarias. Favor revisar.')
      }
    }
  }

  stage ('pruebasCobertura') {
    if (soloStageArray.contains('pruebasCobertura') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        pruebasCobertura.call()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en pruebasCobertura. Favor revisar.')
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
  
  stage ('validarConfigFileIC') {
    if (soloStageArray.contains('validarConfigFileIC') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        def configServerPath = "/opt/kubernetes/configuration-files/configuration-files-desarrollo-az/"
        def configFileName = common.getValueFromBootstrapProps('spring.application.name','integracion')
        common.checkIfConfigFileExists(configServerPath, configFileName, 'IC')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en validarConfigFileIC. Favor revisar.')
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
