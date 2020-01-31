def call(){
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')

  env.ANDROID_HOME = '/opt/android-sdk'
  env.PROJECT_NAME = getProjectName()

  def fueEjecutado = false

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def buildCodigo = load(env.PIPELINE_HOME+'android-modules/stages/buildCodigo.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'android-modules/stages/pruebasUnitarias.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'android-modules/stages/pruebasCobertura.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def upload = load(env.PIPELINE_HOME+'android-modules/stages/upload.groovy');

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  bitbucketStatusNotify(buildState: 'INPROGRESS')

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        timeout(time: 20, unit: 'MINUTES') {
          buildCodigo.call()
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

  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      fueEjecutado=true
      try{
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
          //veracode.android()
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
    } else {
      println "Condicion no cumplida, ignoramos stage"
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
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        checkingQA.ejecutar('/'+checkingQAPath().trim())
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

  stage ('uploadSnapshot') {
    if (soloStageArray.contains('upload') || "${soloStage}" == '') {
      fueEjecutado=true
      try{
        withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
          upload.call()
        }
        bitbucketStatusNotify(buildState: 'SUCCESSFUL')
      }
      catch(Exception ex) {
        bitbucketStatusNotify(buildState: 'FAILED')
        currentBuild.result = 'FAILURE'
        println "Exception: " + ex.getMessage()
        error('Se presentan problemas en uploadSnapshot. Favor revisar.')
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

@NonCPS
def checkingQAPath() {
  def path = sh (script: ''' set +x
    cat jenkins.json | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["checkingqa-path"];'
  ''', returnStdout: true).trim()
  return path
}

return this;
