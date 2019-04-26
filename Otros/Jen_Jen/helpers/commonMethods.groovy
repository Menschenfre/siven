def notificarSlack(String tipoNotificacion, Boolean stageOK = true){
  switch(tipoNotificacion) {
    case "respaldoYAML":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: ":tabla: Respaldar YAML <${env.BUILD_URL}console|Detalle>", tokenCredentialId: 'slack-token-mobile'
    break
    case "imagenesNexus":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: "Imagen ${env.PROJECT_NAME} ${env.VERSION_COMPONENTE} <${env.BUILD_URL}console|Detalle build>", tokenCredentialId: 'slack-token-mobile'
    break
    case "tempAzure":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: "AZURE <${env.BUILD_URL}console|Detalle build>", tokenCredentialId: 'slack-token-mobile'
    break
    case "PAP":
      def color = (stageOK) ? 'good' : 'danger'
      def containerParamsJson = readJSON file:'container_params.json'
      def producto = containerParamsJson.labels.product.replace(" ", "_")
      def mensaje = "${env.PROJECT_NAME} - OC: ${env.OC_VALUE} - Product: ${producto} - Stage: ${soloStage} <${env.BUILD_URL}console|Detalle>"
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#notificacion_pap", message: mensaje, tokenCredentialId: 'slack-token-mobile'
      slackSend baseUrl: 'https://everisbci.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#notificacion_pap", message: mensaje, tokenCredentialId: 'slack_everis'
    break
  }
}

def getApiVersion(def componente){
  return "v"+componente.split("re-v")[1].replace("-",".").take(3)
}

def obtenerParametro(String key){
  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  if (fileExists(archivoJson)){
    return obtenerParametroJSON(key)
  } else {
    if (key.equalsIgnoreCase("api.published")){
      key="API_CONNECT_PUBLISH"
    } else if (key.equalsIgnoreCase("api.product.name")){
      key="API_CONNECT_PRODUCT"
    }
    return obtenerParametroTXT(key)
  }
}
def obtenerValoresJSONPorPipeline(def regIngress, def regApiConnect, def apiConnectProduct, def pathbase){
  def respuestaValidacion = false
  switch (env.PIPELINE_APP){
    case "BFF":
      if (regIngress && regApiConnect){
        respuestaValidacion = true
      } else {
        println "Para BFF los valores son kubernetes.ingress: true y api.published: true"
      }
      break
    case "FRONTEND":
    case "FRONTEND-AngularJS":
      if (regIngress && !regApiConnect){
        respuestaValidacion = true
      } else {
        println "Para FE los valores son kubernetes.ingress: true y api.published: false"
      }
      break
    case "MS":
    case "MOBILE":
    case "BFF-MOBILE":
      if (!regIngress && regApiConnect){
        println "Para MS/API los valores son kubernetes.ingress: false y api.published: false"
      } else {
        if (apiConnectProduct == null || apiConnectProduct == ""){
          println "Para MS/API el valor api.product.name debe existir y tener un valor no vacío, por favor corregir"
        } else if (pathbase == null || pathbase == ""){
          println "Para MS/API el valor context.pathbase debe existir y tener un valor no vacío, por favor corregir"
        } else {
          respuestaValidacion = true
        }
      }
      break
    case "SI":
    case "SI-EJB":
      if (!regIngress && !regApiConnect){
        respuestaValidacion = true
      } else {
        println "Para IG los valores son kubernetes.ingress: false y api.published: false"
      }
      break
    default :
      println "PIPELINE_APP "+env.PIPELINE_APP+" incorrecto"
      break
  }
  return respuestaValidacion
}

def obtenerParametroTXT(String key){
  env.key=key
  def valor = ""
  valor = sh (script: ''' set +x
  cat container_params.txt | grep ${key} | cut -d'"' -f 2
  ''', returnStdout: true).trim()
  return valor

}

def obtenerParametroJSON(String key){
  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  def listaKeys = key.tokenize('.')
  def value = null

  if (!fileExists(archivoJson)){
    return value
  }
  def data = readJSON file: archivoJson

  for (i = 0; i <listaKeys.size(); i++) {
    def find=false
    if (data instanceof Map){
      data.each{item-> if (item.key==listaKeys[i]) {
                       find=true
                       value=item.value
                    }
               }
      if (find){
        data=value
      } else {
        return null
      }
    } else {
      return null
    }
  }
  return value
}

