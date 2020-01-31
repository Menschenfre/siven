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
  def buildCodigo = load(env.PIPELINE_HOME+'microservicios/stages/buildCodigo.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'microservicios/stages/pruebasUnitarias.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'microservicios/stages/pruebasCobertura.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def pruebasFuncionales = load(env.PIPELINE_HOME+'microservicios/stages/pruebasFuncionales.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');
  def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
  def pruebasRendimiento = load(env.PIPELINE_HOME+'microservicios/stages/pruebasRendimiento.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
  def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def apiConnectProduct = common.obtenerParametro("api.product.name")
  def pathbase = common.obtenerParametro("context.pathbase")
  def webapp = common.obtenerParametro("api.product.apps")
  if (apiMngt.validarDeveloperApp(webapp, regApiConnect) == "Webapp_error" || !common.validaValoresJSON(regIngress, regApiConnect, podSize, apiConnectProduct, pathbase)){
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

  if(!validaciones.bootstrapProp("${env.WORKSPACE}/src/main/resources/bootstrap.properties","MS")){
    currentBuild.result = 'UNSTABLE'
    return
  }

  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('validarProyectoBci') {
    if (soloStageArray.contains('validarProyectoBci') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        validaciones.validarProyectoBci()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en validarProyectoBci. Favor revisar.')
      }
    }
  }

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

  if ((soloStageArray.contains('deployIngressMS') || "${soloStage}" == '') && env.REG_EUREKA) {
    stage ('deployIngressMS') {
      fueEjecutado=true
      try{
        common.deployIngress(deployIngress, azureIngress, common)
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en deployIngressMS. Favor revisar.')
      }
    }
  }

  if ((soloStageArray.contains('registrarApiGee') || "${soloStage}" == '') && regApiConnect) {
    stage ('registrarApiGee') {
      fueEjecutado=true
      try{
        validaciones.swaggerDesdePod(common)
        apiMngt.registrar(apiConnectProduct, env.SPACE, webapp)
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

  stage ('pruebasFuncionales') {
    if (soloStageArray.contains('pruebasFuncionales') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        pruebasFuncionales.ejecucion(env.SPACE, common)
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en pruebasFuncionales. Favor revisar.')
      }
    }
  }
  stage ('pruebasRendimiento')  {
    if (soloStageArray.contains('pruebasRendimiento') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        env.AVERAGE_TIME_LIMIT=12
        env.PORC_ERROR=15
        env.THREADS=10
        env.OC_VALUE="IC"
        env.CONTEXT=pathbase
        env.VERSION=env.VERSION_COMPONENTE
        env.HOST='api-dsr01.bci.cl'

        pruebasRendimiento.call()
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en pruebasRendimiento. Favor revisar.')
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
