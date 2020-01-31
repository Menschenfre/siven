def compilar(Object common, Object angular8, Boolean imagenProductiva = false) {
  try{  
    if (env.SPACE != "PROD_LOCAL"){
      imagenPreProductiva(common, angular8)
    } else {
      imagenProdTemporal(common, angular8, imagenProductiva)
    }
  } catch (Exception err) {
    common.notificarSlack("tempAzure")
    error ("ERROR -> "+err)
  }
}

def imagenPreProductiva(Object common, Object angular8) {
  sh "sudo chown -R jenkins:jenkins coverage/ || true"
  def envImage = "ic"
  if (env.SPACE == "CERT"){
    envImage = "qa"
  }
  def outputPath = "dist/"+envImage
  def container = env.PROJECT_NAME+"_ng_build_"+env.VERSION_DESPLIEGUE
  def tagConfig = obtenerTagConfig()
  def nombreImagen = env.PROJECT_NAME+"/ng_test:"+env.VERSION_DESPLIEGUE

  println "Compilando fuentes para el ambiente "+envImage+", por favor espere..."

  limpiezaTemporales(container)
  if (!env.PROJECT_NAME.contains("widget")) {
    def flagsAngular = tagConfig+"="+envImage+" --aot --output-hashing=all"
    if (angular8){
      flagsAngular = "--configuration "+envImage
    }
    def compilacion = sh (script: 
      '''docker run -v '''+env.WORKSPACE+'''/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' ng build \
      '''+flagsAngular+''' --output-path='''+outputPath+''' --base-href=${BASE_HREF} --no-delete-output-path 2>&1 || true'''
    , returnStdout: true).trim()
    validacionCompilacion(compilacion)
  } else{
    println "FE con elements"
    sh '''docker run -v '''+env.WORKSPACE+'''/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' ng build \
      '''+tagConfig+'''='''+envImage+''' --aot --output-hashing=all --output-path='''+outputPath+''' --base-href=${BASE_HREF} \
      --no-delete-output-path

      docker rm '''+container+'''  || true

      docker run -v '''+env.WORKSPACE+'''/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' node ./scripts/build.js -c='''+envImage+'''
    '''
  }
  archivosImagenFrontEnd(common, outputPath)
}

def imagenProdTemporal(Object common, Object angular8, Boolean imagenProductiva = false) {
  def envImage = "prodpl"
  def outputPath = "dist/"+envImage
  def container = env.PROJECT_NAME+"_ng_build_"+env.VERSION_DESPLIEGUE+"_c"
  def tagConfig = obtenerTagConfig()

  def nombreImagen = env.PROJECT_NAME+"/ng_test:"+env.VERSION_DESPLIEGUE
  if (imagenProductiva){
    //imagen productiva de canales
    nombreImagen = env.PROJECT_NAME+"/npm_prodinstall:"+env.VERSION_DESPLIEGUE
  }

  println "Compilando fuentes para el ambiente "+envImage+", por favor espere..."

  def flagsEmpresas = ""
  if (env.PROJECT_NAME == "fe-empresasBase" || env.PROJECT_NAME == "fe-empresasbase"){
    flagsEmpresas = "--vendor-chunk --common-chunk --buildOptimizer"
  }

  limpiezaTemporales(container)
  if (!env.PROJECT_NAME.contains("widget")) {
    def flagsAngular = tagConfig+"="+envImage+" --aot --output-hashing=all"
    if (angular8){
      flagsAngular = "--configuration "+envImage
    }
    def compilacion = sh (script: 
      '''docker run -v '''+env.WORKSPACE+'''/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' ng build \
      '''+flagsAngular+''' --output-path='''+outputPath+''' --base-href=${BASE_HREF} --no-delete-output-path '''+flagsEmpresas+''' 2>&1 || true'''
    , returnStdout: true).trim()
    validacionCompilacion(compilacion)
  } else{
    println "FE con elements"
    sh '''docker run -v $(pwd)/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' ng build \
      '''+tagConfig+'''='''+envImage+''' --aot --output-hashing=all --output-path='''+outputPath+''' --base-href=${BASE_HREF} \
      --no-delete-output-path

      docker rm '''+container+'''  || true

      docker run -v $(pwd)/dist:/usr/src/app/dist --name '''+container+''' '''+nombreImagen+''' node ./scripts/build.js -c='''+envImage+'''
    '''
  }
  archivosImagenFrontEnd(common, outputPath)
}