def validaValoresJSON(def regIngress, def regApiConnect, def podSize, def apiConnectProduct, def pathbase){
  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  if (fileExists(archivoJson)) {
    //Validando el formato del archivo json
    println "Validando formato correcto de archivo json"
    sh "jsonlint -q ${archivoJson}"
    //validamos que los labels existan y sean correctos en su formato
    if (!labelsValidos()){
      println "Error: Por favor verificar valores asignados a los labels"
      return false
    }
    if (env.PIPELINE_APP in ["MS", "MOBILE", "BFF-MOBILE", "SI", "SI-EJB"]){
      def javaopts = obtenerParametro("context.javaopts")
      if (javaopts == null || javaopts == ""){
        env.JAVAOPTS = "-Xms128m -Xmx128m"
      } else {
        env.JAVAOPTS = javaopts
      }
    }
    if (!env.REG_EUREKA.equals(null)){
      //validamos que los valores de ingress y apiconnect correspondan al tipo de aplicacion
      if (regIngress != null && regApiConnect != null ){
        return obtenerValoresJSONPorPipeline(regIngress, regApiConnect, apiConnectProduct, pathbase)
      } else {
        println "Parametros en JSON kubernetes.ingress y api.published no deben ser null, por favor agreguelos al archivo y vuelva a ejecutar"
        return false
      }

      println "kubernetes.size -> "+podSize
      //validamos que el podSize exista y que corresponda a uno de los valores predeterminados S M L XL
      if (podSize && (podSize in ["S", "M", "L", "XL"])){
        //metodo para obtener los valores del pod dependiendo de la aplicacion
      } else {
        println "En archivo JSON, valor para kubernetes.size debe existir y debe ser una de los siguientes valores S, M, L, XL"
        return false
      }
    }
  } else {
    //Cuando sea obligatorio el json, este debe corregirse por un unstable (return false)
    println "Archivo JSON no encontrado"
    env.JAVAOPTS = "-Xms128m -Xmx128m"
    return false
  }
  return true
}

def obtenerNombreCluster(String sitioDespliegue = ''){
  if (env.SPACE == 'PROD_LOCAL'){
    env.SITIO_DESPLIEGUE=sitioDespliegue.replace("-","_")
    env.PROVIDENCIA_CLUSTER_CONFIG='bcicp1cluster'
    env.SAN_BERNARDO_CLUSTER_CONFIG='bcicp2cluster'
    return env."${SITIO_DESPLIEGUE}_CLUSTER_CONFIG"+'-context'
  } else {
    return env."${SPACE}_CLUSTER_NAME"
  }
}

def gitPullIngress(){
  dir('/opt/kubernetes/archivos-ingress') {
    println "Ejecutamos un git pull sobre el repositorio para obtener cambios recientes"
    sh ''' set +x
      git clean -df
      git reset --hard HEAD
      if gitCommand=$(git pull --rebase); then
        echo ${gitCommand}
      else
        sleep 60
        git clean -df
        git reset --hard HEAD
        if gitCommand=$(git pull --rebase); then
          echo ${gitCommand}
        else
          echo "Problema al obtener el repositorio de archivos ingress, por favor intente nuevamente"
        fi
      fi
    '''
  }
}

def branchName() {
  println "Obteniendo nombre del branch"
  def branchName = sh (script: ''' set +x
    if [[ -z "${BRANCH_NAME}" ]]; then
      BRANCH_NAME=$(git branch 2>/dev/null | grep '^*' | colrm 1 2)
    fi
    echo ${BRANCH_NAME} | sed -e "s/_/-/g"
  ''', returnStdout: true).trim().toLowerCase()

    if(!branchNameValido(branchName)){
        error ("FAILURE")
    } else {
        return branchName
    }
}

def deployName(){
  println "Obteniendo el nombre del despliegue"
  def deployName = sh (script: ''' set +x
    length=55
    pattern="^[[:alnum:]].*[[:alnum:]]$"

    deployName="${PROJECT_NAME}-${VERSION_COMPONENTE}"

    while [[ $length -gt 10 ]]; do
      deployName=$(echo "${deployName:0:$length}")

      if [[ $deployName =~ $pattern ]]; then
        break
      fi
      length=$((length-1))
    done

    echo ${deployName}
  ''', returnStdout: true).trim().toLowerCase()
  return deployName
}

def modificacionPathBFF(){
  println "Modificando el path del BFF"
  def basePath = sh (script: ''' set +x
    tipoArchivo=$(find . -name Api.*)
    if [[ "${tipoArchivo}" == *"Api.json"* ]]; then
      jsonlint -q ./server/common/swagger/Api.json
      pathOriginal=$(cat ./server/common/swagger/Api.json | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["basePath"];')
    elif [[ "${tipoArchivo}" == *"Api.yaml"* ]]; then
      pathOriginal=$(cat ./server/common/swagger/Api.yaml | grep basePath | sed "s/basePath: //g")
    else
      echo "No se encuentra archivo Api.json o Api.yaml"
      exit -1
    fi
    pathOriginal="${pathOriginal//\\//\\/}"

    nuevoPath="${PROJECT_NAME}-${VERSION_COMPONENTE}"

    if [[ "${VISIBILITY}" == "False" ]]; then
      pathFinal="/bff/priv/${nuevoPath}"
      find ./ -type f -exec sed -i "s/${pathOriginal}/\\/bff\\/priv\\/${nuevoPath}/g" {} \\;
    else
      pathFinal="/bff/${nuevoPath}"
      find ./ -type f -exec sed -i "s/${pathOriginal}/\\/bff\\/${nuevoPath}/g" {} \\;
    fi

    echo ${pathFinal}
  ''', returnStdout: true).trim()
  return basePath
}

