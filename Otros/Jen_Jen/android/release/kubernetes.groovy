def call() {
  def soloStage = "${soloStage}"

  env.ANDROID_HOME = '/opt/android-sdk-linux'
  env.VAULT_ADDR = 'http://127.0.0.1:8200'
  env.PROJECT_NAME="${params.projectName.toLowerCase()}"
  env.DEXGUARD_LICENSE = "${WORKSPACE}/dexguard/dexguard-license.txt"

  env.INT_VAULT_HOST = '172.31.32.115'
  env.CERT_VAULT_HOST = '169.44.4.80'

  def common, buildAppIntegration, checkingQA, pruebasUnitarias, pruebasUnitariasQA, pruebasCobertura, hockeyAppIntegration, hockeyAppQA, publishAPK
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    buildAppQA = fileLoader.load('android/stages/buildQA');
    buildApp = fileLoader.load('android/stages/buildApp');
    checkingQA = fileLoader.load('helpers/checkingQA');
    pruebasUnitarias = fileLoader.load('android/stages/pruebasUnitarias');
    pruebasUnitariasQA = fileLoader.load('android/stages/pruebasUnitariasQA');
    pruebasCobertura = fileLoader.load('android/stages/pruebasCobertura');
    hockeyAppQA = fileLoader.load('android/stages/hockeyAppQA');
    publishAPK = fileLoader.load('android/stages/publishAPK');
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  stage ('buildCodigoQA') {
    if ("${soloStage}" == 'buildCodigoQA') {
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN')]) {
        buildAppQA.call()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo') {
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN')]) {
        buildApp.call()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA') {
      env.DIR_EXTRA='/app'
      checkingQA.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      pruebasUnitarias.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura') {
      pruebasCobertura.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }


  stage ('pruebasUnitariasQA') {
    if ("${soloStage}" == 'pruebasUnitariasQA') {
      pruebasUnitariasQA.call()
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('hockeyAppQA') {
    if ("${soloStage}" == 'hockeyAppQA') {
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN')]) {
        hockeyAppQA.call()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('gitDiff') {
    if ("${soloStage}" == 'gitDiff') {
      lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('publishAPK') {
    if ("${soloStage}" == 'publishAPK') {
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN')]) {
        publishAPK.call()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }
}

return this;