def archivosImagenFrontEnd(Object common, Object outputPath){
  println "Generamos Dockerfile y nginx.conf para front-end"
  def nginxHeader = common.obtenerParametro("context.nginxheader")
  def nginxXframe = common.obtenerParametro("context.nginxheaderxframeoptions")
  sh "git --git-dir=/opt/kubernetes/properties_pipeline_jenkins/.git --work-tree=/opt/kubernetes/properties_pipeline_jenkins/ pull"

  if (nginxHeader){
    if (env.SPACE == "INT"){
      sh "/usr/local/bin/jj2 -v BASEHREF=${BASE_HREF} -v SPACE='IC' /opt/kubernetes/properties_pipeline_jenkins/templates/nginx.conf-header.j2 > nginx.conf"
    } else {
      sh "/usr/local/bin/jj2 -v BASEHREF=${BASE_HREF} -v SPACE=${SPACE} /opt/kubernetes/properties_pipeline_jenkins/templates/nginx.conf-header.j2 > nginx.conf"
    }
  } else {
    sh "/usr/local/bin/jj2 -v BASEHREF=${BASE_HREF} -v nginxXframe=$nginxXframe /opt/kubernetes/properties_pipeline_jenkins/templates/nginx.conf.j2 > nginx.conf"
  }

  if (env.FE_ANGULARJS){
    sh "/usr/local/bin/jj2 -v DIST_PATH=$outputPath -v PATH_FINAL=${BASE_HREF} /opt/kubernetes/properties_pipeline_jenkins/templates/dockerfile-feangularjs.j2 > webPublico/Dockerfile"
  } else {
    sh "/usr/local/bin/jj2 -v OUTPUT_PATH=$outputPath -v PATH_FINAL=${BASE_HREF} /opt/kubernetes/properties_pipeline_jenkins/templates/dockerfile-fe.j2 > Dockerfile"
  }
}

def validacionCompilacion(Object compilacion){
  if (compilacion.contains("heap out of memory")){
    error("Error en compilacion de fuentes -> JavaScript heap out of memory")
  } else if (compilacion.contains("not found: does not exist or no pull access.")){
    error("Por favor lanzar la ejecucion del pipeline desde el inicio para que se generen las imagenes build de manera correcta")
  } else if (compilacion.contains("Error")){
    error(compilacion)
  } else {
    println compilacion
  }
}

def limpiezaTemporales(Object container){
  sh ''' set +x
    docker rm '''+container+'''  || true

    mkdir -p dist
    sudo rm -Rf $(pwd)/dist/*
  '''
}

def obtenerTagConfig(){
  def tagConfig = "-e"
  def archivoJson = "${env.WORKSPACE}" + "/package.json"
  if (fileExists(archivoJson)){
    def packageJson = readJSON file:'package.json'
    def versionAngular = packageJson.devDependencies."@angular/cli"
    //eliminar simbolo ^ en la versionAngular
    versionAngular = versionAngular.replace("^","")
    versionAngular = versionAngular.replace("~","")
    versionAngular = versionAngular.replace(".","-")
    def scopesAngular = versionAngular.split('-')

    //intercambio entre --config(angular version 7) y --environment(angular version < 7)
    if (scopesAngular[0].toInteger() >= 7){
      tagConfig = "-c"
    }
  }
  return tagConfig
}

return this;