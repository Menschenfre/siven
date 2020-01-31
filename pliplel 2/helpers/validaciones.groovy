def validarArchivosTS(){
  def mensajeUnstable = "Corregir los siguientes archivos que contienen urls que apuntan al api connect pre productivo \ny deberian apuntar al apigee pre productivo:\n"
  def archivosMensaje = "- "
  def urlApiConnect = "https://api.us.apiconnect.ibmcloud.com/bci-api-dev/api-bci"
  def urlApiConnectDomain = "https://api.us-south.apiconnect.appdomain.cloud/bci-api-dev/api-bci"
  def urlApiConnectQA = "https://api.us.apiconnect.ibmcloud.com/bci-api-qa/api-bci"
  def urlApiConnectDomainQA = "https://api.us-south.apiconnect.appdomain.cloud/bci-api-qa/api-bci"
  def flagValidacion = true

  if (env.SPACE == "INT" || env.SPACE == "CERT"){
    println "Validando urls en archivos environment para ic y qa"
    if (env.FE_ANGULARJS){
      def nombreArchivo = "app/scripts/environment_config.js"
      def data = readFile nombreArchivo
      if (data.contains(urlApiConnect) || data.contains(urlApiConnectDomain) || data.contains(urlApiConnectQA) || data.contains(urlApiConnectDomainQA)){
        archivosMensaje = archivosMensaje + nombreArchivo + "\n"
        flagValidacion = false
      }
    } else {
      def nombreArchivo = "src/environments/environment.ic.ts"
      def nombreArchivoQA = "src/environments/environment.qa.ts"

      //validamos archivo para IC
      def data = readFile nombreArchivo
      if (data.contains(urlApiConnect) || data.contains(urlApiConnectDomain)){
        archivosMensaje = archivosMensaje + nombreArchivo + "\n"
        flagValidacion = false
      }

      def dataQA = readFile nombreArchivoQA
      if (dataQA.contains(urlApiConnectQA) || dataQA.contains(urlApiConnectDomainQA)){
        archivosMensaje = archivosMensaje + "- " + nombreArchivoQA
        flagValidacion = false
      }
    }
  } 

  if (archivosMensaje != "- "){
    println mensajeUnstable + archivosMensaje
  }
  return flagValidacion
}

//dir ms = "${env.WORKSPACE}/src/main/resources/bootstrap.properties"
//dir ig = "${env.WORKSPACE}/${env.PROJECT_NAME}-server/src/main/resources/bootstrap.properties"
//dir api = "${env.WORKSPACE}/app/src/main/resources/bootstrap.properties"

def bootstrapProp(String dir, String type){

  def dirGradle = ""
  if (type == "MS" || type == "CRONJOB") {
    dirGradle = "${env.WORKSPACE}/build.gradle"
  }
  if (type == "IG") {
    dirGradle = "${env.WORKSPACE}/${env.PROJECT_NAME}-server/build.gradle"
  }
  if (type == "API") {
    dirGradle = "${env.WORKSPACE}/app/build.gradle"
  }

  def buildGradle = readProperties file:dirGradle
  def value = buildGradle['springBootVersion']
  println value + " Version en el build.gradle"
  def scopeSpring = value.replace("\'","").replace(".","-").split("-")

  def sprinConfigProperti = "spring.cloud.config.fail-fast"
  if (scopeSpring[0].toInteger() < 2) {
    sprinConfigProperti = "spring.cloud.config.failFast"
  }

  def props = readProperties file:dir
  def configFail = props[sprinConfigProperti]
  if (configFail == null || configFail.equalsIgnoreCase("false")){
    println "la configuracion " + sprinConfigProperti + " debe estar en true para ejecutar el pipeline en el archivo bootstrap.properties"
    return false
  }

  def vaultFail = props['spring.cloud.vault.fail-fast']
  if (vaultFail == null || vaultFail.equalsIgnoreCase("false")){
    println "la configuracion spring.cloud.vault.fail-fast debe estar en true para ejecutar el pipeline en el archivo bootstrap.properties"
    return false
  }
  return true
}

