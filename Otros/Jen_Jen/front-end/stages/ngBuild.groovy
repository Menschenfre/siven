def compilar(Boolean imagenProductiva = false) {
  env.ENVIRONMENT_IMAGE = env."${SPACE}_ENVIRONMENT_IMAGE"
  env.OUTPUT_PATH = "dist/"+env.ENVIRONMENT_IMAGE
  env.NOMBRE_CONTAINER = env.PROJECT_NAME+"_ng_build_"+env.VERSION_DESPLIEGUE+"_c"

  env.TAG_CONFIG = "-e"
  def archivoJson = "${env.WORKSPACE}" + "/package.json"
  if (fileExists(archivoJson)){
    def packageJson = readJSON file:'package.json'
    def versionAngular = packageJson.devDependencies."@angular/cli"
    //eliminar simbolo ^ en la versionAngular
    versionAngular = versionAngular.replace("^","")
    versionAngular = versionAngular.replace("~","")
    versionAngular = versionAngular.replace(".","-")
    println "version angular: " + versionAngular
    def scopesAngular = versionAngular.split('-')

    println "Angular" + scopesAngular[0]
    //intercambio entre --config(angular version 7) y --environment(angular version < 7)
    if (scopesAngular[0].toInteger() >= 7){
      env.TAG_CONFIG = "-c"
    }else{
      env.TAG_CONFIG = "-e"
    }
    println env.TAG_CONFIG
  }

  if (imagenProductiva){
    //imagen productiva de canales
    env.NOMBRE_IMAGEN = env.PROJECT_NAME+"/npm_prodinstall:"+env.VERSION_DESPLIEGUE
  } else {
    env.NOMBRE_IMAGEN = env.PROJECT_NAME+"/ng_test:"+env.VERSION_DESPLIEGUE
  }

  println "Compilando fuentes para el ambiente "+env.ENVIRONMENT_IMAGE+", por favor espere..."

  if (!env.PROJECT_NAME.contains("widget")) {
    sh ''' set +x
      docker rm ${NOMBRE_CONTAINER}  || true

      mkdir -p dist
      rm -Rf $(pwd)/dist/*
      docker run -v $(pwd)/dist:/usr/src/app/dist --name ${NOMBRE_CONTAINER} ${NOMBRE_IMAGEN} ng build \
      ${TAG_CONFIG}=${ENVIRONMENT_IMAGE} --aot --output-hashing=all --output-path=${OUTPUT_PATH} --base-href=${BASE_HREF} \
      --no-delete-output-path

    '''
  }else{
    println "FE con elements"
    sh ''' set +x
      docker rm ${NOMBRE_CONTAINER}  || true

      mkdir -p dist
      rm -Rf $(pwd)/dist/*
      docker run -v $(pwd)/dist:/usr/src/app/dist --name ${NOMBRE_CONTAINER} ${NOMBRE_IMAGEN} ng build \
      ${TAG_CONFIG}=${ENVIRONMENT_IMAGE} --aot --output-hashing=all --output-path=${OUTPUT_PATH} --base-href=${BASE_HREF} \
      --no-delete-output-path

      docker rm ${NOMBRE_CONTAINER}  || true

      docker run -v $(pwd)/dist:/usr/src/app/dist --name ${NOMBRE_CONTAINER} ${NOMBRE_IMAGEN} node ./scripts/build.js -c=${ENVIRONMENT_IMAGE}
    '''
  }
}
return this;
