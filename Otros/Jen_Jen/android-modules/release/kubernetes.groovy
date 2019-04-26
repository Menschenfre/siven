def call(){
  //integracion continua
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
    upload = fileLoader.load('android-modules/stages/upload');
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  stage ('buildCodigo') {
    if (soloStageArray.contains('buildCodigo')) {
      buildCodigo.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias')) {
      pruebasUnitarias.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasCobertura') {
    if (soloStageArray.contains('pruebasCobertura')) {
      pruebasCobertura.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if (soloStageArray.contains('checkingQA')) {
      checkingQA.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('uploadRelease') {
    if (soloStageArray.contains('upload')) {
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
