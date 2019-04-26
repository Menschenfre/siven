def call() {
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStage = "${soloStage}"

  env.ANDROID_HOME = '/opt/android-sdk-linux'
  env.VAULT_ADDR = 'http://127.0.0.1:8200'
  env.PROJECT_NAME = getProjectName()

  def common, buildAppIC, hockeyAppIntegration, checkingQA, pruebasUnitarias, pruebasCobertura
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    buildAppIC = fileLoader.load('android/stages/buildAppIC');
    hockeyAppIntegration = fileLoader.load('android/stages/hockeyAppIntegration');
    checkingQA = fileLoader.load('helpers/checkingQA');
    dtp = fileLoader.load('helpers/dtp');
    pruebasUnitarias = fileLoader.load('android/stages/pruebasUnitarias');
    pruebasCobertura = fileLoader.load('android/stages/pruebasCobertura');
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo' || "${soloStage}" == '') {
      buildAppIC.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA' || "${soloStage}" == '') {
      env.DIR_EXTRA='/app'
      checkingQA.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }
  
  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      env.DIR_EXTRA='/app'
      node('Jtest'){
        dtp.android()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias' || "${soloStage}" == '') {
      pruebasUnitarias.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura' || "${soloStage}" == '') {
      pruebasCobertura.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('hockeyAppIntegration') {
    if ("${soloStage}" == 'hockeyAppIntegration' || "${soloStage}" == '') {
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN')]) {
        hockeyAppIntegration.call()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
