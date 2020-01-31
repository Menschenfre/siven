def validarRegistroApiMngmnt(Object flag){
  if (flag == "True"){
    return true
  } else {
    return false
  }
}

def deployKubernetes(Object bluemix, Object azure, Object common, Object sitioDespliegue = ''){
  parallel (
    'BLUEMIX': {
      if(env.SPACE == "PROD_LOCAL"){
        try {
          bluemix.despliegueKubernetes(common, sitioDespliegue)
        }
        catch(Exception err) {
          println err
          error ("error en deployKubernetes")
        }
      }else{
        println "ambiente pre-productivo, despliegue solo en azure..."
      }    
    },
    'AZURE': {
      try {
        stash includes: 'az-'+env.DEPLOY_NAME+'.yml', name: 'deployYaml'
        node (common.obtenerNombreNodo(env.SPACE)){
          common.loginAzure()
          unstash 'deployYaml'
          if (env.PIPELINE_APP == "CRONJOB"){
            azure.despliegueKubernetesCronJob()
          }else {
            azure.despliegueKubernetes()
          }
        }
      } catch (Exception err) {
        common.notificarSlack("tempAzure")
        error ("error en deployKubernetes")
      }
    }
  )
}

def deployIngress(Object bluemix, Object azure, Object common, String sitioDespliegue = '') {
  if(env.SPACE == "PROD_LOCAL"){
    try {
      bluemix.despliegueIngress(common, sitioDespliegue)
    }
    catch(Exception err) {
      println err
      error ("Error en deployIngress")
    }
  }else{
    println "ambiente pre-productivo, deployIngress solo en azure..."
  }
  
  
  try {
    azure.despliegueIngress(common)
  } catch (Exception err) {
    println err
    common.notificarSlack("tempAzure")
    error ("Error en deployIngress")
  }
}

def obtenerNombreNodo(Object nodo){
  if (env.SPACE != "PROD_LOCAL"){
    if(env.PROJECT_NAME.startsWith("fe-")){
      nodo = nodo+"-FRONT"
    } else if(env.PROJECT_NAME.startsWith("ms-") || env.PROJECT_NAME.startsWith("job-") || env.PROJECT_NAME.startsWith("ig-") || env.PROJECT_NAME.startsWith("bff-") || env.PROJECT_NAME.startsWith("api-")){
      nodo = nodo+"-API"
    }
  }

  def nombreNodo
  switch(nodo){
    //IC AZURE
    case "INT-API":
      nombreNodo = 'Slave Azure Deploy DSR'
    break
    case "INT-FRONT":
      nombreNodo = 'Slave Azure Deploy DSR FRONT'
    //CERT AZURE
    break
    case "CERT-API":
      nombreNodo = 'Slave Azure Deploy QA'
    break
    case "CERT-FRONT":
      nombreNodo = 'Slave Azure Deploy QA FRONT'
    //PROD AZURE
    break
    case "PROD_LOCAL":
      nombreNodo = 'Slave Azure Deploy PROD DR'
    break
    case "ACONCAGUA":
      nombreNodo = 'Slave nuevo Azure Deploy PROD ACO'
    break
    case "LONGOVILO":
      nombreNodo = 'Slave nuevo Azure Deploy PROD LONGV'
    break
    default:
      error('Espacio '+nodo+' no definido en pipeline')
    break
  }

  if (Jenkins.instance.getNode(nombreNodo).toComputer().isOnline()){
    //Nodo esta online
    return nombreNodo
  } else {
    error("Nodo "+nombreNodo+" se encuentra offline, contactarse con el equipo devops")
  }
}

def loginAzure(){
  println "Validando conexion correcta a cluster "+env."${SPACE}_AZURE_CLUSTER_NAME"
  env.AZ_USER = env."${SPACE}_AZURE_USER"
  env.AZ_PASS = env."${SPACE}_AZURE_PASS"
  def conexionViva = sh (script: ''' set +x
    az account list
  ''', returnStdout: true).trim()
  def kubectlConn = sh (script: ''' set +x
    kubectl -n '''+env.NAMESPACE+''' get ingress --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin
  ''', returnStatus: true)
  if(conexionViva.equalsIgnoreCase('[]') || kubectlConn == 1 ){
    sh '''set +x
    az login -u ${AZ_USER} -p ${AZ_PASS}
    az aks get-credentials --admin -n '''+env."${SPACE}_AZURE_CLUSTER_NAME"+''' -g '''+env."${SPACE}_RESOURCE_GROUP"+''' --overwrite-existing
    '''
  } else {
    println "Conexion correcta"
  }
}

