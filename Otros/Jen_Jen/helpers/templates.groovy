def generar(Boolean templateAzure = false){
  env.AZ_PREFIX = ""
  if(env.SPACE == "PROD_LOCAL"){
    env.REGISTRY = "repository.bci.cl:8089/reg_prod/"+env.OC_VALUE
  } else if(templateAzure){
    env.REGISTRY = registryAzure(env.SPACE)+"/"+env."${SPACE}_NAMESPACE_VALUE"
    env.AZ_PREFIX = "az-"
  } else {
    env.REGISTRY = "registry.ng.bluemix.net/"+env."${SPACE}_NAMESPACE_VALUE"
  }

  checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'jenkins-shared']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git']]])
  
  env.DESIRED_INSTANCE_COUNT = definirReplicas(templateAzure)
  obtenerValoresContainerParams()
  if (env.PIPELINE_APP in ["MS", "SI", "MOBILE", "BFF-MOBILE"]){
    microservicios()
  } else if (env.PIPELINE_APP in ["FRONTEND", "FRONTEND-AngularJS", "BFF"]){
    frontBff()
  } else {
    igEjb()
  }
}

def registryAzure(String space){
  def registry
  switch(space){
    case "INT":
    case "IC":
      registry = 'bcirg3dsrcnr001.azurecr.io'
    break
    case "CERT":
      registry = 'bcirg3crtcnr001.azurecr.io'
    break
    default:
      error('No space defined')
    break
  }
  return registry
}

def healthCheck(){
  def livenessPath = "/health"
  def livenessPort = "8080"
  def livenessInitialDelay = "180"
  def livenessPeriod = "10"
  def livenessTimeout = "3"
  def livenessFailureThreshold = "3"
  def livenessSuccessThreshold = "1"

  def readinessPath = "/health"
  def readinessPort = "8080"
  def readinessInitialDelay = "30"
  def readinessPeriod = "10"
  def readinessTimeout = "3"
  def readinessFailureThreshold = "3"
  def readinessSuccessThreshold = "1"

  def archivoJson = "${env.WORKSPACE}" + "/container_params.json"
  if (fileExists(archivoJson)){
    def containerParamsJson = readJSON file:'container_params.json'
    if (containerParamsJson.kubernetes.healthCheck.custom != null && containerParamsJson.kubernetes.healthCheck.custom){
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
    }
  } else{
    error "No existe archivo container_params.json, Por favor contactar a la célula responsable.\n Mas info :  https://bcibank.atlassian.net/wiki/spaces/PI/pages/882835579?atlOrigin=eyJpIjoiOWQyMTU5MmJhZDY1NDM5Zjk2ZTBjNWVkNWE3NTg4Y2MiLCJwIjoiYyJ9"
  }

  def liveness = "-v PATH_LIVENESS="+livenessPath+" -v PORT_LIVENESS="+livenessPort+" -v INITIAL_DELAY_LIVENESS="+
      livenessInitialDelay+" -v PERIOD_LIVENESS="+livenessPeriod+" -v TIMEOUT_LIVENESS="+livenessTimeout+" -v FAILURE_LIVENESS="+
      livenessFailureThreshold+" -v SUCCESS_LIVENESS="+livenessSuccessThreshold
  def readiness = "-v PATH_READINESS="+readinessPath+" -v PORT_READINESS="+readinessPort+" -v INITIAL_DELAY_READINESS="+
      readinessInitialDelay+" -v PERIOD_READINESS="+readinessPeriod+" -v TIMEOUT_READINESS="+readinessTimeout+" -v FAILURE_READINESS="+
      readinessFailureThreshold+" -v SUCCESS_READINESS="+readinessSuccessThreshold

  return liveness + " " + readiness + " -v HEALTHCHECK=true"
}

