import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

def registrar(Object productoApiConnect, Object ambiente, Object developerApp = '', Object nodo = ''){
  println "Registro en api management "+ambiente
  def apigeeEnv= obtenerVirtualHost(ambiente)
  def apigeeUrl = obtenerUrlCurl(ambiente)
  def apigeeProfile = obtenerProfileDespliegue(ambiente)
  productoApiConnect = productoApiConnect.toLowerCase()

  generacionArchivos(ambiente)

  println "Subimos revision a apigee"
  sh "apigeelint -s ${DEPLOY_NAME}/apiproxy -f table.js"

  if(nodo != ''){
    stash includes: env.DEPLOY_NAME+'/**/*', name: 'apigeeSources'
    node (nodo){
      unstash 'apigeeSources'
      sh "/home/bciljkns/apache-maven-3.5.0/bin/mvn -q -f ${DEPLOY_NAME}/pom.xml install -P"+apigeeProfile+" -Dusername=${APIGEE_USR} -Dpassword=${APIGEE_PSW}"
      revisionProductoSuscription(apigeeUrl, productoApiConnect, apigeeEnv, developerApp)
    }
  } else {
    sh "/opt/apache-maven-3.5.0/bin/mvn -q -f ${DEPLOY_NAME}/pom.xml install -P"+apigeeProfile+" -Dusername=${APIGEE_USR} -Dpassword=${APIGEE_PSW}"
    revisionProductoSuscription(apigeeUrl, productoApiConnect, apigeeEnv, developerApp)
  }
}

def revisionProductoSuscription(Object apigeeUrl, Object productoApiConnect, Object apigeeEnv, Object developerApp){
  if (existeProducto(apigeeUrl, productoApiConnect)){
    println "Producto existe, continuamos con su edicion"
    def proxies = obtenerProxiesProducto(apigeeUrl, productoApiConnect)
    if (proxies.contains(env.DEPLOY_NAME)){
      println "Proxy ya existe en producto"
    } else {
      println "Agregamos proxy al producto"
      proxies = agregamosProxy(proxies, env.DEPLOY_NAME)
      modificacionProducto("PUT", apigeeUrl+"/apiproducts/"+productoApiConnect, proxies, apigeeEnv, productoApiConnect)
    }
  } else {
    println "Creamos producto con api nuevo"
    modificacionProducto("POST", apigeeUrl+"/apiproducts", "\""+env.DEPLOY_NAME+"\"", apigeeEnv, productoApiConnect)
  }
  //Revisar forma de hacer automatico el paso final de suscribir en APP el producto
  if (validarDeveloperApp(developerApp, true, apigeeUrl) == "Webapp_existe"){
    for (String app:developerApp){
      validacionProductoEnAPP(app, apigeeUrl, productoApiConnect)
    }
  }
}

def validarDeveloperApp(Object webapp, Object regApiConnect, Object url = ''){
  println "Validamos si developerApp esta declarada en container_params.json"

  if (regApiConnect){
    if (url == ''){
      url = obtenerUrlCurl(env.SPACE)
    }
    def listado = curlGet(url+"/developers/deploy.ci@bci.cl/apps")
    if (webapp == null){
      println "Debe existir api.product.apps en container_params.json para suscribir producto en apigee\n\nFormato debe ser apps: [ \"NOMBRE_APP1\", \"NOMBRE_APP2\" ]\n(NOMBRE_APP2 en caso de que se requiera suscribir a mas de una app)\n\nLos valores se deben obtener del siguiente listado "+listado
      return "Webapp_error"
    }

    if (webapp.getClass().getSimpleName() != "JSONArray"){
      println "Formato de key api.product.apps incorrecto\nFormato debe ser apps: [ \"NOMBRE_APP1\", \"NOMBRE_APP2\" ]\n(NOMBRE_APP2 en caso de que se requiera suscribir a mas de una app)"
      return "Webapp_error"
    }

    for (String app:webapp){
      if (!listado.contains(app)){
        println "Webapp declarada en json \""+app+ "\" no existe en cluster de apigee, por favor validar nombre correcto del listado "+listado
        println "Corrija el nombre de la app en caso de que este mal escrita. En caso de que la app necesite agregarse al listado, favor contactarse con el devops practitioner con la solicitud"
        return "Webapp_error"
      }
    }
  }
  return "Webapp_existe"
}

def agregamosProxy(Object proxies, String nuevoProxy){
    def listado = ""
    for (i = 0; i < proxies.size(); i++) {
      listado += "\""+proxies[i]+"\","
    }
    listado += "\""+nuevoProxy+"\""
    return listado
}

