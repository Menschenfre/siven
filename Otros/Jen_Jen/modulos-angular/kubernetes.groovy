def call() {
  def soloStageArray = "${soloStage}".split(';')
  env.PORT_NUMBER = '80'
  env.BLUEMIX_HOME = '.bluemix'
  
  def common, construirImagen, pruebasUnitarias
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    construirImagen = fileLoader.load('front-end/stages/construirImagen');
    pruebasUnitarias = fileLoader.load('front-end/stages/pruebasUnitarias');
  }

  env.BRANCH_NAME = common.branchName()
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", env.BRANCH_NAME)
  def registryURL
  
  if (env.WORKING_PIPELINE.contains("Release")){
    registryURL = 'http://repository.bci.cl:8081/repository/npm-bci-neg-releases/'
    env.PROJECT_NAME="${params.projectName.toLowerCase()}"
  } else {
    registryURL = 'http://repository.bci.cl:8081/repository/npm-bci-neg-snapshots/'
    env.PROJECT_NAME = getProjectName()
  }
  
  def data = readJSON file:'package.json'
  def registryPackageJson = data.publishConfig.registry
  if (!registryPackageJson.equalsIgnoreCase(registryURL) ){    
    if(env.BRANCH_NAME.equals("master") && registryPackageJson.contains("releases")){
      common.npmLogin('http://repository.bci.cl:8081/repository/npm-bci-neg-releases/')
      println "Rama master correcta"    
    } else {
      error ("Corregir archivo package.json, registry no coincide con despliegue en npm-bci-neg-releases")
    }   
  }
  
  common.npmLogin(registryURL)
  
  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      common.modificarNpmrcNPMInstall()
      construirImagen.generarPreinstall()
      construirImagen.generarNpminstall(true)
    }
  }

  stage ('pruebasUnitarias') {
    if (soloStageArray.contains('pruebasUnitarias') || "${soloStage}" == '') {
      sh (script: 'sudo rm -r dist || true; sudo rm dist.tgz || true', returnStdout: false)
      pruebasUnitarias.call()
      sh (script: 'sudo chown -R jenkins:jenkins coverage/*', returnStdout: false)
    }
  }

  stage ('generacionPackage') {
    if (soloStageArray.contains('generacionPackage') || "${soloStage}" == '') {
      sh ''' set +x
      docker rm ${PROJECT_NAME}_ng_build_${VERSION_DESPLIEGUE}_c || true
      rm -r $(pwd)/dist || true
      
      mkdir -p dist
      
      docker run --name ${PROJECT_NAME}_ng_build_${VERSION_DESPLIEGUE}_c ${PROJECT_NAME}/ng_test:${VERSION_DESPLIEGUE} npm run package
      docker cp ${PROJECT_NAME}_ng_build_${VERSION_DESPLIEGUE}_c:/usr/src/app/dist .
      docker cp ${PROJECT_NAME}_ng_build_${VERSION_DESPLIEGUE}_c:/usr/src/app/dist.tgz .
      '''
    }
  }

  stage ('publicacionModulo') {
    if (soloStageArray.contains('publicacionModulo') || "${soloStage}" == '') {
      sh (script: 'npm run publicar', returnStdout: true)
    }
  }

  if ("${soloStage}" == 'moduloOficial' && env.WORKING_PIPELINE.contains("Release")) {
    stage ('moduloOficial') {
      common.npmLogin('http://repository.bci.cl:8081/repository/npm-bci-neg-releases-certified/')
      dir('dist'){
        def dataDist = readJSON file: 'package.json'
        dataDist.publishConfig.registry = "http://repository.bci.cl:8081/repository/npm-bci-neg-releases-certified/"
        writeJSON file: 'package.json', json: dataDist
      }
      sh (script: 'npm run publicar', returnStdout: true)
    }
  }
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
