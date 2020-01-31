import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

def registrar(String productoApiConnect){
    env.PRODUCT_NAME=productoApiConnect
    env.API_FILENAME = "api-"+env.PROJECT_NAME+"_"+env.BRANCH_NAME+".yaml"
    env.PRODUCT_FILENAME = "product-"+env.PRODUCT_NAME+"_product_1.0.0.yaml"
    def urlBaseServicio, tlsProfile, nombreAmbiente

    if (env.SPACE == "PROD_LOCAL"){
      urlBaseServicio = 'https://bciapi.bci.cl'
      tlsProfile = 'ingress-tls'
      env.API_CONNECT_HOST='161.131.199.247'
      env.ORG_NAME='bci-produccion'
    } else {
      urlBaseServicio = obtenerHost()
      tlsProfile = obtenerTlsProfile(env.SPACE)
      env.API_CONNECT_HOST='us.apiconnect.ibmcloud.com'
      nombreAmbiente = conversionAmbiente(env.SPACE).toLowerCase()
      if (env.SPACE.contains("PROD")){
        env.ORG_NAME='bci-produccion-'+nombreAmbiente
      } else {
        if (env.SPACE.equals("INT") && env.INT_CLUSTER_NAME.equals("bci-api-ic01")){
          env.ORG_NAME='bci-api-dev'
        } else {
          env.ORG_NAME='bci-api-'+nombreAmbiente
        }
      }
    }

    def data = readJSON file: env.WORKSPACE+"/spec.json"
    def output = generarAPI(String.valueOf(data), 'api-bci', env.PROJECT_NAME, env.BRANCH_NAME, urlBaseServicio, tlsProfile, obtenerBaseHref())
    writeFile file: env.WORKSPACE+"/"+env.API_FILENAME, text: output
    
    timeout(time: 5, unit: 'MINUTES') {
      if (env.SPACE == "PROD_LOCAL"){
          sh '''apic login --username ${PASSLOCAL_APIC_USER} --password ${PASSLOCAL_APIC_PASS} --server ${API_CONNECT_HOST}'''
      } else {
          sh '''
          BLUEMIX_CREDENTIALS_USR=${SPACE}_BLUEMIX_CREDENTIALS_USR
          BLUEMIX_CREDENTIALS_PSW=${SPACE}_BLUEMIX_CREDENTIALS_PSW
          apic login --username ${!BLUEMIX_CREDENTIALS_USR} --password ${!BLUEMIX_CREDENTIALS_PSW} --server ${API_CONNECT_HOST}
          '''
      }
      
      sh "apic drafts:push ${API_FILENAME} --organization ${ORG_NAME} --server ${API_CONNECT_HOST}"
      sh "mkdir -p yamlApiConnect; rm -Rf yamlApiConnect/*"
      dir('yamlApiConnect') {
        def infoProducto = sh (script: "apic drafts:get product-${PRODUCT_NAME} --organization ${ORG_NAME} --server ${API_CONNECT_HOST}", returnStdout: true).trim()
        sh '''cp ../${API_FILENAME} .
        rm ${PRODUCT_FILENAME} || true'''
        def yamlMod, yaml
        if (infoProducto != null && infoProducto != ""){
          println "*****Modificamos producto*****"
          def apisInicial = generarListaApis(infoProducto)
          sh "apic drafts:pull product-${PRODUCT_NAME}:1.0.0 --organization ${ORG_NAME} --server ${API_CONNECT_HOST}"
          yaml = readYaml file: env.PRODUCT_FILENAME
          yamlMod = establecerApisIniciales(yaml, apisInicial)
          if (!infoProducto.contains("api-"+env.PROJECT_NAME+":"+env.BRANCH_NAME)){
            //Si el api no esta en el producto, lo incorporamos al grupo de apis en el yaml
            println "*****Agregamos api a producto*****"
            yamlMod = agregarApi(yamlMod)
          } else{
            println "*****Api ya existe en producto*****"
          }
          sh "rm ${PRODUCT_FILENAME} || true"
          writeYaml file: env.PRODUCT_FILENAME, data: yamlMod
          sh "sed -i \"s/- api/  api/g\" ${PRODUCT_FILENAME}"
        } else {
          println "*****Creamos producto*****"
          sh "apic create --type product --title product-${PRODUCT_NAME} --apis ${API_FILENAME} --name product-${PRODUCT_NAME} --version 1.0.0 --filename ${PRODUCT_FILENAME}"
          yaml = readYaml file: env.PRODUCT_FILENAME
          yamlMod = modificarPlan(yaml)
          sh "rm ${PRODUCT_FILENAME} || true"
          writeYaml file: env.PRODUCT_FILENAME, data: yamlMod
          sh '''
          echo "    apis: {}" >> ${PRODUCT_FILENAME}
          '''
        }

        if (env.PIPELINE_APP != "BFF"){
          clearApisFiles()
        }
        
        sh '''
        apic drafts:push ${PRODUCT_FILENAME} --organization ${ORG_NAME} --server ${API_CONNECT_HOST}
        apic drafts:publish product-${PRODUCT_NAME}:1.0.0 --catalog 'api-bci' --organization ${ORG_NAME} --server ${API_CONNECT_HOST}
        apic products:set product-${PRODUCT_NAME}:1.0.0 --visibility authenticated --subscribability authenticated --catalog api-bci --organization ${ORG_NAME} --server ${API_CONNECT_HOST}
        '''
      }
      sh "apic logout --server ${API_CONNECT_HOST}"
    }
}

