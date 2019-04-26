def call() {
  script {
    sh ''' set +x
    CLUSTER_NAME=${SPACE}_CLUSTER_NAME
    NAMESPACE_DEPLOY_VALUE=${SPACE}_NAMESPACE_DEPLOY_VALUE

    ## configuramos kubectl
    `/usr/local/bin/ibmcloud cs cluster-config ${!CLUSTER_NAME} --export | tail -n 1`

    ## Obtenemos variables definidas en container_params.txt
    if [[ -f container_params.txt ]]; then
      MIN_INSTANCE_COUNT=$(cat container_params.txt | grep "MIN_INSTANCE_COUNT_${SPACE}" | cut -d'"' -f 2)
      MAX_INSTANCE_COUNT=$(cat container_params.txt | grep "MAX_INSTANCE_COUNT_${SPACE}" | cut -d'"' -f 2)
      CPU_PERCENT=$(cat container_params.txt | grep "CPU_PERCENT_${SPACE}" | cut -d'"' -f 2)
    else
      echo "Archivo container_params.txt no encontrado"
      exit -1
    fi

    ## Reemplazamos el template con los valores que corresponden (NombreProyecto en Prod, nombreCorto en los demas)
    if [[ "${SPACE}" == "PROD" ]]; then
      /usr/local/bin/jj2 -v NAME=${PROJECT_NAME} -v NAMESPACE=${!NAMESPACE_DEPLOY_VALUE} -v REPLICA_MIN=${MIN_INSTANCE_COUNT} -v REPLICA_MAX=${MAX_INSTANCE_COUNT} -v CPU_TARGET=${CPU_PERCENT} jenkins-shared/templates/hpa.yaml.j2 > hpa.yaml
    else
      /usr/local/bin/jj2 -v NAME=${DEPLOY_NAME} -v NAMESPACE=${!NAMESPACE_DEPLOY_VALUE} -v REPLICA_MIN=${MIN_INSTANCE_COUNT} -v REPLICA_MAX=${MAX_INSTANCE_COUNT} -v CPU_TARGET=${CPU_PERCENT} jenkins-shared/templates/hpa.yaml.j2 > hpa.yaml
    fi

    ## Creamos el HorizontalPodAutoscaler
    /usr/local/bin/kubectl --namespace=${!NAMESPACE_DEPLOY_VALUE} apply -f hpa.yaml
    '''
  }
}

return this;
