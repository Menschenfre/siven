def call() {
    def urlEureka= sh (script: 'find . -name bootstrap.properties | head -1 | xargs cat | grep spring.application.name | sed -e "s/spring.application.name=//g"', returnStdout: true).trim()
    if (urlEureka.contains("_\$")) {
        urlEureka=urlEureka.substring(0, urlEureka.lastIndexOf("_\$"))+"_"+env.BRANCH_NAME
    }
    env.urlEureka = urlEureka
    script {
    sh ''' 
    CLUSTER_NAME=${SPACE}_CLUSTER_NAME
    NAMESPACE_DEPLOY_VALUE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    ZUUL_URI="http://zuul-server-service.bci-api:8080"
    EXTRA_URI="actuator-admin"
    
    finalUrl=${ZUUL_URI}/${urlEureka}/health

    #configuramos cluster-config para tener comando /usr/local/bin/kubectl
    set +e
    kubernetesPath=$(/usr/local/bin/ibmcloud cs cluster-config ${!CLUSTER_NAME} --export | tail -n 1)
    set -e

    if [[ "${kubernetesPath}" == *"Your IBM Cloud access tokens are expired"* ]]; then
      kubernetesPath=$(/usr/local/bin/ibmcloud cs cluster-config ${!CLUSTER_NAME} --export | tail -n 1)
    elif [[ "${kubernetesPath}" == *"You are not authorized to complete this action"* ]]; then
      /usr/local/bin/ibmcloud cs init
      kubernetesPath=$(/usr/local/bin/ibmcloud cs cluster-config ${!CLUSTER_NAME} --export | tail -n 1)
    fi

    export $kubernetesPath
    
    ##Validamos health de container
    echo "Evaluando health check del container desplegado"
    echo "${finalUrl}"
    
    podName=$(/usr/local/bin/kubectl --namespace=${!NAMESPACE_DEPLOY_VALUE} get pods --selector=app=${DEPLOY_NAME}-app | grep ${PROJECT_NAME} | awk '{print $1}' | head -1)
    resultCurl=$(/usr/local/bin/kubectl --namespace=${!NAMESPACE_DEPLOY_VALUE} exec ${podName} /usr/bin/curl ${finalUrl})
    
    ##Validamos primero si esta autorizada la URL
    set +e
    errorDetail=$(echo ${resultCurl} | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["error"];')
    set -e
    if [[ "${errorDetail}" == "unauthorized" ]]; then
      finalUrl=${ZUUL_URI}/${urlEureka}/${EXTRA_URI}/health
    fi
    
    resultCurl=$(/usr/local/bin/kubectl --namespace=${!NAMESPACE_DEPLOY_VALUE} exec ${podName} /usr/bin/curl ${finalUrl})
    statusHealthCheck=$(echo ${resultCurl} | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["status"];')
    if [[ "${statusHealthCheck}" != "UP" ]]; then
      /usr/local/bin/kubectl --namespace=${!NAMESPACE_DEPLOY_VALUE} logs ${podName}
      echo ""
      echo ""
      echo "${DEPLOY_NAME} no se registró en Eureka de manera correcta, revise los logs para corregir el problema"
      exit -1
    fi
    echo "${DEPLOY_NAME} registrado en Eureka, despliegue correcto | status: ${statusHealthCheck}"
    echo "Container Ok"
    echo "FECHA DE DESPLIEGUE -> $(date '+%Y-%m-%d %H:%M:%S')"
    exit 0

    '''
    }
}

return this;
