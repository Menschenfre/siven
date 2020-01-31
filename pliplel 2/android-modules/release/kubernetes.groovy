def call(){
  //integracion continua
  def soloStageArray = "${soloStage}".split(';')

  env.ANDROID_HOME = '/opt/android-sdk'
  env.PROJECT_NAME = getProjectName()

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
      checkingQA.ejecutar('/'+checkingQAPath().trim())
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('dtp') {
    if (soloStageArray.contains('dtp')) {
      node('Jtest'){
        dtp.android()
      }
    } else {
      println "Condicion no cumplida, ignoramos stage"
    }
  }

  stage ('veracode') {
    if (soloStageArray.contains('veracode')) {
      fueEjecutado=true
      try {
          //veracode.android()
        } catch (Exception err) {
          println err
        }
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