def notificarSlack(String tipoNotificacion, Boolean stageOK = true){
  def jenkins = env.JENKINS_URL.replace("http://", "").replace(":8080/", "")
  switch(tipoNotificacion) {
    case "mergeBranch":
      def color = (stageOK) ? 'good' : 'danger'
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#mergebranch", message: "${env.RESULTADO_MERGE} <${env.BUILD_URL}console|Detalle>", tokenCredentialId: 'slack-token-mobile'
      slackSend baseUrl: 'https://everisbci.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#mergebranch", message: "${env.RESULTADO_MERGE} <${env.BUILD_URL}console|Detalle>", tokenCredentialId: 'slack_everis'
    break
    case "respaldoYAML":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: ":tabla: Respaldar YAML ${env.DEPLOY_NAME} <${env.BUILD_URL}console|Detalle>", tokenCredentialId: 'slack-token-mobile'
    break
    case "imagenesNexus":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: "Imagen ${env.PROJECT_NAME} ${env.VERSION_COMPONENTE} <${env.BUILD_URL}console|Detalle build>", tokenCredentialId: 'slack-token-mobile'
    break
    case "tempAzure":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: "AZURE "+env.SPACE+" "+env.DEPLOY_NAME+" <${env.BUILD_URL}console|Detalle build>", tokenCredentialId: 'slack-token-mobile'
    break
    case "apigee":
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: 'danger', channel: "#jenkins-notifications", message: "APIGEE "+env.SPACE+" "+env.DEPLOY_NAME+" <${env.BUILD_URL}console|Detalle build>", tokenCredentialId: 'slack-token-mobile'
    break
    case "PAP":
    case "PAP_AZURE":
      def color = (stageOK) ? 'good' : 'danger'
      def containerParamsJson = readJSON file:'container_params.json'
      def producto = containerParamsJson.labels.product.replace(" ", "_")
      def mensaje = tipoNotificacion + ": ${env.DEPLOY_NAME} - OC: ${env.OC_VALUE} - Product: ${producto} - Stage: ${soloStage} <${env.BUILD_URL}console|Detalle>"
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#notificacion_pap", message: mensaje, tokenCredentialId: 'slack-token-mobile'
      slackSend baseUrl: 'https://everisbci.slack.com/services/hooks/jenkins-ci/', color: color, channel: "#notificacion_pap", message: mensaje, tokenCredentialId: 'slack_everis'
    break
  }
}

def getApiVersion(def componente){
    def componenteArray = componente.split("-")
    
    if(componenteArray[2].length()>1){
        return "v"+componente.split("re-v")[1].replace("-",".").take(4)
    }else{
        return "v"+componente.split("re-v")[1].replace("-",".").take(3)
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
    case "SI-EJB":
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
    case "CRONJOB":
      if (!regIngress && !regApiConnect){
        respuestaValidacion = true
      } else {
        println "Para IG y CRONJOB los valores son kubernetes.ingress: false y api.published: false"
      }
      break
    default :
      println "PIPELINE_APP "+env.PIPELINE_APP+" incorrecto"
      break
  }
  return respuestaValidacion
}