def validacionProductoEnAPP(Object developerApp, Object url, Object productoApiConnect){  
  println "Obtenemos consumerKey de la app "+developerApp
  def data = curlGet(url+"/developers/deploy.ci@bci.cl/apps/"+developerApp)

  def consumerKey = parsearJson(data, false)
  println "Obtenemos productos asociados a developer app "+developerApp
  def products = curlGet(url+"/developers/deploy.ci@bci.cl/apps/"+developerApp+"/keys/"+consumerKey)
  def nombreProductos = parsearJson(products, true)
  if (!encontrarProductoEnApp(nombreProductos, productoApiConnect)){
    println "Agregamos el producto a developer app "+developerApp
    def proxies = ""
    for (String prod:nombreProductos){
        proxies = proxies + "\"" + prod + "\","
    }
    proxies = proxies + "\""+productoApiConnect+"\""
    env.PROXIES = proxies
    def resCurl = sh (script: ''' set +x
      curl -s -u ${APIGEE_USR}:${APIGEE_PSW} -X POST --header "Content-Type: application/json" -d "{ 
      \\"apiProducts\\": [${PROXIES}], 
      \\"attributes\\": [] 
      }" "'''+url+'''/developers/deploy.ci@bci.cl/apps/'''+developerApp+'''/keys/'''+consumerKey+'''"
    ''', returnStdout: true).trim()
    if (resCurl.contains("\"message\"")){
      error(resCurl)
    }
  } else {
    println "Producto ya se encuentra en developer app "+developerApp
  }
}

def encontrarProductoEnApp(Object listaProductos, Object producto){
  for (String prod:listaProductos){
    if(producto.equalsIgnoreCase(prod)){
      return true
    }
  }
  return false
}

@NonCPS
def parsearJson(Object data, Boolean productos){
  JsonSlurper slurper = new JsonSlurper()
  def builder = new JsonBuilder(slurper.parseText(data))
  if (productos){
    return builder.content.apiProducts.apiproduct
  } else {
    return builder.content.credentials[0].consumerKey
  }
}

def generacionArchivos(Object ambiente){
  println "Editamos valores para zip de apigee"
  sh "git --git-dir=/opt/kubernetes/properties_pipeline_jenkins/.git --work-tree=/opt/kubernetes/properties_pipeline_jenkins/ pull"
  sh ''' set +x
    rm -r ${DEPLOY_NAME} || true
    mkdir -p ${DEPLOY_NAME}
    cp -r /opt/kubernetes/properties_pipeline_jenkins/Proxy-BCI-Template/* ${DEPLOY_NAME}
    mv ${DEPLOY_NAME}/apiproxy/Proxy-BCI-Template.xml ${DEPLOY_NAME}/apiproxy/${DEPLOY_NAME}.xml
  '''
  editarArchivos("Proxy-BCI-Template", env.DEPLOY_NAME)
  editarArchivos("proxy-basepath", obtenerBaseHref())
  editarArchivos("proxy-virtualhost", obtenerVirtualHost(ambiente))
  editarArchivos("proxy-vip-virtualhost", obtenerVIPVirtualHost(ambiente))
  editarArchivos("ms-k8s-ssl", obtenerTargetServer(ambiente))
}

def editarArchivos(Object reemplazar, Object variable){
  def archivos = sh (script: "grep -rl $reemplazar ${DEPLOY_NAME}", returnStdout: true).trim().split("\n")
  for(String archivo: archivos){
    def datos = readFile archivo
    datos = datos.replace(reemplazar, variable)
    writeFile file: archivo, text: datos
  }
}

def obtenerProxiesProducto(Object url, Object productName){
  def proxies = curlGet(url+"/apiproducts/"+productName)
  def parser = new JsonSlurper()
  def json = parser.parseText(proxies)
  return json.proxies
}

def modificacionProducto(Object metodo, Object url, Object proxies, Object apigeeEnv, Object productoApiConnect){
  env.PROXIES = proxies
  def resCurl = sh (script: ''' set +x
    curl -s -u ${APIGEE_USR}:${APIGEE_PSW} -X '''+metodo+''' --header "Content-Type: application/json" -d "{
    \\"name\\" : \\"'''+productoApiConnect+'''\\",
    \\"displayName\\": \\"'''+productoApiConnect+'''\\",
    \\"approvalType\\": \\"auto\\",
    \\"attributes\\": [
      {
        \\"name\\": \\"access\\",
        \\"value\\": \\"private\\"
      }
    ],
    \\"description\\": \\"\\",
    \\"apiResources\\": [],
    \\"environments\\": [ \\"'''+apigeeEnv.replace("-cloud", "").replace("-cloud", "")+'''\\"],
    \\"proxies\\": [${PROXIES}],
    \\"quota\\": \\"\\",
    \\"quotaInterval\\": \\"\\",
    \\"quotaTimeUnit\\": \\"\\",
    \\"scopes\\": [\\"\\"]
    }" "'''+url+'''"
  ''', returnStdout: true).trim()
  if (resCurl.contains("\"message\"")){
    error(resCurl)
  }
}