@NonCPS
def establecerApisIniciales(Object data, Object apisIniciales){
  data.apis = apisIniciales
  return data
}

@NonCPS
def generarListaApis(infoProducto){
  def amap = []
  def lineas = infoProducto.split("\n")
  for(String label: lineas){
    if(label.startsWith("apis")){
      def valores = label.split(" ")
      for(String apis: valores){
        if(apis.startsWith("api-")){
          def api = apis.split(":")
          String nombre = api[0]+"_"+api[1]
          LinkedHashMap map = [:]
          map.put(nombre, ['\$ref': nombre+".yaml"])
          amap << map
        }
      }
    }   
  }
  return amap
}

@NonCPS
def agregarApi(Object data){
    def apiName = "api-"+env.PROJECT_NAME+"_"+env.BRANCH_NAME
    LinkedHashMap map = [:]
    map.put(apiName, ['\$ref': apiName+".yaml"])
    data.apis << map
    return data
}

@NonCPS
def modificarPlan(Object data){
    data.plans = ['Ilimitado': ['title': 'Ilimitado']]
    data.visibility.view.type = 'authenticated'
    return data
}

@NonCPS
def generarAPI(inputJson, catalogo, nombreServicio, apiVersion, urlBaseServicio, tlsProfile, baseHref){
    JsonSlurper slurper = new JsonSlurper()
    def builder = new JsonBuilder(slurper.parseText(inputJson))
    builder.content.info."x-ibm-name" = 'api-'+nombreServicio
    builder.content.info.title = 'api-'+nombreServicio
    builder.content.info.version = apiVersion
    builder.content.host = catalogo
    builder.content.schemes = ['https']
    builder.content.basePath = baseHref
    builder.content.info.description = builder.content.info.description.replace("\n\n", "--------")

    builder.content.securityDefinitions = slurper.parseText("""
            {
                "clientIdHeader": {
                    "type": "apiKey",
                    "in": "header",
                    "name": "X-IBM-Client-Id"
                }
            }
            """)

    builder.content.security = slurper.parseText("""
            [
                {
                    "clientIdHeader": [ ]
                }
            ]
            """)

    if (env.SPACE == "PROD_LOCAL"){
      builder.content."x-ibm-configuration" = slurper.parseText("""
        {
            "testable": true,
            "enforced": true,
            "cors": {
                "enabled": true
            },
            "assembly": {
                "execute": [
                    {
                        "invoke": {
                            "target-url": "${urlBaseServicio}\$(request.path)",
                            "secure-gateway": false,
                            "tls-profile": "${tlsProfile}"
                        }
                    }
                ]
            }
        }
        """)
    } else {
      builder.content."x-ibm-configuration" = slurper.parseText("""
        {
            "testable": true,
            "enforced": true,
            "cors": {
                "enabled": true
            },
            "assembly": {
                "execute": [
                    {
                        "gatewayscript": {
                            "title": "gatewayscript",
                            "version": "1.0.0",
                            "source": "apim.setvariable('message.headers.origin-addr', '127.0.0.1');"
                        }
                    }, {
                        "invoke": {
                            "target-url": "${urlBaseServicio}\$(request.path)",
                            "secure-gateway": false,
                            "tls-profile": "${tlsProfile}"
                        }
                    }
                ]
            }
        }
        """)
    }
    return builder.toPrettyString()

}