def definirReplicas(Boolean templateAzure){
  def replicas = 1
  if (env.SPACE == "PROD_LOCAL"){
    replicas = 2
  }
  if (templateAzure){
    replicas = 0
  }
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


def microservicios() {
  println "Creando "+env.AZ_PREFIX+env.DEPLOY_NAME+".yml"
  env.FLAG_EUREKA="-v FLAG_EUREKA=true"
  env.HEALTHCHECK = "-v HEALTHCHECK=false"
  if (env.REG_EUREKA){
    env.FLAG_EUREKA="-v FLAG_EUREKA=false"
    //if (env.PIPELINE_APP in ["MS", "MOBILE", "BFF-MOBILE"]){
    //  env.HEALTHCHECK = healthCheck()
    //}
  }
  sh ''' set +x
    NAMESPACE_VALUE=${SPACE}_NAMESPACE_VALUE
    NAMESPACE_DEPLOY_VALUE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    TARGET_WORKSPACE=${SPACE}_TARGET_WORKSPACE
    VAULT_TOKEN_VALUE=${SPACE}_VAULT_TOKEN

    FILENAME="deploy-service.yml.j2"
    if [[ "${PROJECT_NAME}" == "ms-documentosempresa-neg" ]]; then
      FILENAME="ms-documentosempresa-neg.yml.j2"
    fi

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE=${!NAMESPACE_VALUE} -v NAMESPACE_DEPLOY=${!NAMESPACE_DEPLOY_VALUE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} -v SPRING_PROFILE=${!TARGET_WORKSPACE} -v DATETIME="$(date)" -v CONTEXT_PATH="${BASE_HREF}"\
    -v JAVA_OPTS="${JAVAOPTS}" -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} ${FLAG_EUREKA} ${HEALTHCHECK} -v REGISTRY=${REGISTRY}\
    -v POD_SIZE="M" -i jenkins-shared/limits/${PIPELINE_APP}.json \
    jenkins-shared/templates/${FILENAME} > ${AZ_PREFIX}${DEPLOY_NAME}.yml

    if [[ ${PIPELINE_APP} == "BFF-MOBILE" ]] && [[ ${SPACE} == "PROD_LOCAL" ]]; then
      sed -i -e "s/100m/1024m/g" ${AZ_PREFIX}${DEPLOY_NAME}.yml
    fi
  '''
}

def frontBff() {
  println "Creando "+env.DEPLOY_NAME+".yml"
  sh ''' set +x
    NAMESPACE_VALUE=${SPACE}_NAMESPACE_VALUE
    NAMESPACE_DEPLOY_VALUE=${SPACE}_NAMESPACE_DEPLOY_VALUE

    if [[ "${SPACE}" == "PROD_LOCAL" ]]; then
      BSPACE="PROD"
    else
      BSPACE=${SPACE}
    fi

    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE=${!NAMESPACE_VALUE} -v NAMESPACE_DEPLOY=${!NAMESPACE_DEPLOY_VALUE} \
    -v PROJECT_NAME=${PROJECT_NAME} -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v BLUEMIX_SPACE=${BSPACE} \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} -v PORT_NUMBER=${PORT_NUMBER} -v DATETIME="$(date)" \
    -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} \
    -v POD_SIZE="M" -i jenkins-shared/limits/${PIPELINE_APP}.json \
    -v VISIBILITY=${VISIBILITY} jenkins-shared/templates/front-bff.yml.j2 > ${DEPLOY_NAME}.yml

  '''
}

def igEjb() {
  def paramsJson = readJSON file:'params.json'
  env.DESTINATIONS = paramsJson.DESTINATIONS
  env.TARGET_IPS = paramsJson.TARGET_IPS
  env.TARGET_PORT = paramsJson.TARGET_PORT

  println "Creando "+env.DEPLOY_NAME+".yml"
  sh ''' set +x
    NAMESPACE_VALUE=${SPACE}_NAMESPACE_VALUE
    TARGET_WORKSPACE=${SPACE}_TARGET_WORKSPACE
    VAULT_TOKEN_VALUE=${SPACE}_VAULT_TOKEN

    ## Reemplazamos el template con los valores que corresponden (NombreProyecto en Prod, nombreCorto en los demas)
    /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE=${!NAMESPACE_VALUE} -v PROJECT_NAME=${PROJECT_NAME} \
    -v JAVA_OPTS="${JAVAOPTS}" -v VERSION_DESPLIEGUE=${VERSION_DESPLIEGUE} -v VERSION_COMPONENTE=${VERSION_COMPONENTE} \
    -v BRANCH_NAME=${BRANCH_NAME} -v SPRING_PROFILE=${!TARGET_WORKSPACE} -v DATETIME="$(date)" -v CONTEXT_PATH="${BASE_HREF}"\
    -v OC_VALUE=${OC_VALUE} -v AFFECTED_CHANNEL=${LABELS_AFFECTEDCHANNEL} -v OWNER=${LABELS_OWNER} \
    -v PRODUCT=${LABELS_PRODUCT} -v TIERAPP=${LABELS_TIERAPP} \
    -v DESTINATIONS=${DESTINATIONS} -v TARGET_IPS=${TARGET_IPS} -v TARGET_PORT=${TARGET_PORT} \
    -v POD_SIZE="M" -i jenkins-shared/limits/${PIPELINE_APP}.json \
    -v REPLICAS=${DESIRED_INSTANCE_COUNT} jenkins-shared/templates/ig-ejb.yml.j2 > ${DEPLOY_NAME}.yml
  '''
}

return this;
