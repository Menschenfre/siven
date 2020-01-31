def call() {
  def soloStage = "${soloStage}"

  env.ANDROID_HOME = '/opt/android-sdk-linux'
  env.VAULT_ADDR = 'http://127.0.0.1:8200'
  env.PROJECT_NAME="${params.projectName.toLowerCase()}"
  env.DEXGUARD_LICENSE = "${WORKSPACE}/dexguard/dexguard-license.txt"

  env.INT_VAULT_HOST = '172.31.32.115'
  env.CERT_VAULT_HOST = '169.44.4.80'

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def buildAppQA = load(env.PIPELINE_HOME+'android/stages/buildQA.groovy');
  def buildApp = load(env.PIPELINE_HOME+'android/stages/buildApp.groovy');
  def checkingQA = load(env.PIPELINE_HOME+'helpers/checkingQA.groovy');
  def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'android/stages/pruebasUnitarias.groovy');
  def pruebasUnitariasQA = load(env.PIPELINE_HOME+'android/stages/pruebasUnitariasQA.groovy');
  def pruebasCobertura = load(env.PIPELINE_HOME+'android/stages/pruebasCobertura.groovy');
  def hockeyAppQA = load(env.PIPELINE_HOME+'android/stages/hockeyAppQA.groovy');
  def publishAPK = load(env.PIPELINE_HOME+'android/stages/publishAPK.groovy');

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
      checkingQA.ejecutar('/app')
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('dtp') {
    if ("${soloStage}" == 'dtp') {
      env.DIR_EXTRA='/app'
      node('Jtest'){
        dtp.android()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('veracode') {
    if ("${soloStage}" == 'veracode' || "${soloStage}" == '') {
      fueEjecutado=true
      try {
          //veracode.appAndroid()
        } catch (Exception err) {
          println err
        }
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
      withCredentials([string(credentialsId: 'VAULT_TOKEN_APPS', variable: 'VAULT_TOKEN'),
        string(credentialsId: 'ANDROID_API_TOKEN', variable: 'ANDROID_API_TOKEN'),
        string(credentialsId: 'ANDROID_APP_ID_QA', variable: 'ANDROID_APP_ID_QA')]) {
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