def obtenerSpecJson(Object login, Object common){
  def sitioDespliegue = ''
  if(env.SPACE.equals("PROD_LOCAL")){
    sitioDespliegue = 'PROVIDENCIA'
    login.validacionConexion(common, sitioDespliegue)
  }
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  def port = "8080"
  if (env.PIPELINE_APP == "BFF"){
    env.rutaSpec = env.BASE_HREF+"/spec"
    port = "3000"
  } else if (env.REG_EUREKA){
    env.rutaSpec = env.BASE_HREF+"/v2/api-docs"
  } else{
    env.rutaSpec = "/v2/api-docs"
  }

  sh ''' set +x
    # Limpiar spec.json del workspace, para evitar error en multiples ejecuciones
    rm ${WORKSPACE}/spec.json || true

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    if [[ "${VISIBILITY}" == "False" ]]; then
      privActivado="-priv"
    fi

    podName=$(/usr/local/bin/kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} | grep ${PROJECT_NAME} | awk '{print $1}' | head -1)

    #exec sobre el container para obtener json y guardarlo en archivo
    /usr/local/bin/kubectl -n bci-api exec ${podName} -- /usr/bin/curl -s http://localhost:'''+port+'''${rutaSpec} > ${WORKSPACE}/spec.json
  '''
  if (!validacionSpec()){
    error("Swagger no es valido")
  }
}

def validacionSpec(){
  def specJson = sh (script: "cat spec.json", returnStdout: true).trim()
  def mensajeError = ""
  def retorno = true
  if (specJson.contains("Full authentication is required to access this resource")){
    mensajeError = "Archivo swagger contiene error 'Full authentication is required to access this resource', contactarse con celula para solucionar el problema"
    retorno = false
  } else if (specJson.contains("\"error\":\"Not Found\"")){
    mensajeError = "Archivo swagger necesario para registrar en api connect no se encuentra configurado en proyecto, contactarse con celula para solucionar el problema"
    retorno = false
  } else if (specJson.contains("{\"codigo\":\"400\",\"mensaje\":\"Cabeceras incompletas\"}")){
    mensajeError = " contenido de swagger -> {\"codigo\":\"400\",\"mensaje\":\"Cabeceras incompletas\"}"
    retorno = false
  } else if (specJson.equalsIgnoreCase("")){
    mensajeError = "archivo vacio"
    retorno = false
  }
  println "Validando contenido correcto de archivo swagger"
  //def retstatus = sh(script: 'swagger-spec-validator spec.json', returnStatus: true)
  def retstatus = 0
  if (retstatus == 0 && retorno){
    return true
  } else {
    println mensajeError
    return false
  }
}

def validarNombreProducto(String productoApiConnect){
  if (productoApiConnect.equals("") || productoApiConnect.equals(null)){
    println "Valor api.product.name es incorrecto, favor corregir archivo container_params.json"
    return false
  } else {
    return true
  }
}

def obtenerHost(){
  def nombreArchivo = "ingress-"+env."${SPACE}_CLUSTER_NAME"+".yml"
  def rutaingress

  if (env.SPACE == "PROD_LOCAL"){
    rutaingress = '/opt/kubernetes/archivos-ingress-prod'
  }else{
    rutaingress = '/opt/kubernetes/archivos-ingress'
  }

  dir(rutaingress){
    def data = readYaml file: nombreArchivo
    return "http://"+data.spec.rules[0].host
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

def obtenerTlsProfile(String space){
    def tlsprofile
    switch(space){
      case "INT":
        tlsprofile = 'ingress-tls-profile'
      break
      case "CERT":
        tlsprofile = 'ingress-tls-profile'
      break
      case "PROD_LOCAL":
        tlsprofile = 'ingress-tls'
      break
        default:
        error('No space defined')
      break
    }
    return tlsprofile
}


def conversionAmbiente(String space){
    def ambiente
    switch(space){
      case "INT":
        ambiente = 'Integracion'
      break
      case "CERT":
        ambiente = 'QA'
      break
      case "PROD_LOCAL":
        ambiente = 'Produccion'
      break
        default:
        error('No space defined')
      break
    }
    return ambiente
}

def clearApisFiles(){
  println("iniciando limpieza de archivos apis")
  def apisListFinal = sh(script: "set +x;ls | grep 'api-'", returnStdout:true).split("\n")
  for(String api : apisListFinal){
      sh'''set +x;sed -i '/^$/d' '''+api
  }
}

return this;
