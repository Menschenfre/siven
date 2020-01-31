def call() {
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')
  env.PROJECT_NAME = getProjectName()
  env.OC_VALUE = ""

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def buildCodigo = load(env.PIPELINE_HOME+'servicios-integracion/stages/buildCodigo.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasUnitarias.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasCobertura.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
  def pruebasFuncionales = load(env.PIPELINE_HOME+'servicios-integracion/stages/pruebasFuncionales.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');
  def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');

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
  env.NAMESPACE='bci-integ'

  env.INT_AZURE_CLUSTER_NAME="bci-api-desa001"
  env.INT_RESOURCE_GROUP="BCIRG3DSR-RG-AKSMSAPI001"

  def versionBuild = obtenerVersionBuildIG()
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

  def fueEjecutado = false
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
        withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
          buildCodigo.call()
        }
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
        checkingQA.ejecutar(sh (script: 'set +x;find . -type d -iname *Server | sed -e "s/\\.//g"', returnStdout: true).trim())
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
        env.TIPO_COMPONENTE='ig'
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
        def serverPath = sh (script: 'find . -type d -iname *Server', returnStdout: true).trim()
        dir ( serverPath ){
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
        bitbucketStatusNotify(buildState: 'SUCCESSFUL')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en deployKubernetes. Favor revisar.')
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

def obtenerVersionBuildIG(){
  println "Obteniendo version del ig a desplegar"
  def versionBuild = sh (script: '''set +x
  sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties
  proyectoApi=$(find . -type d -iname *Api | sed -e "s/\\.\\///g")
  versionBuild=$(/opt/gradle/gradle-4.1/bin/gradle ${proyectoApi}:properties | grep version)
  echo ${versionBuild}
  ''', returnStdout: true).trim()
  return versionBuild
}


return this;