def agregarDesplieguePrivado(String visibility, String versionDespliegue){
  def versionDespliegueMod
  if (visibility != null && visibility.equalsIgnoreCase("False")){
    versionDespliegueMod = versionDespliegue + '-priv'
  } else {
    versionDespliegueMod = versionDespliegue
  }
  return versionDespliegueMod
}

def modificacionPathFRONTEND(){
  println "Modificando el path para FRONT-END"
  checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'jenkins-shared']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git']]])
  def nginxHeader = obtenerParametroJSON("context.nginxheader")
  def nginxXframe = obtenerParametroJSON("context.nginxheaderxframeoptions")
  def outputPath = "dist/"+env."${SPACE}_ENVIRONMENT_IMAGE"
  def nuevoPath = env.PROJECT_NAME+"-"+env.VERSION_COMPONENTE
  def pathFinal = ""

  if (env.VISIBILITY == "False"){
    pathFinal = "/nuevaWeb/priv/"+nuevoPath+"/"
  } else {
    pathFinal = "/nuevaWeb/"+nuevoPath+"/"
  }

  if (env.SPACE == "PROD_LOCAL"){
    pathFinal = "/nuevaWeb/"+env.PROJECT_NAME+"/"
  }

  if (nginxHeader){
    if (env.SPACE == "INT" && env.INT_CLUSTER_NAME == "bci-front-ic01"){
      sh "/usr/local/bin/jj2 -v BASEHREF=$pathFinal -v SPACE='IC' jenkins-shared/templates/nginx.conf-header.j2 > nginx.conf"
    } else {
      sh "/usr/local/bin/jj2 -v BASEHREF=$pathFinal -v SPACE=${SPACE} jenkins-shared/templates/nginx.conf-header.j2 > nginx.conf"
    }
  } else {
    sh "/usr/local/bin/jj2 -v BASEHREF=$pathFinal -v nginxXframe=$nginxXframe jenkins-shared/templates/nginx.conf.j2 > nginx.conf"
  }

  if (env.FE_ANGULARJS){
    sh "/usr/local/bin/jj2 -v PATH_FINAL=$pathFinal jenkins-shared/templates/dockerfile-feangularjs.j2 > webPublico/Dockerfile"
  } else {
    sh "/usr/local/bin/jj2 -v OUTPUT_PATH=$outputPath -v PATH_FINAL=$pathFinal jenkins-shared/templates/dockerfile-fe.j2 > Dockerfile"
  }

  return pathFinal
}

def obtenerValorTagDocker(String version, String branchName){
  if (version.equalsIgnoreCase("")){
    return branchName
  } else {
//    if (!"${params.versionComponente}".equalsIgnoreCase(branchName)){
//      error ("El branch configurado en el job de jenkins "+branchName+" no coindide con la version componente establecida en XL Release "+version)
//    }
    return version
  }
}