def obtenerParametro(String key){
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
    if (env.PIPELINE_APP in ["MS", "MOBILE", "SI", "SI-EJB", "CRONJOB"]){
      def javaopts = obtenerParametro("context.javaopts")
      if (javaopts == null || javaopts == ""){
        env.JAVAOPTS = "-Xms128m -Xmx128m"
      } else {
        env.JAVAOPTS = javaopts
      }
    }
    if (!env.REG_EUREKA.equals(null) || env.PIPELINE_APP == "BFF"){
      if (env.WORKING_PIPELINE.contains("-IntegracionContinua")){
        if (regIngress != null && regApiConnect != null){
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
  def rutaingress

  if (env.SPACE == "PROD_LOCAL"){
    rutaingress = '/opt/kubernetes/archivos-ingress-prod'
  }else{
    rutaingress = '/opt/kubernetes/archivos-ingress'
  }

  dir(rutaingress) {
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
  //Largo permitido en kubernetes para el nombre del deploy es de 55 caracteres, por lo tanto tenemos que cortarlo en caso de que exceda esa cantidad
  def deployName = env.PROJECT_NAME+"-"+env.VERSION_COMPONENTE
  deployName = deployName.take(55).toLowerCase()
  if (deployName.endsWith("-")){
    deployName = deployName.take(54)
  }
  return deployName
}

def obtenerPathBFF(){
  if (env.VISIBILITY == "False"){
    return "/bff/priv/"+env.DEPLOY_NAME
  } else {
    return "/bff/"+env.DEPLOY_NAME
  }
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

def obtenerPathFE(){
  def pathFinal = "/nuevaWeb/"+env.PROJECT_NAME+"-"+env.VERSION_COMPONENTE+"/"

  if (env.VISIBILITY == "False"){
    pathFinal = "/nuevaWeb/priv/"+env.PROJECT_NAME+"-"+env.VERSION_COMPONENTE+"/"
  }

  if (env.SPACE == "PROD_LOCAL"){
    pathFinal = "/nuevaWeb/"+env.PROJECT_NAME+"/"
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

def getRevisionAzureProd(){
  println 'Obteniendo revision para manejo rollback'
  def revision = sh (script: '''set +x
    kubectl -n '''+env.NAMESPACE+''' rollout history deploy/${DEPLOY_NAME} --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin | tail -2 | head -1 | awk '{print $1}'
  ''', returnStdout: true).trim()

  if (revision == ''){
    return -1
  } else {
    return revision.toInteger()
  }
}

def getRevision(String site){
  println 'Obteniendo revision para manejo rollback'
  env.CLUSTER_NAME = obtenerNombreCluster(site)
  def revision = sh (script: '''set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi
    codigo=$(kubectl -n ${NAMESPACE} rollout history deploy/${DEPLOY_NAME} | tail -2 | head -1 | awk '{print $1}')

    if [[ "$codigo" == '' ]]; then
      echo '-1'
    else
      echo $codigo
    fi
  ''',
   returnStdout: true).trim()
  return revision
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
    nexusStage.deleteTMPImages(env.PROJECT_NAME, branchName())
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
    println "***********Respaldando YAML desplegado***********"
    lock(resource:'archivosYaml'){
      def directorio = '', dirAz = ''
      def nombreArchivo = env.DEPLOY_NAME+".yml"
      switch(env.SPACE){
        case "INT":
          directorio = "intcontinua/"
          dirAz = "intcontinuaAz/"
        break
        case "CERT":
          directorio = "certificacion/"
          dirAz = "certificacionAz/"
        break
        case "PROD_LOCAL":
          directorio = "produccion/paas-local/"
        break
        default:
          error 'Ambiente no corresponde con respaldo de YAMLs'
        break
      }

      dir('/opt/kubernetes/yamls') {
        sh "git clean -df; git reset --hard HEAD; git pull --rebase"
        sh 'cp ' + env.WORKSPACE + '/' + nombreArchivo + ' ' + directorio + env.DEPLOY_NAME+".yaml"
        if (env.SPACE != "PROD_LOCAL"){
          sh 'cp ' + env.WORKSPACE + '/az-' + nombreArchivo + ' ' + dirAz + env.DEPLOY_NAME+".yaml"
        }
        sh''' set +x
          git add .
          git commit -m "agregamos yaml ${DEPLOY_NAME}.yaml en '''+directorio+'''"
          git push -u origin master
        '''
      }
      println "***********Archivo YAML respaldado***********"
    }
  } catch (Exception err) {
    "***********Archivo YAML no respaldado***********"
    println err.getMessage()
    notificarSlack("respaldoYAML")
  }
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
      def valor = obtenerParametro(label)
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

def getValueFromBootstrapProps(String key, String environment){
  def propsFile = sh (script: "set +x; find . -iname *bootstrap.properties |head -1", returnStdout: true).trim().split(/\.\//)[1]
  def dir = env.WORKSPACE + '/' + propsFile
  if (fileExists(dir)){
    def props = readProperties file:dir
    if (key.contains('spring.application.name')){
      def appName = props[key]
      def value = appName.split(/-\$/)[0] + "-${env.VERSION_COMPONENTE}-" + environment + ".yml"
      return value
    } else {
      def value = props[key]
      return value
    }
  } else {
    error('No existe o no se puede leer archivo bootstrap.properties.')
  }
}

def checkIfConfigFileExists(String configServerPath, String configFileName, String amb){
  def fullConfigServerPath = configServerPath + configFileName
  println 'Buscamos archivo : '+configFileName+' en Config server '+amb
  sh "set +x;cd ${configServerPath} && git clean -df && git reset --hard HEAD && git pull --rebase"
  if (fileExists(fullConfigServerPath)){
    println "Config File "+configFileName+" encontrado en Config server "+amb
    checkYamllint(configFileName, configServerPath)
    if (amb == 'QA'){
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: "good", channel: "#releases-config-files", tokenCredentialId: 'slack-token-mobile', message: "Config File "+configFileName+" encontrado en Config Server QA. <${env.BUILD_URL}console|Detalle build>"
    }
  } else {
    error("Config File "+configFileName+" NO encontrado en Config server "+amb+".\nEl nombre debe concordar con el establecido en bootstrap.properties del proyecto: "+configFileName)
    currentBuild.result = "FAILURE"
    if (amb == 'QA'){
      slackSend baseUrl: 'https://bciinnovacion.slack.com/services/hooks/jenkins-ci/', color: "danger", channel: "#releases-config-files", tokenCredentialId: 'slack-token-mobile', message: "Config File "+configFileName+" NO encontrado en Config Server QA. <${env.BUILD_URL}console|Detalle build>"
    }    
  }
}

def checkYamllint(configFile, configFilePath){
    try{
      println 'Verificando Yaml lint de config file ' + configFile
      sh "set +x; cd ${configFilePath} && /usr/bin/yamllint ${configFile}"
      println 'Config file ' + configFile + ' estructurado correctamente.'
    } catch(Exception err){
      error("Config file contiene errores en su estructura Yaml.")
      currentBuild.result = "FAILURE"  
    }
}

def compareYamlFiles(configFileQa, configFileProd){
  def ymlFileQa = readYaml file: configFileQa
  def ymlFileProd = readYaml file: configFileProd
  def foundError = false
  for ( Object lineaQA : ymlFileQa ){
    def foundKey = false
    for (Object lineaProd : ymlFileProd){
      if(lineaQA.getKey() == lineaProd.getKey()){
        foundKey = true
        def subKeysQA = lineaQA.getValue()
        def subKeysProd = lineaProd.getValue()
        def foundSubKey = false
        for (String subKeyQA :  subKeysQA){
          subKeyQA = subKeyQA.toString()
          def valQA = subKeyQA.split('=')
          for(String subKeyProd : subKeysProd){
            subKeyProd = subKeyProd.toString()
            def valProd = subKeyProd.split('=')
            if(valQA[0] == valProd[0]){
              foundSubKey = true
            }
          }
          if(!foundSubKey){
            println "[WARNING] La subKey '"+valQA[0]+"' perteneciente a la Key '"+lineaQA.getKey()+"' no fue encontrada en el config File '"+configFileProd.split("/")[-1]+"'."
            foundError = true
          }
        }
      }
    }
    if(!foundKey){
    println "[WARNING] La key '"+lineaQA.getKey()+"' no fue encontrada en el config File '"+configFileProd.split("/")[-1]+"'."
    foundError = true
    }
  }
  if (foundError){
    println ("== Archivo Yaml '"+configFileProd.split("/")[-1]+"' presenta diferencias con config file QA. Favor considerar en paso a Produccion. ==")
  } else {
    println ("== Archivo Yaml '"+configFileProd.split("/")[-1]+"' validado correctamente. ==")
  }
}

def listadoDespleigues(nombre, branch, workspace, ambiente){
  env.NOMBRE = nombre
  env.BRANCH = branch
  env.WORKSPACE = workspace
  env.AMBIENTE = ambiente
  env.IPJENKINS = (ambiente == "QA") ? '172.16.98.112' : '172.16.98.110'
  try{
    sh'''
      ssh jenkins@${IPJENKINS} << 'ENDSSH'
        cd /var/lib/jenkins/registrodespliegues
        git pull
      exit
    '''
    comando = sh(script: "ssh jenkins@${IPJENKINS} 'echo '${NOMBRE}:${BRANCH}:${WORKSPACE}' >> /var/lib/jenkins/registrodespliegues/listadoDespliegues${AMBIENTE}' ", returnStdout: true).trim()

    sh'''
      ssh jenkins@${IPJENKINS} << 'ENDSSH'
        cd /var/lib/jenkins/registrodespliegues
        git add .
        git commit -m "Push automatico Jenkins"
        git push origin master
      exit
    '''
  }catch(Exception ex){
    println "ErrorRegistroDespleigues: " + ex
  }
}

def mergeRama(def ramaMerge, def branchName){

 if (ramaMerge == "" || ramaMerge == "null" ){
  println "no se encuentra variable branchMerge, favor revisar job de jenkins"
  println "variable branchMerge = "+ ramaMerge
  error ('FAILURE')
 }else {
  println "variable branchMerge ok: "+ramaMerge
 }

 def tag = branchName.split("re-")[1]

 env.TAG = tag
 env.RAMA_MERGE = ramaMerge
 env.RUTA = "Carpeta_Merge"
 env.COMPONENTE = env.PROJECT_NAME.toLowerCase()
 env.GIT_PROYECT = "git@bitbucket.org:bancocreditoeinversiones/"+env.COMPONENTE+".git"
 env.RESULTADO_MERGE = "Merge a "+env.RAMA_MERGE+" para componente: "+env.COMPONENTE+"  desde branch release: "+env.BRANCH_NAME

     try {
      // Se crea carpeta_merge y se realiza git clone para proyecto
       sh ''' set +x
          mkdir -p $RUTA
          cd $RUTA
          if gitClone=$(git clone ${GIT_PROYECT}); then
            echo ${gitClone}
          else
            echo "error al realizar git clone"
            echo ${gitClone}
            exit 1
          fi
       '''
     }
     catch(Exception err) {
       println "error:"
       println err
       sh '''
          rm -rf $WORKSPACE/$RUTA
       '''
       error ('FAILURE')
     }
 
    try {
      println "Se realiza Merge de rama "+branchName+" a "+ramaMerge
      sh ''' set +x

          cd $RUTA/$COMPONENTE 

           #checkout y pull a rama master
          git checkout ${RAMA_MERGE}
          if gitCommandPullRamaMerge=$(git pull origin ${RAMA_MERGE}); then
            echo ${gitCommandPullRamaMerge}
          else
            echo ${gitCommandPullRamaMerge}
            echo "Error al realizar git pull de rama ${RAMA_MERGE}"
            exit 1
          fi
          
          #checkout y pull a rama release
          git checkout ${BRANCH_NAME}
          if gitCommandPullRelease=$(git pull origin ${BRANCH_NAME}); then
            echo ${gitCommandPullRelease}
          else
            echo ${gitCommandPullRelease}
            echo "Error al realizar git pull de rama ${BRANCH_NAME}"
            exit 1
          fi
          
          #en rama release, se hace actualizacion contra master.
          if gitCommandActualizaContraMaster=$(git merge -s ours ${RAMA_MERGE}); then
            echo ${gitCommandActualizaContraMaster}
            echo "rama actualizada contra master, se procede a realizar merge..."
          else
            echo "error al realizar actualizacion contra master desde release"
            exit 1
          fi
          
          #checkout nuevamente a la master para mezclar con la release
          git checkout ${RAMA_MERGE}
          
          #inicia proceso de mezcla, consulta si es develop o master

          if [[ "${RAMA_MERGE}" == "develop" ]]; then
            if gitCommandMergeDevelop=$(git merge origin/${BRANCH_NAME}); then
              echo ${gitCommandMergeDevelop}
              if gitCommandPushDevelop=$(git push origin ${RAMA_MERGE}); then
                echo ${gitCommandPushDevelop}
              else 
                echo "Error al subir los cambios, validar error:"
                echo ${gitCommandPushDevelop}
                exit 1
              fi
            else
              echo "Error al realizar merge a ${RAMA_MERGE}, se aborta merge..."
              git merge --abort
              echo "================================================================"
              echo "Se solicita validar y resolver Conflicto:"
              echo ${gitCommandMergeDevelop}
              echo "================================================================"        
              exit 1
            fi
          fi


          if [[ "${RAMA_MERGE}" == "master" ]]; then
            if gitCommandMergeMaster=$(git merge origin/${BRANCH_NAME}); then
                echo ${gitCommandMergeMaster}
                if gitCommandPushMaster=$(git push origin ${RAMA_MERGE}); then
                  echo ${gitCommandPushMaster}
                else 
                  echo "Error al subir los cambios, validar error:"
                  echo ${gitCommandPushMaster}
                  exit 1
                fi

                if gitCommandCrearTag=$(git tag ${TAG}); then 
                  echo ${gitCommandCrearTag}
                  if gitCommandPushTag=$(git push origin ${TAG}); then
                     echo ${gitCommandPushTag}
                     echo "se sube tag correctamente: ${TAG}"
                      #se comenta para marcha blanca antes de eliminar rama release
                      #if gitCommandBorrarBranch=$(git push origin --delete ${BRANCH_NAME}); then
                      #  echo "Se elimina correctamente la rama release en BitBucket"
                      #  echo ${gitCommandBorrarBranch}
                      #  git branch -D ${BRANCH_NAME}
                      #else
                      #  echo "Error al eliminar rama release en BitBucket:"
                      #  echo ${gitCommandBorrarBranch}
                      #  echo "¡¡¡Realizar borrado de rama release ${BRANCH_NAME} en BitBucket!!!"
                      #fi
                      #se comenta para marcha blanca antes de eliminar rama release
                  else
                    echo "Error al realizar push de tag"
                    echo ${gitCommandPushTag}
                    echo "Se borra tag creado..."
                    git tag -d ${TAG}
                    echo "merge realizado correctamnte pero no se creo tag. Crear tag en bitbucket"
                  fi
                else
                  echo ${gitCommandCrearTag}
                  echo "error al crear tag, merge correcto pero tag no fue creado. Crear tag en bitbucket"
                fi
            else
               echo "Error al realizar merge a ${RAMA_MERGE}, se aborta merge..."
               git merge --abort
               echo "================================================================"
               echo "Validar error:"
               echo ${gitCommandMergeMaster}
               echo "================================================================"
               exit 1
            fi
          fi

          #al finalizar sin errores, se elimina carpeta_merge
          rm -rf $WORKSPACE/$RUTA
      '''
      notificarSlack("mergeBranch", true)
    }
    catch(Exception err) {
       println err
       println "se elimina carpeta_merge..."
       sh '''
          rm -rf $WORKSPACE/$RUTA
       '''

       if ( env.RAMA_MERGE == "develop"){
          currentBuild.result = 'SUCCESS'
          env.RESULTADO_MERGE = env.RESULTADO_MERGE + ", Realizar merge manual a rama develop, notificar a celula correspondiente"
          notificarSlack("mergeBranch", false)
       } else if ( env.RAMA_MERGE == "master"){
          notificarSlack("mergeBranch", false)
          error ('FAILURE') 
       }
       //por cualquier otro error
       error ('FAILURE') 
    }
}

def validarFlagApiConnect(){
    def azure = obtenerParametro("kubernetes.azure")
    if (azure ){
      println "por variable azure=true, stage validado finaliza SUCCESS."
      currentBuild.result = 'SUCCESS'
    } else if (!azure) {
      println "por variable azure=false, se valida stage normalmente"
      currentBuild.result = 'FAILURE'
      error ("FAILURE")
    }
}

def validarFlagBluemix(){
  def varAzure = obtenerParametro("kubernetes.azure")
  def varBluemix = obtenerParametro("kubernetes.bluemix")

  println "variable azure: "+varAzure
  println "variable bluemix: "+varBluemix

  if(env.SPACE != "PROD_LOCAL" && varAzure && varBluemix == false){
    println "variable azure=true y bluemix=false"
    return false
  } else if(env.SPACE == "PROD_LOCAL" || varBluemix || varBluemix == null){
    println "variable bluemix=true o variable no declarada"
    return true
  } else{
    println "error al validar flag de bluemix, validar que existan las variables 'azure' o 'bluemix' en container_params.json"
    error ("FAILURE")
  }
}

return this;
