def call(){
  //integracion continua
  properties(
    [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
  )
  def soloStageArray = "${soloStage}".split(';')

  env.ANDROID_HOME = '/opt/android-sdk'
  env.PROJECT_NAME = getProjectName()
  env.DIR_EXTRA = '/'+checkingQAPath().trim()

  def common, buildCodigo, pruebasUnitarias, pruebasCobertura, checkingQA, upload
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    buildCodigo = fileLoader.load('android-modules/stages/buildCodigo');
    pruebasUnitarias = fileLoader.load('android-modules/stages/pruebasUnitarias');
    pruebasCobertura = fileLoader.load('android-modules/stages/pruebasCobertura');
    checkingQA = fileLoader.load('helpers/checkingQA');
    dtp = fileLoader.load('helpers/dtp');
    upload = fileLoader.load('android-modules/stages/upload');
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo') || "${soloStage}" == '') {
      buildCodigo.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }
  
  stage ('dtp') {
    if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
      node('Jtest'){
        dtp.android()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      pruebasUnitarias.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasCobertura') {
    if (soloStageArray.contains('pruebasCobertura') || "${soloStage}" == '') {
      pruebasCobertura.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA') || "${soloStage}" == '') {
      checkingQA.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('uploadSnapshot') {
    if (soloStageArray.contains('upload') || "${soloStage}" == '') {
      withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
        upload.call()
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

@NonCPS
def checkingQAPath() {
  def path = sh (script: ''' set +x
    cat jenkins.json | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["checkingqa-path"];'
  ''', returnStdout: true).trim()
  return path
}

return this;
