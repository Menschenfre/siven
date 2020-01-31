def generar(Boolean templateAzure = false){
  def regNamespace = obtenerNamespace(env.SPACE)
  env.AZ_PREFIX = ""
  env.REGISTRY = "registry.ng.bluemix.net/"+regNamespace

  if (templateAzure){
    env.REGISTRY = registryAzure(env.SPACE)+"/"+regNamespace
    env.AZ_PREFIX = "az-"
  }

  if(env.SPACE == "PROD_LOCAL"){
    env.REGISTRY = "repository.bci.cl:8089/reg_prod/"+env.OC_VALUE
  }
  
  env.DESIRED_INSTANCE_COUNT = definirReplicas(templateAzure)
  obtenerValoresContainerParams()
  sh "git --git-dir=/opt/kubernetes/properties_pipeline_jenkins/.git --work-tree=/opt/kubernetes/properties_pipeline_jenkins/ pull"
  if (env.PIPELINE_APP in ["MS", "MOBILE"]){
    microservicios()
  } else if (env.PIPELINE_APP in ["FRONTEND", "FRONTEND-AngularJS", "BFF"]){
    frontBff()
  } else if (env.PIPELINE_APP in ["SI"]){
    serviciosIntegracion()
  } else if (env.PIPELINE_APP in ["CRONJOB"]){
    cronJob()
  } else {
    igEjb()
  }
}

def healthCheck(){

  def livenessPath = "/health"
  def livenessPort = "8080"
  def livenessInitialDelay = "90"
  def livenessPeriod = "10"
  def livenessTimeout = "10"
  def livenessFailureThreshold = "21"
  def livenessSuccessThreshold = "1"

  def readinessPath = "/health"
  def readinessPort = "8080"
  def readinessInitialDelay = "90"
  def readinessPeriod = "10"
  def readinessTimeout = "3"
  def readinessFailureThreshold = "3"
  def readinessSuccessThreshold = "1"

  if (!env.SPACE.contains("PROD") && env.PIPELINE_APP == "SI"){
    println "Se inicializan los valores de readiness y liveness period por defecto para componentes IG."
    livenessPeriod = "180"
    readinessPeriod = "180"
  }

  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  if (fileExists(archivoJson)){
    def containerParamsJson = readJSON file:'container_params.json'
    if (containerParamsJson.kubernetes.healthCheck.custom != null && containerParamsJson.kubernetes.healthCheck.custom){
      println "Se setean valores de liveness y readiness segun container_params.json del componente."
      livenessPath = containerParamsJson.kubernetes.healthCheck.livenessProbe.http.path
      livenessPort = containerParamsJson.kubernetes.healthCheck.livenessProbe.http.port
      livenessInitialDelay = containerParamsJson.kubernetes.healthCheck.livenessProbe.initialDelaySeconds
      livenessPeriod = containerParamsJson.kubernetes.healthCheck.livenessProbe.periodSeconds
      livenessTimeout = containerParamsJson.kubernetes.healthCheck.livenessProbe.timeoutSeconds
      livenessFailureThreshold = containerParamsJson.kubernetes.healthCheck.livenessProbe.failureThreshold
      livenessSuccessThreshold = containerParamsJson.kubernetes.healthCheck.livenessProbe.successThreshold
      
      readinessPath = containerParamsJson.kubernetes.healthCheck.readinessProbe.http.path
      readinessPort = containerParamsJson.kubernetes.healthCheck.readinessProbe.http.port
      readinessInitialDelay = containerParamsJson.kubernetes.healthCheck.readinessProbe.initialDelaySeconds
      readinessPeriod = containerParamsJson.kubernetes.healthCheck.readinessProbe.periodSeconds
      readinessTimeout = containerParamsJson.kubernetes.healthCheck.readinessProbe.timeoutSeconds
      readinessFailureThreshold = containerParamsJson.kubernetes.healthCheck.readinessProbe.failureThreshold
      readinessSuccessThreshold = containerParamsJson.kubernetes.healthCheck.readinessProbe.successThreshold
    } else {
      println "Flag Custom no activo o nulo. Se mantienen los valores por defecto."
    }
  } else{
    error "No existe archivo container_params.json, Por favor contactar a la celula responsable.\n Mas info :  https://bcibank.atlassian.net/wiki/spaces/PI/pages/882835579?atlOrigin=eyJpIjoiOWQyMTU5MmJhZDY1NDM5Zjk2ZTBjNWVkNWE3NTg4Y2MiLCJwIjoiYyJ9"
  }

  def liveness = "-v PATH_LIVENESS="+livenessPath+" -v PORT_LIVENESS="+livenessPort+" -v INITIAL_DELAY_LIVENESS="+
      livenessInitialDelay+" -v PERIOD_LIVENESS="+livenessPeriod+" -v TIMEOUT_LIVENESS="+livenessTimeout+" -v FAILURE_LIVENESS="+
      livenessFailureThreshold+" -v SUCCESS_LIVENESS="+livenessSuccessThreshold
  def readiness = "-v PATH_READINESS="+readinessPath+" -v PORT_READINESS="+readinessPort+" -v INITIAL_DELAY_READINESS="+
      readinessInitialDelay+" -v PERIOD_READINESS="+readinessPeriod+" -v TIMEOUT_READINESS="+readinessTimeout+" -v FAILURE_READINESS="+
      readinessFailureThreshold+" -v SUCCESS_READINESS="+readinessSuccessThreshold

  if (env.PIPELINE_APP == "SI" && env.PROJECT_NAME != "ig-nexus-servicios"){
    println "Validando version de libreria bci-igcore y actuator para activar healthCheck"
    def igcore = validarVersion("cl.bci.integ.core:bci-igcore", "1.2.0")
    def actuator = validarVersion("actuator","1.0.0")
    if ((igcore) && (actuator)){
      return liveness + " " + readiness + " -v HEALTHCHECK=true"
    } else {
      error('No se encuentra(n) libreria(s) necesaria(s) para ejecucion de Healthcheck IG.')
      currentBuild.result = 'FAILURE'
    }
  } else {
    println "Validando version de librerias bci-mscore y lib-perfilacion-pcom para activar healthCheck"
    def mscore = validarVersion("cl.bci.mscore:bci-mscore", "2.0.3")
    if (mscore){
      def existeLibreria = sh (script: "/opt/gradle/gradle-4.1/bin/gradle dependencies --quiet | grep cl.bci.pcom.perfilacion:lib-perfilacion-pcom-core || true", returnStdout: true).trim()
      if (existeLibreria == ""){
        return liveness + " " + readiness + " -v HEALTHCHECK=true"
      } else {
        def pcom = validarVersion("cl.bci.pcom.perfilacion:lib-perfilacion-pcom-core", "2.0.1")
        if (pcom){
          return liveness + " " + readiness + " -v HEALTHCHECK=true"
        } else {
          return "-v HEALTHCHECK=false"
        }
      }
    } else {
      return "-v HEALTHCHECK=false"
    }
  }
}

