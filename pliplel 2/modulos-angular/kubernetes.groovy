def call() {
  def soloStageArray = "${soloStage}".split(';')
  def branchMerge = "${params.branchMerge}"
  env.VALIDAR_RAMA = "${params.validarRama}"

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def construirImagen = load(env.PIPELINE_HOME+'front-end/stages/construirImagen.groovy');
  def pruebasUnitarias = load(env.PIPELINE_HOME+'front-end/stages/pruebasUnitarias.groovy');
  def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');
  def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');

  if(env.VALIDAR_RAMA != "null" && env.VALIDAR_RAMA != null && env.VALIDAR_RAMA == true){
    println "Se valida rama release respecto al master"
    if(!validaciones.ramaActualizada()){
      error("LA RAMA SE ENCUENTRA DESACTUALIZADA RESPECTO AL MASTER")
      currentBuild.result = 'FAILURE'
    }
  }else{
    println "No se valida rama release respecto al master"
  }

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker("", env.BRANCH_NAME)
  def registryURL

  if (env.WORKING_PIPELINE.contains("Release")){
    registryURL = 'http://repository.bci.cl:8081/repository/npm-bci-neg-releases/'
    env.PROJECT_NAME="${params.projectName.toLowerCase()}"
    env.SPACE = "${params.nombreAmbiente}"
  } else {
    registryURL = 'http://repository.bci.cl:8081/repository/npm-bci-neg-snapshots/'
    env.PROJECT_NAME = getProjectName()
    env.SPACE='INT'
  }

  def data = readJSON file:'package.json'
  def registryPackageJson = data.publishConfig.registry
  if (!registryPackageJson.equalsIgnoreCase(registryURL) ){
    if(env.BRANCH_NAME.equals("master") && registryPackageJson.contains("releases")){
      npmLogin('http://repository.bci.cl:8081/repository/npm-bci-neg-releases/')
      println "Rama master correcta"
    } else {
      error ("Corregir archivo package.json, registry no coincide con despliegue en npm-bci-neg-releases")
    }
  }

  npmLogin(registryURL)

  stage ('veracode') {
    if (soloStageArray.contains('veracode') || "${soloStage}" == ''){
        try {
          //veracode.front()
        } catch (Exception err) {
          println err
        }
    }
  }

  stage ('prepararImagenesNpm') {
    if (soloStageArray.contains('prepararImagenesNpm') || "${soloStage}" == '') {
      validaciones.valUrlInTsFiles()
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
      npmLogin('http://repository.bci.cl:8081/repository/npm-bci-neg-releases-certified/')
      dir('dist'){
        def dataDist = readJSON file: 'package.json'
        dataDist.publishConfig.registry = "http://repository.bci.cl:8081/repository/npm-bci-neg-releases-certified/"
        writeJSON file: 'package.json', json: dataDist
      }
      sh (script: 'npm run publicar', returnStdout: true)
    }
  }

  if ("${soloStage}" == 'rollback' && env.WORKING_PIPELINE.contains("Release")) {
    stage ('rollback') {
      rollback.modulosFrontCertified()
    }
  }

  if ("${soloStage}" == 'MergeDevelop' && env.WORKING_PIPELINE.contains("Release")) {
    stage ('MergeDevelop') {
      common.mergeRama(branchMerge, nombreBranch)
    }
  }

  if ("${soloStage}" == 'MergeMaster' && env.WORKING_PIPELINE.contains("Release")) {
    stage ('MergeMaster') {
      common.mergeRama(branchMerge, nombreBranch)
    }
  }
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

def npmLogin(String urlRegistro){
  println "Conectandonos al repositorio nexus"
  env.URL_LOGIN = urlRegistro
  sh ''' set +x
    echo "npm login --registry=${URL_LOGIN}"
    authToken=$(curl -s -H "Accept: application/json" -H "Content-Type:application/json" -X PUT --data '{"name": \"'${NEXUS_USR}'\", "password": \"'${NEXUS_PSW}'\"}'  ${URL_LOGIN}-/user/org.couchdb.user:${NEXUS_USR} 2>&1 | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["token"];')

    fullRegistry=$(echo "${URL_LOGIN}:_authToken=${authToken}" | sed -e "s/http://g")
    set +e
    resultNpmrc=$(cat ${JENKINS_HOME}/.npmrc | grep ${fullRegistry})
    set -e
    if [[ -z "${resultNpmrc}" ]]; then
      echo ${fullRegistry} >> ${JENKINS_HOME}/.npmrc
    fi
    echo "npm info ok"
    echo ""
  '''
}

return this;
