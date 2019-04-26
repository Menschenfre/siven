def call(Object common, String sitioDespliegue = '') {
if (env.SPACE == "PROD_LOCAL" || env.PIPELINE_APP in ["FRONTEND", "FRONTEND-AngularJS"]){
    bluemix(common, sitioDespliegue)
  } else {
    parallel (
      'BLUEMIX': {
        bluemix(common, sitioDespliegue)
      },
      'AZURE': {
        try {
          azure(common, sitioDespliegue)
        } catch (Exception err) {
          common.notificarSlack("tempAzure")
          currentBuild.result = 'SUCCESS'
        }
      }
    )
  }
}

def azure(Object common, String sitioDespliegue = '') {
    if (env.PIPELINE_APP in ["MS", "SI", "MOBILE"] && !env.REG_EUREKA){
      env.EUREKA = "True"
    }
    sh ''' set +x
    CLUSTER=${SPACE}_AZURE_CLUSTER_NAME
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    nombreArchivo="az-${DEPLOY_NAME}.yml"

    /usr/local/bin/kubectx ${!CLUSTER}

    if [[ "${VISIBILITY}" == "False" ]]; then
      privActivado="-priv"
    fi

    podAntiguo=$(/usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} | grep ${PROJECT_NAME} | awk '{print $1}' | head -1)

    sleep 10
    if gitCommand=$(/usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${nombreArchivo} --record); then
      echo ${gitCommand}
    elif [[ "${gitCommand}" == *"Authentication required"* ]]; then
      sleep 30
      /usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${nombreArchivo} --record
    else
      echo ${gitCommand}
    fi

    echo "Desplegando container, por favor espere..."
    #validamos estado de despliegue
    sleep 120

    podName=$(/usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} | grep ${PROJECT_NAME} | awk '{print $1}' | head -1)

    ## Valida estado del contenedor
      while true; do
        case $(/usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].ready}') in
          *false*)
            echo "Contenedor desplegado, pero fallo su ejecución, obteniendo los logs correspondientes..."
            /usr/local/bin/kubectl -n ${!NAMESPACE} logs ${podName}
            exit 1
            ;;
          *true*)
            echo "Contenedor en estado Running. Esperando un minuto para validaciones finales"
            sleep 60

            if [[ $(/usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].ready}') != *"false"* ]]; then
              if [[ ! -z ${EUREKA} ]]; then
                echo "Registrando en eureka"
                i=0
                while [[ $i -lt 6 ]]; do
                  set +e
                  regEurekaNuevo=$(/usr/local/bin/kubectl -n ${!NAMESPACE} exec ${podName} /usr/bin/curl http://eureka1-server-service.bci-infra:8762/eureka/apps | grep ${podName})
                  set -e
                  if [[ ! -z ${regEurekaNuevo} ]]; then
                    i=6
                  else
                    sleep 30
                  fi
                  i=$((i+1))
                done

                if [[ ! -z ${podAntiguo} ]]; then
                  j=0
                  while [[ $j -lt 5 ]]; do
                    set +e
                    regEurekaAntiguo=$(/usr/local/bin/kubectl -n ${!NAMESPACE} exec ${podName} /usr/bin/curl http://eureka1-server-service.bci-infra:8762/eureka/apps | grep ${podAntiguo})
                    set -e
                    if [[ -z ${regEurekaAntiguo} ]]; then
                      echo "Registro en eureka completo"
                      j=5
                    else
                      sleep 60
                    fi
                    j=$((j+1))
                  done
                else
                  echo "Registro en eureka completo"
                fi
              fi
              echo "Contenedor OK"
              echo "FECHA DE DESPLIEGUE -> $(date '+%Y-%m-%d %H:%M:%S')"
              exit 0
            else
              echo "Contenedor desplegado, pero fallo su ejecución, obteniendo los logs correspondientes..."
              /usr/local/bin/kubectl -n ${!NAMESPACE} logs ${podName}
              exit 1
            fi
            ;;
          *)
            echo "Contenedor no desplegado"
            /usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].state.waiting.reason}'
            exit 1
        esac
      done
    '''
}