def variableRegistroApiMngmnt(Object registroApiMngmnt, Object regApiConnect){
  /*
  Metodo temporal para capturar cuales microservicios que van a api connect y deben tener el parametro en el job de jenkins
  A futuro deberia eliminarse la invocacion de este metodo
  */
  if (regApiConnect && registroApiMngmnt == "null"){
    slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'red', channel: "#jenkins-notifications", message: "Sin parametro registroApiMngmnt <${env.BUILD_URL}console|Detalle>", tokenCredentialId: 'slack-token-mobile'
  }
}

def pathsDeclaradosSwagger(){
  /*
  Metodo para validar swagger que ira a produccion para los BFF mientras aun convivan bluemix y azure en prod
  */
  println "Validando concordancia en paths declarados en swagger y en las fuentes"
  def apiJson = "server/common/swagger/Api.json"
  def apiYaml = "server/common/swagger/Api.yaml"
  def listaSwagger = []
  def archivo

  if(encontrarArchivoApi(apiJson)){
    archivo = readJSON file: apiJson
  } else if(encontrarArchivoApi(apiYaml)){
    archivo = readYaml file: apiYaml
  } else {
    error("No se encuentra archivo Api.json o Api.yaml en directorio server/common/swagger")
  }

  for (Object elemento:archivo.paths){
    listaSwagger << elemento.toString().split('=\\{')[0].replace("/{","/:").replace("}","")
  }

  println "Paths declarados en archivo swagger -> "+listaSwagger
  def lineas = sh (script: "grep -r \"'/\" server/api/ | grep -v -e controller -e \"\\.service\" -e example -e environment- -e \"url:\" -e services || true", returnStdout: true).trim().split("\n")
  for (String linea:lineas){
    if (!linea.equals("") && !linea.split(":")[1].startsWith("//")){
      def parseo = linea.split("'/")[1].split("',")
      def var = linea.split("/")[2]
      def path = cortarPath("/"+parseo[0])
      def pathCompleto = "/"+var+path

      if (!encontrarPath(listaSwagger, pathCompleto) && !encontrarPath(listaSwagger, path)){
        def ruta = sh (script: "grep -r -e "+var+" -e "+var.replace("-", "_")+" server/routes.js | grep -v import || true", returnStdout: true)
        if (ruta != null || ruta != ""){
          def valor = ruta.trim().split("\n")[0].split("/")
          for (String elem:valor){
            if (elem.contains(var)){
              pathCompleto = "/"+elem.replace("',", "")+path
              if (!encontrarPath(listaSwagger, pathCompleto)){
                def serverRoutes = sh (script: "grep -r \"/bff\" server/routes.js || true", returnStdout: true).trim().split("\n")
                for(String aux:serverRoutes){
                  try {
                    pathCompleto = "/"+aux.split("',")[0].split("/")[3]+path
                  } catch (err){
                    println "Path "+path+" ni path "+pathCompleto+" se encuentran declarados en archivo swagger, por favor corregir"
                    return false
                  }
                  if (!encontrarPath(listaSwagger, pathCompleto)){
                    println "Path "+path+" ni path "+pathCompleto+" se encuentran declarados en archivo swagger, por favor corregir"
                    return false
                  }
                }
              }
            }
          }
        } else {
          println "Path "+path+" ni path "+pathCompleto+" se encuentran declarados en archivo swagger, por favor corregir"
          return false
        }
      }
    }
  }
  return true
}

def encontrarArchivoApi(String pathApi){
    def api = findFiles(glob: pathApi)
    if(api.size() == 1){
        return true
    } else {
        return false
    }
}

def cortarPath(Object path){
  if (path.endsWith("/")){
    return path.take(path.size()-1)
  } else {
    return path
  }
}

def encontrarPath(Object listado, String path){
  return listado.any{path.toLowerCase().contains(it.toLowerCase())}
}