def validarVersion(Object library, Object minVersion){
  env.LIB = library
  def version = ""
  if (env.PIPELINE_APP == "SI"){
    if (library.contains('igcore')){
      println 'Buscamos version de igcore en componente Api'
      env.APIFOLDER = sh (script: 'set +x;find . -iname *Api | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
      version = sh (script: ''' set +x
      /opt/gradle/gradle-4.1/bin/gradle :${APIFOLDER}:dependencies --quiet | grep ${LIB} | grep -v -e "->" | head -1 | awk '{split($0,a,":"); print a[3]}' | awk '{split($0,a," "); print a[1]}'
      ''', returnStdout: true).trim().replace("-SNAPSHOT", "")
      if (version == ""){
        println 'igcore no encontrado en componente Api. Buscamos version de igcore en componente Server'
        env.APIFOLDER = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
        version = sh (script: ''' set +x
        /opt/gradle/gradle-4.1/bin/gradle :${APIFOLDER}:dependencies --quiet | grep ${LIB} | grep -v -e "->" | head -1 | awk '{split($0,a,":"); print a[3]}' | awk '{split($0,a," "); print a[1]}'
        ''', returnStdout: true).trim().replace("-SNAPSHOT", "")
      }
    } else {
      println 'Buscamos version de actuator'
      env.APIFOLDER = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
      version = sh (script: ''' set +x
      /opt/gradle/gradle-4.1/bin/gradle :${APIFOLDER}:dependencies --quiet | grep ${LIB} | grep -v -e "->" | head -1 | awk '{split($0,a,":"); print a[3]}' | awk '{split($0,a," "); print a[1]}'
      ''', returnStdout: true).trim().replace("RELEASE", "") 
    }
  } else {
    version = sh (script: ''' set +x
    /opt/gradle/gradle-4.1/bin/gradle dependencies --quiet | grep ${LIB} | grep -v -e "->" | head -1 | awk '{split($0,a,":"); print a[3]}' | awk '{split($0,a," "); print a[1]}'
    ''', returnStdout: true).trim().replace("-SNAPSHOT", "")
  }
  if (version == null || version.equalsIgnoreCase("")){
    println "libreria "+library+" no encontrada"
    return false
  }
  
  def versionObtenida = version.replace(".", "").toInteger()
  def versionMinima = minVersion.replace(".", "").toInteger()
    
  if (versionObtenida >= versionMinima){
    println library + " -> "+version+" mayor o igual a "+minVersion
    return true
  } else {
    println library + " -> "+version+" menor a "+minVersion
    return false
  }
}

def definirReplicas(Boolean templateAzure){
  def replicas = 1
  if (env.SPACE == "PROD_LOCAL"){
    replicas = 2
  }
  //if (templateAzure){
  //  replicas = 0
  //}
  return replicas
}

def obtenerValoresContainerParams(){
    def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
    if (fileExists(archivoJson)){
        def containerParamsJson = readJSON file:'container_params.json'
        env.LABELS_AFFECTEDCHANNEL = containerParamsJson.labels.affectedChannel.replace(" ", "_")
        env.LABELS_OWNER = containerParamsJson.labels.owner.replace(" ", "_")
        env.LABELS_PRODUCT = containerParamsJson.labels.product.replace(" ", "_")
        env.LABELS_TIERAPP = containerParamsJson.labels.tierApp.replace(" ", "_")
    }
    else{
      error  "No existe archivo container_params.json, Por favor contactar a la celula responsable.\n Mas info :  https://bcibank.atlassian.net/wiki/spaces/PI/pages/882835579?atlOrigin=eyJpIjoiOWQyMTU5MmJhZDY1NDM5Zjk2ZTBjNWVkNWE3NTg4Y2MiLCJwIjoiYyJ9"
    }
}

def obtenerCommitComponente(){
  def commit = sh (script: ''' set +x; git log -n 1 | grep commit | cut -d " " -f 2 ''', returnStdout: true).trim()
  env.COMMIT = commit
  println "Commit del componente a desplegar: " + env.COMMIT
}


def serviciosIntegracion(){
  println 'Healthcheck Servicio de Integracion'
  obtenerCommitComponente()
  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"
  env.FLAG_EUREKA = "-v FLAG_EUREKA=false"
  env.HEALTHCHECK = "-v HEALTHCHECK=false"
  def regNamespace = obtenerNamespace(env.SPACE)
  def targetWorkspace = obtenerTargetWorkspace(env.SPACE)
  println 'Ejecutando Healthcheck ...'
  env.HEALTHCHECK = healthCheck()
  sh ''' set +x
    FILENAME="deploy-service.yml.j2"

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE='''+regNamespace+''' -v NAMESPACE_DEPLOY=${NAMESPACE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} -v SPRING_PROFILE='''+targetWorkspace+''' -v DATETIME="$(date)" -v CONTEXT_PATH="${BASE_HREF}"\
    -v JAVA_OPTS="${JAVAOPTS}" -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} ${FLAG_EUREKA} ${HEALTHCHECK} -v REGISTRY=${REGISTRY}\
    -v POD_SIZE="M" -v COMMIT=${COMMIT} -i /opt/kubernetes/properties_pipeline_jenkins/limits/${PIPELINE_APP}.json \
    /opt/kubernetes/properties_pipeline_jenkins/templates/${FILENAME} > ${AZ_PREFIX}${DEPLOY_NAME}.yml
  '''
}

def microservicios() {
  obtenerCommitComponente()
  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"
  env.FLAG_EUREKA = "-v FLAG_EUREKA=true"
  env.HEALTHCHECK = "-v HEALTHCHECK=false"
  if (env.REG_EUREKA){
    env.FLAG_EUREKA="-v FLAG_EUREKA=false"
    if (env.PIPELINE_APP in ["MS", "MOBILE"]){
      env.HEALTHCHECK = healthCheck()
    }
  }
  def regNamespace = obtenerNamespace(env.SPACE)
  def targetWorkspace = obtenerTargetWorkspace(env.SPACE)
  sh ''' set +x
    FILENAME="deploy-service.yml.j2"
    if [[ "${PROJECT_NAME}" == "ms-documentosempresa-neg" ]]; then
      FILENAME="ms-documentosempresa-neg.yml.j2"
    elif [[ "${PROJECT_NAME}" == "ms-pagosmasivos-neg" ]] || [[ "${PROJECT_NAME}" == "ms-nominasrecaudaciondebito-neg" ]]; then
      FILENAME="ms-pagosmasivos-neg.yml.j2"
    fi

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE='''+regNamespace+''' -v NAMESPACE_DEPLOY=${NAMESPACE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} -v SPRING_PROFILE='''+targetWorkspace+''' -v DATETIME="$(date)" -v CONTEXT_PATH="${BASE_HREF}"\
    -v JAVA_OPTS="${JAVAOPTS}" -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} ${FLAG_EUREKA} ${HEALTHCHECK} -v REGISTRY=${REGISTRY}\
    -v POD_SIZE="M" -v COMMIT=${COMMIT} -i /opt/kubernetes/properties_pipeline_jenkins/limits/${PIPELINE_APP}.json \
    /opt/kubernetes/properties_pipeline_jenkins/templates/${FILENAME} > ${AZ_PREFIX}${DEPLOY_NAME}.yml

  '''
}

def cronJob() {
  obtenerCommitComponente()
  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"

  def regNamespace = obtenerNamespace(env.SPACE)
  def targetWorkspace = obtenerTargetWorkspace(env.SPACE)

  sh ''' set +x
    FILENAME="deploy-cronjob.yml.j2"

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE='''+regNamespace+''' -v NAMESPACE_DEPLOY=${NAMESPACE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v SPRING_PROFILE='''+targetWorkspace+''' -v DATETIME="$(date)" -v SCHEDULE="${VAR_SCHEDULE}" -v JOBSHISTORYLIMIT=${VAR_JOBSHISTORYLIMIT} \
    -v JAVA_OPTS="${JAVAOPTS}" -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} -v REGISTRY=${REGISTRY} \
    -v POD_SIZE="M" -v COMMIT=${COMMIT} -i /opt/kubernetes/properties_pipeline_jenkins/limits/${PIPELINE_APP}.json \
    /opt/kubernetes/properties_pipeline_jenkins/templates/${FILENAME} > ${AZ_PREFIX}${DEPLOY_NAME}.yml

  '''
}

def frontBff() {
  obtenerCommitComponente()
  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"
  def regNamespace = obtenerNamespace(env.SPACE)
  def port = "80"
  if (env.PIPELINE_APP == "BFF"){
    port = "3000"
  }
  sh ''' set +x
    if [[ "${SPACE}" == "PROD_LOCAL" ]]; then
      BSPACE="PROD"
    else
      BSPACE=${SPACE}
    fi

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE='''+regNamespace+''' -v NAMESPACE_DEPLOY=${NAMESPACE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v BLUEMIX_SPACE=${BSPACE} \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} -v PORT_NUMBER='''+port+''' -v DATETIME="$(date)" \
    -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} -v REGISTRY=${REGISTRY}\
    -v POD_SIZE="M" -v COMMIT=${COMMIT} -i /opt/kubernetes/properties_pipeline_jenkins/limits/${PIPELINE_APP}.json \
    -v VISIBILITY=${VISIBILITY} /opt/kubernetes/properties_pipeline_jenkins/templates/front-bff.yml.j2 > ${AZ_PREFIX}${DEPLOY_NAME}.yml

  '''
}

def igEjb() {
  obtenerCommitComponente()
  def paramsJson = readJSON file:'params.json'
  env.DESTINATIONS = paramsJson.DESTINATIONS
  env.TARGET_IPS = paramsJson.TARGET_IPS
  env.TARGET_PORT = paramsJson.TARGET_PORT

  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"
  def regNamespace = obtenerNamespace(env.SPACE)
  def targetWorkspace = obtenerTargetWorkspace(env.SPACE)
  sh ''' set +x
    ## Reemplazamos el template con los valores que corresponden (NombreProyecto en Prod, nombreCorto en los demas)
    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE='''+regNamespace+''' -v PROJECT_NAME=${PROJECT_NAME} \
    -v JAVA_OPTS="${JAVAOPTS}" -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v BRANCH_NAME=${BRANCH_NAME} -v SPRING_PROFILE='''+targetWorkspace+''' -v DATETIME="$(date)" -v CONTEXT_PATH="${BASE_HREF}"\
    -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} -v REGISTRY=${REGISTRY}\
    -v DESTINATIONS=${DESTINATIONS} -v TARGET_IPS=${TARGET_IPS} -v TARGET_PORT=${TARGET_PORT} \
    -v POD_SIZE="M" -v COMMIT=${COMMIT} -i /opt/kubernetes/properties_pipeline_jenkins/limits/${PIPELINE_APP}.json \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} /opt/kubernetes/properties_pipeline_jenkins/templates/ig-ejb.yml.j2 > ${AZ_PREFIX}${DEPLOY_NAME}.yml
  '''
}

def obtenerTargetWorkspace(Object ambiente){
  switch(space){
    case "INT":
    case "IC":
      return 'integracion'
    case "CERT":
      return 'qa'
    case "PROD_LOCAL":
      return "produccion"
    default:
      error('obtenerNamespace() -> No space defined')
  }
}

def obtenerNamespace(Object ambiente){
  switch(space){
    case "INT":
    case "IC":
      return 'reg_ic'
    case "CERT":
      return 'reg_qa'
    case "PROD_LOCAL":
      return "reg_prod"
    default:
      error('obtenerNamespace() -> No space defined')
  }
}

def registryAzure(String space){
  switch(space){
    case "INT":
    case "IC":
      return 'bcirg3dsrcnr001.azurecr.io'
    case "CERT":
      return 'bcirg3crtcnr001.azurecr.io'
    case "PROD_LOCAL":
      return "repository.bci.cl:8089/reg_prod/"+env.OC_VALUE
    default:
      error('registryAzure() -> No space defined')
    break
  }
}

return this;