def existeProducto(Object url, Object productName){
  def existeProducto = curlGet(url+"/apiproducts/"+productName)
  if (existeProducto.contains("keymanagement.service.apiproduct_doesnot_exist")){
    return false
  } else {
    return true
  }
}

def curlGet(Object url){
  def resultado = sh (script: '''
    curl -s -u ${APIGEE_USR}:${APIGEE_PSW} -X GET "'''+url+'''/"
  ''', returnStdout: true).trim()
  return resultado
}

def eliminarApiDeProducto(Object productoApiConnect, Object nodo, Object ambiente){
  def apigeeEnv= obtenerVirtualHost(ambiente)
  def apigeeUrl = obtenerUrlCurl(ambiente)
  node (nodo){
    def proxies = obtenerProxiesProducto(apigeeUrl, productoApiConnect)
    if (proxies.contains(env.DEPLOY_NAME)){
      println "Eliminamos proxy del producto"
      proxies = eliminamosProxy(proxies, env.DEPLOY_NAME)
      modificacionProducto("PUT", apigeeUrl+"/apiproducts/"+productoApiConnect, proxies, apigeeEnv, productoApiConnect)
    }
  }
}

def eliminamosProxy(Object proxies, String proxyEliminar){
    def listado = ""
    for (i = 0; i < proxies.size(); i++) {
      if (proxies[i] != proxyEliminar){
        listado += "\""+proxies[i]+"\","
      }
    }
    if (listado.endsWith(",")){
      listado = listado.substring(0, listado.length() - 1);
    }
    return listado
}

def obtenerUrlCurl(Object ambiente){
  switch(ambiente){
    case "INT":
      return 'http://10.242.32.6:8080/v1/organizations/bci-dsr'
    case "CERT":
      return 'http://10.243.32.8:8080/v1/organizations/bci-crt'
    case "ACONCAGUA":
      return 'http://10.105.4.33:8080/v1/organizations/bci-prd'
    case "LONGOVILO":
      return 'http://10.105.0.34:8080/v1/organizations/bci-prd'
    default:
      error('obtenerUrlCurl() -> No space defined')
  }
}

def obtenerHost(Object ambiente){
  switch(ambiente){
    case "INT":
      return "http://api-dsr01.bci.cl"
    case "CERT":
      return "http://api-crt01.bci.cl"
    case "ACONCAGUA":
      return 'http://api-prd01.bci.cl'
    case "LONGOVILO":
      return 'http://api-prd02.bci.cl'
    default:
      error('obtenerHost() -> No space defined')
  }
}

def obtenerBaseHref(){
  if (env.PIPELINE_APP.equalsIgnoreCase("BFF")){
    return env.BASE_HREF
  } else {
    def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
    if (fileExists(archivoJson) && env.REG_EUREKA){
      return env.BASE_HREF
    } else {
      if (env.PIPELINE_APP == "MOBILE"){
        env.DIR_EXTRA='app/'
      }
      def msAppName = sh (script: ''' set +x;find ${DIR_EXTRA}src -name bootstrap.properties | xargs awk \'/spring.application.name/ {split($0,a,"="); print a[2]}\'''', returnStdout: true).trim()
      msAppName = "/"+msAppName.replace(":latest", "").replace("\${MS_VERSION}", env.VERSION_COMPONENTE).toLowerCase()
      return msAppName
    }
  }
}

def obtenerVirtualHost(Object ambiente){
  switch(ambiente){
    case "INT":
      return 'dsr'
    case "CERT":
      return 'crt'
    case "ACONCAGUA":
    case "LONGOVILO":
      return 'prd-cloud'
    default:
      error('obtenerVirtualHost() -> No space defined')
  }
}

def obtenerProfileDespliegue(Object ambiente){
  switch(ambiente){
    case "INT":
      return 'dsr'
    case "CERT":
      return 'crt'
    case "ACONCAGUA":
      return 'prd01'
    case "LONGOVILO":
      return 'prd02'
    default:
      error('obtenerProfileDespliegue() -> No space defined')
  }
}

def obtenerVIPVirtualHost(Object ambiente){
  switch(ambiente){
    case "INT":
      return 'vip-desa'
    case "CERT":
      return 'vip-cert'
    case "ACONCAGUA":
    case "LONGOVILO":
      return 'vip-prod'
    default:
      error('obtenerVIPVirtualHost() -> No space defined')
  }
}

def obtenerTargetServer(Object ambiente){
  switch(ambiente) {
    case "INT":
    case "CERT":
      return "ms-k8s-ssl"
    case "PROD_LOCAL":
    case "ACONCAGUA":
    case "LONGOVILO":
      return "ms-k8s"
    default:
      error("obtenerTargetServer() -> No space defined")
    break
  }
}

return this