def swaggerDesdePod(Object common){
  println "Validacion de swagger obtenido del pod desplegado previamente"
  def rutaSpec, spec, port = "8080"
  if (env.PIPELINE_APP == "BFF"){
    rutaSpec = env.BASE_HREF+"/spec"
    port = "3000"
  } else if (env.REG_EUREKA){
    rutaSpec = env.BASE_HREF+"/v2/api-docs"
  } else{
    rutaSpec = "/v2/api-docs"
  }

  fileOperations([ fileDeleteOperation(includes:'**/spec.json')])
  node (common.obtenerNombreNodo(env.SPACE)){
    common.loginAzure()
    retry(3){
      spec = sh (script: ''' set +x
        podName=$(kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin | grep ${PROJECT_NAME} | awk '{print $1}' | head -1)
        kubectl -n ${NAMESPACE} exec ${podName} -- /usr/bin/curl -s http://localhost:'''+port+rutaSpec
      , returnStdout: true).trim()
    }
  }
  def specJson = readJSON text: spec
  writeJSON file: 'spec.json', json: specJson

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

def validarProyectoBci(){
  figlet 'Validacion Proyecto BCI'
  println 'Herramienta de validacion de equipo Integracion.'
  env.README = (env.PIPELINE_APP == 'MS') ? 'readme_pipeline_ms.txt' : 'readme_pipeline_ig.txt'
  sh '''
    set +x
    cd /opt/gradle/gradle-4.1/init.d/
    git archive --remote=git@bitbucket.org:bancocreditoeinversiones/validation-configuration-project.git HEAD ${README} | tar -x
    git archive --remote=git@bitbucket.org:bancocreditoeinversiones/validation-configuration-project-script.git HEAD bci-plugins.gradle | tar -x
    cat ${README}
  '''
  def task = sh (script: 'set +x; /opt/gradle/gradle-4.1/bin/gradle tasks |grep validateBciProject |awk "{print $1}" ', returnStdout: true).trim()
  if (task.contains('validateBciProject')){
    println 'Se inicia validacion de proyecto BCI para este componente.'
    sh '''
      set +x
      /opt/gradle/gradle-4.1/bin/gradle validateBciProject
    '''
    println 'Validacion de proyecto BCI completada satisfactoriamente.'
  }
  else {
    println 'Componente no contempla validacion de proyecto BCI. Se omite.'
  }
}

def valUrlInTsFiles(){
  sh '''
    du -a ./src/environments/ | grep -F  '.ts' | awk '{print $2}' | xargs cat | grep "http://\\|https://" > UrlTsFe
  '''
  def listado = readFile file:'UrlTsFe'
  def comment = 0
  def noComment = 0
  def contApiGtw = 0
  String[] lineas = listado.replaceAll(' ','').split("\n")
  for (String linea : lineas) {
    if(linea.startsWith("//")){
      comment++
    }else{
      noComment++
    }
    if(linea.contains("https://api.us.apiconnect.ibmcloud.com/bci-api-qa/api-bci/") || linea.contains("https://apigwdsr.bci.cl") || linea.contains("https://apigwcrt.bci.cl")){
      contApiGtw++
    }
  }

  println "comentarios --> " + comment
  println "sin comentar --> " + noComment
  println "URL que contienen apigtw --> " + contApiGtw

  if (contApiGtw > 0){
    String msj = slackMsj(comment, noComment, contApiGtw)
    notificarSlack("valUrlInTsFiles",msj )
  }
}

def slackMsj (int comment, int noComment, int contApiGtw){
  txt = " ¡¡WARNING: Hard code encontrado!!\n "+env.PROJECT_NAME+" "+env.VERSION_DESPLIEGUE+" "+env.WORKING_PIPELINE+"\n "+"<${env.BUILD_URL}console|Detalle build>\n"+"\n cantidad de url comentadas: "+comment+"\n cantidad de url No comentadas: "+noComment+"\n cantidad de url que contienen apigtw: "+contApiGtw
  return txt
}

def notificarSlack(String tipoNotificacion, Boolean stageOK = true,String msj ){
  def jenkins = env.JENKINS_URL.replace("http://", "").replace(":8080/", "")
  switch(tipoNotificacion) {
    case "valUrlInTsFiles":
      def color = (stageOK) ? 'good' : 'danger'
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#valurlintsfiles", message: msj, tokenCredentialId: 'slack-token-mobile'
      break
  }
}

// Valida que la rama release contenga el ultimo commit del master
def ramaActualizada(){
  commitMaster = sh(script: "git log -1 origin/master --oneline", returnStdout: true).trim()
  commitRelease = sh(script: "git log --oneline", returnStdout: true).trim()
  if (!commitRelease.contains(commitMaster)){
    return false
  }else{
    return true
  }
}

return this