def getRevision(String site){
  println 'Obteniendo revision para manejo rollback'
  env.REG_INGRESS='true'
  env.CLUSTER_NAME = obtenerNombreCluster(site)
  def revision = sh (script: '''set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi
    codigo=$(kubectl -n ${!NAMESPACE} rollout history deploy/${DEPLOY_NAME} | tail -2 | head -1 | awk '{print $1}')

    if [[ "$codigo" == '' ]]; then
      echo '-1'
    else
      echo $codigo
    fi
  ''',
   returnStdout: true).trim()
  return revision
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

def modificarNpmrcNPMInstall(){
  println "Configurando archivo .npmrc de manera correcta"
  def content = "registry=http://repository.bci.cl:8081/repository/npm-group/\n"
  content += "//repository.bci.cl:8081/repository/npm-group/:_authToken=NpmToken.8e5cd9df-2260-35f3-b086-994eb74d540a\n"
  writeFile file: '.npmrc', text: content
}

def imagenProdANexus(String versionDespliegue, Object nexusStage, Object dockerStage){
  nexusStage.push2prod(env.PROJECT_NAME, versionDespliegue)
  println "Borrando imagenes docker locales"
  dockerStage.delete(env.PROJECT_NAME, versionDespliegue, "repository.bci.cl:8089/reg_prod/"+env.OC_VALUE)
  dockerStage.delete(env.PROJECT_NAME, versionDespliegue, "repository.bci.cl:8323/tmp_prodlocal")
  try {
    nexusStage.deleteTMPImages(env.PROJECT_NAME, env.VERSION_COMPONENTE)
    def login = fileLoader.fromGit('helpers/login','git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
    println "Borrando imagenes temporales del registry"
    login.bxRegistryLogin()
    deleteBluemixImages()
  } catch (Exception err) {
    notificarSlack("imagenesNexus")
  }
}

def deleteBluemixImages(){
  println "Eliminando imagenes registry qa bluemix"
  sh '''
    export BLUEMIX_HOME=${JENKINS_HOME}
    /usr/local/bin/ibmcloud cr login
    
    imagenes=$(/usr/local/bin/ibmcloud cr images | grep -w reg_qa/${PROJECT_NAME} | grep ${BRANCH_NAME} | grep -v ${VERSION_DESPLIEGUE} | awk '{print "/usr/local/bin/ibmcloud cr image-rm "$1":"$2}')
    while read line; do
      eval $line || true
    done <<< "$imagenes"
  '''
}

def respaldarYAML(Boolean templateAzure = false){
  try{
    println "Respaldando YAML desplegado"
    lock(resource:'archivosYaml'){
      switch(env.SPACE){
        case "INT":
          if(env.INT_CLUSTER_NAME == "bci-api-ic01" || env.INT_CLUSTER_NAME == "bci-front-ic01"){
            env.DIRECTORIO = "intcontinua"
          } else if(env.INT_CLUSTER_NAME == "bci-api-int01" || env.INT_CLUSTER_NAME == "bci-front-int01"){
            env.DIRECTORIO = "integracion"
          }
        break
       case "CERT":
          env.DIRECTORIO = "certificacion"
        break
        case "PROD_LOCAL":
          env.DIRECTORIO = "produccion/paas-local"
        break
        default:
          error 'Ambiente no corresponde con respaldo de YAMLs'
        break
      }

      if (templateAzure){
        env.AZ_PREFIX = "az-"
        if (env.DIRECTORIO == "intcontinua" || env.DIRECTORIO == "certificacion"){
          env.DIRECTORIO = env.DIRECTORIO + "Az"
        }
      }

      sh''' set +x
        rutaYamls="/opt/kubernetes/yamls"

        cd ${rutaYamls}
        git clean -df
        git reset --hard HEAD
        git pull --rebase

        cp ${WORKSPACE}/${AZ_PREFIX}${DEPLOY_NAME}.yml ${rutaYamls}/${DIRECTORIO}/${DEPLOY_NAME}.yaml

        git add .
        git commit -m "agregamos yaml ${DEPLOY_NAME}.yaml en ${DIRECTORIO}"
        git push -u origin master

        echo "Archivo YAML respaldado"
      '''
    }
  } catch (Exception err) {
    println err.getMessage()
    notificarSlack("respaldoYAML")
  }
}

def obtenerVersionBuildIG(){
  println "Obteniendo version del ig a desplegar"
  def versionBuild = sh (script: '''set +x
  sed -i -e "s/\\.\\./\\/var\\/lib\\/jenkins\\/scp/g" build.properties
  proyectoApi=$(find . -type d -iname *Api | sed -e "s/\\.\\///g")
  versionBuild=$(/opt/gradle/gradle-4.1/bin/gradle ${proyectoApi}:properties | grep version)
  echo ${versionBuild}
  ''', returnStdout: true).trim()
  return versionBuild
}

@NonCPS
def branchNameValido(String branchNameCandidato){
  def branchNamePattern = ~/^[a-zA-Z][a-zA-Z0-9_.-]+$/
  def match = ( "$branchNameCandidato" ==~ branchNamePattern )
  assert match instanceof Boolean
  if (match) {
    echo '[SUCCESS] branchName correcto'
    return true
  } else{
    echo '[FAILURE] branchName no valido'
    return false
  }

}

def labelsValidos(){
  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  if (fileExists(archivoJson)){
    def labelsList = ["labels.affectedChannel","labels.owner","labels.product","labels.tierApp"]
    for(String label: labelsList){
      def valor = obtenerParametroJSON(label)
      if(valor == "" || valor == null || valor.isNumber()){
        println "El label '"+label+"' no debe ser nulo, vacio o un numero"
        return false
      }
    }
  } else{
    println "No existe archivo container_params.json, Por favor contactar a la célula responsable.\n Mas info :  https://bcibank.atlassian.net/wiki/spaces/PI/pages/882835579?atlOrigin=eyJpIjoiOWQyMTU5MmJhZDY1NDM5Zjk2ZTBjNWVkNWE3NTg4Y2MiLCJwIjoiYyJ9"
    return false
  }
  return true
}

return this;
