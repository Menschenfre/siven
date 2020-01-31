def call() {
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStage = "${soloStage}"

  env.ANDROID_HOME = '/opt/android-sdk-linux'
  env.VAULT_ADDR = 'http://127.0.0.1:8200'
  env.PROJECT_NAME = getProjectName()

  def fueEjecutado = false

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def buildAppIC = load(env.PIPELINE_HOME+'android/stages/buildAppIC.groovy');
  def hockeyAppIntegration = load(env.PIPELINE_HOME+'android/stages/hockeyAppIntegration.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'android/stages/pruebasUnitarias.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'android/stages/pruebasCobertura.groovy');

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo' || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        timeout(time: 20, unit: 'MINUTES') { 
          buildAppIC.call()
        }
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en buildCodigo. Favor revisar.')
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA' || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        checkingQA.ejecutar('/app')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en checkingQA. Favor revisar.')
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        env.DIR_EXTRA='/app'
        node('Jtest'){
          dtp.android()
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

  stage ('veracode') {
    if ("${soloStage}" == 'veracode' || "${soloStage}" == '') {
      fueEjecutado=true
      try {
        if(env.VERACODE != "null" ){
         // veracode.call()
          println "Pendiente por requerimiento tecnico"
        }else {
          println "Se salta Inspección con Veracode."
        }
      } catch (Exception err) {
        println err
      }
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias' || "${soloStage}" == '') {
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
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura' || "${soloStage}" == '') {
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
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('hockeyAppIntegration') {
    if ("${soloStage}" == 'hockeyAppIntegration' || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN'),
          string(credentialsId: 'ANDROID_API_TOKEN', variable: 'ANDROID_API_TOKEN'),
          string(credentialsId: 'ANDROID_APP_ID_DEV', variable: 'ANDROID_APP_ID_DEV')]) {
          hockeyAppIntegration.call()
        }
        bitbucketStatusNotify(buildState: 'SUCCESSFUL')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en hockeyAppIntegration. Favor revisar.')
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
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