def bluemix( Object common, String sitioDespliegue = '') {
    def clusterName = common.obtenerNombreCluster(sitioDespliegue)
    if (env.PIPELINE_APP in ["MS", "SI", "MOBILE"] && !env.REG_EUREKA){
      env.EUREKA = "True"
    }
    if (env.VISIBILITY == "False"){
      env.privActivado="-priv"
    }
    def podAntiguo = obtenerPod(clusterName)
    env.CLUSTER_NAME = clusterName
    sh ''' set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    nombreArchivo="${DEPLOY_NAME}.yml"

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    if gitCommand=$(/usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${nombreArchivo} --record); then
      echo ${gitCommand}
    elif [[ "${gitCommand}" == *"Authentication required"* ]]; then
      sleep 30
      /usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${nombreArchivo} --record
    else
      echo ${gitCommand}
    fi
    '''

    println "Desplegando contenedor, por favor espere..."
    sleep 120

    def podNuevo = obtenerPod(clusterName)

    //Validamos que el pod haya quedado en Running y sin ningun reinicio, para todos los pods que se desplieguen
    if (validacionEstadoContainer(clusterName)){
      //Container está en Running, ahora realizamos validacion dependiendo si va o no a eureka
      if(env.REG_EUREKA){
        //Aplicativo no va a eureka, utilizamos el healthcheck para validar por ultima vez el container desplegado
        if (!validacionContainerHealthCheck(clusterName)){
          error ("HEALTHCHECK para MS marco el contenedor como NOT READY")
        } else {
          println "Contenedor OK, FECHA DE DESPLIEGUE -> "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
        }
      } else {
        if (env.PIPELINE_APP in ["MS", "SI", "MOBILE", "SI-EJB"]){
          registroEnEureka(podNuevo, podAntiguo, clusterName)
        }
        println "Contenedor OK, FECHA DE DESPLIEGUE -> "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
      }
    } else {
      obtenerLogsYEstadoContainer(podNuevo, clusterName)
      error ("Contenedor desplegado incorrectamente")
    }
}

def registroEnEureka(String podNuevo, String podAntiguo, String clusterName){
  //Primero validamos que el pod nuevo se haya registrado en eureka
  println "Registrando en eureka"
  for (i = 0; i < 6; i++) {
    def valoresEureka = obtenerPodsEureka(podNuevo, podNuevo, clusterName)
    if(valoresEureka.contains(podNuevo)){
      i=6
    } else {
      sleep 30
    }
  }

  //Si existe un pod antiguo, nos aseguramos de que el pod antiguo 
  //se elimine de eureka para poder hacer las pruebas funcionales sobre el nuevo pod
  if (!podAntiguo.equalsIgnoreCase("''")){
    for (j = 0; j < 4; j++) {
      def valoresEureka = obtenerPodsEureka(podNuevo, podAntiguo, clusterName)
      if(!valoresEureka.contains(podAntiguo)){
        println "Registro en eureka completo"
        j=4
      } else {
        sleep 60
      }
    }
  } else {
    println "Registro en eureka completo"
  }
}

def obtenerPodsEureka(String podExec, String pod, String clusterName){
  env.POD_EXEC = podExec
  env.POD_NAME = pod
  env.CLUSTER_NAME = clusterName
  def podsEureka = sh (script: ''' set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${!NAMESPACE} exec ${POD_EXEC} /usr/bin/curl http://eureka1-server-service.bci-infra:8762/eureka/apps | grep ${POD_NAME} || true
  ''',
  returnStdout: true).trim()
  return podsEureka
}

def validacionContainerHealthCheck(String clusterName){
  if (env.PIPELINE_APP == "SI"){
    return true
  } else {
    def estadoHealthCheck = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].ready}'", clusterName)
    if (estadoHealthCheck.contains("false")){
      return false
    } else {
      return true
    }
  }
}

def obtenerLogsYEstadoContainer(String pod, String clusterName){
  env.POD_NAME = pod
  env.CLUSTER_NAME = clusterName
  println "Contenedor desplegado, pero fallo su ejecución, obteniendo los logs correspondientes..."
  sh ''' set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].state.waiting.reason}'
    /usr/local/bin/kubectl -n ${!NAMESPACE} logs ${POD_NAME}
  '''
}

def validacionEstadoContainer(String clusterName){
  def estadoRunning = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].state.running}'", clusterName)
  def estadoReinicio = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].lastState.terminated.reason}'", clusterName)

  if (!estadoRunning.equalsIgnoreCase("") && estadoReinicio.equalsIgnoreCase("''")){
    //Pod está en estado Running y sin reinicios
    return true
  } else {
    return false
  }
}

def obtenerValorJSONPath(String jsonpath, String clusterName){
  env.JSONPATH=jsonpath
  env.CLUSTER_NAME = clusterName
  def valor = sh (script: ''' set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o ${JSONPATH}
  ''',
  returnStdout: true).trim()
  return valor
}

def obtenerPod(String clusterName){
  env.CLUSTER_NAME = clusterName
  def nombrePod = sh (script: ''' set +x
    NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${!NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} | grep ${PROJECT_NAME} | awk '{print $1}' | head -1
  ''',
  returnStdout: true).trim()
  return nombrePod
}

return this;
