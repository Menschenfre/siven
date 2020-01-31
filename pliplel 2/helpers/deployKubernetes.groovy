def despliegueKubernetes(Object common, String sitioDespliegue = '') {
  def clusterName = common.obtenerNombreCluster(sitioDespliegue)
  if (env.VISIBILITY == "False"){
    env.privActivado="-priv"
  }
  def podAntiguo = obtenerPod(clusterName)
  env.CLUSTER_NAME = clusterName
  def deployment = sh (script: ''' set +x
    nombreArchivo="${DEPLOY_NAME}.yml"

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    /usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${nombreArchivo} --record || true
    ''', returnStdout: true).trim()

  if (deployment.contains("deployment.apps/"+env.DEPLOY_NAME) && deployment.contains("service/"+env.DEPLOY_NAME)){
    //Despliegue correcto
    print deployment
  } else if (!deployment.contains("deployment.apps/"+env.DEPLOY_NAME) && deployment.contains("service/"+env.DEPLOY_NAME)){
    //Falla en el deployment, lo mas probable por el tema del tz-data
    //Cortamos el archivo para que solo vaya con el deployment y no el service y generamos archivo nuevo
    def data = readFile env.DEPLOY_NAME+".yml"
    writeFile file: "deployment-"+env.DEPLOY_NAME+".yml", text: data.split("---")[1]
    //Ejecutamos el comando replace para el archivo deployment
    sh ''' set +x
    nombreArchivo="${DEPLOY_NAME}.yml"

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    /usr/local/bin/kubectl -n ${NAMESPACE} replace -f deployment-${nombreArchivo} --record
    '''
  } else {
    //Error de otro tipo, simplemente lo lanzamos como error
    error ("Error al intentar ejecutar el comando kuebctl apply: "+deployment)
  }

  println "Desplegando contenedor, por favor espere..."
  def podNuevo = obtenerPod(clusterName)
  if (env.SPACE == "PROD_LOCAL" && env.PIPELINE_APP in ["MS", "MOBILE"]){
    sleep 240
  } else {
    sleep 120
  }

  //Preguntamos primero si el despliegue va a Eureka o no
  if (validacionEstadoContainer(clusterName)){
    if(env.REG_EUREKA){
      //Cuando el despliegue no va a Eureka, hacemos validacion en base al healthcheck
      if (!validacionContainerHealthCheck(clusterName)){
        //Luego del tiempo maximo de validacion el healthcheck sigue marcando el pod como not ready
        //Se imprimen los eventos del describe del pod
        obtenerEventosPod(podNuevo, clusterName)
        error ("Contenedor desplegado incorrectamente")
      }
    } else {
      //Cuando el despliegue va a Eureka o no tiene relacion con eureka, validamos en base al estado del pod
      if (env.PIPELINE_APP in ["MS", "SI", "MOBILE", "SI-EJB"]){
        //En caso de que el despliegue sea un MS o IG que vaya a eureka, esperamos a que el pod antiguo
        //se borre del servidor de eureka y asi las consultas vayan sobre el nuevo despliegue
        registroEnEureka(podNuevo, podAntiguo, clusterName)
      }
    }
  } else {
    obtenerLogsYEstadoContainer(podNuevo, clusterName)
    error ("Contenedor desplegado incorrectamente")
  }
  
  println "Contenedor OK, FECHA DE DESPLIEGUE -> "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
}

def despliegueKubernetesCronJob(Object common, String sitioDespliegue = ''){
  def clusterName = common.obtenerNombreCluster(sitioDespliegue)
  env.CLUSTER_NAME = clusterName
  if (env.VISIBILITY == "False"){
    env.privActivado="-priv"
  }

  def deployment = sh (script: ''' set +x
    nombreArchivo="${DEPLOY_NAME}.yml"

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    /usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${nombreArchivo} --record || true
    ''', returnStdout: true).trim()
  
  if (deployment.contains("cronjob.batch/"+env.DEPLOY_NAME)){
    //Despliegue correcto
    print deployment
  }else{
    print deployment
    error ("Error en deploy de cronjob")
  }

  println "Despliegue OK, FECHA DE DESPLIEGUE -> "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
}

def eliminarCronJob(Object common, String sitioDespliegue = ''){
  def clusterName = common.obtenerNombreCluster(sitioDespliegue)
  env.CLUSTER_NAME = clusterName

  def deployment = sh (script: ''' set +x

    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

    /usr/local/bin/kubectl -n ${NAMESPACE} delete cronjob ${DEPLOY_NAME}  || true
    ''', returnStdout: true).trim()

  if (deployment.contains(env.DEPLOY_NAME) && deployment.contains("deleted")){
    //eliminado correcto
    println "CronJob :"+env.DEPLOY_NAME+" ELIMINADO."
    println "FECHA: "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
    println deployment
  }else{
    print deployment
    error ("Error al eliminar cronjob")
  }

}


def obtenerEventosPod(String pod, String clusterName){
  env.POD_NAME = pod
  env.CLUSTER_NAME = clusterName
  sh ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${NAMESPACE} describe pod ${POD_NAME} | grep -A200 Events
  '''
}

def registroEnEureka(String podNuevo, String podAntiguo, String clusterName){
  //Primero validamos que el pod nuevo se haya registrado en eureka
  println "Registrando en eureka"
  for (i = 0; i < 6; i++) {
    if(existePodEnEureka(podNuevo, podNuevo, clusterName)){
      break
    } else {
      sleep 30
    }
  }

  //Si existe un pod antiguo, nos aseguramos de que el pod antiguo
  //se elimine de eureka para poder hacer las pruebas funcionales sobre el nuevo pod
  if (!podAntiguo.equalsIgnoreCase("''")){
    for (j = 0; j < 4; j++) {
      if(!existePodEnEureka(podNuevo, podAntiguo, clusterName)){
        println "Registro en eureka completo"
        break
      } else {
        sleep 60
      }
    }
  } else {
    println "Registro en eureka completo"
  }
}

def existePodEnEureka(String podExec, String pod, String clusterName){
  env.POD_EXEC = podExec
  env.POD_NAME = pod
  env.CLUSTER_NAME = clusterName
  def podsEureka = sh (script: ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${NAMESPACE} exec ${POD_EXEC} -- /usr/bin/curl -s http://eureka1-server-service.bci-infra:8762/eureka/apps | grep instanceId || true
  ''',
  returnStdout: true).trim()
  if (podsEureka.equalsIgnoreCase("")){
    error ("Error en pod "+podExec)
  } else if(podsEureka.contains(pod)){
    return true
  } else {
    return false
  }
}

def validacionContainerHealthCheck(String clusterName){
  println "Validando healthcheck para el despliegue"
  def estadoHealthCheck = ''
  for (i = 0; i < 3; i++) {
    estadoHealthCheck = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].ready}'", clusterName)
    if (!estadoHealthCheck.contains("false")){
      println "Healthcheck Realizado"
      println "Estado ready del pod -> true"
      return true
    }
    sleep 120
  }
  println "Estado ready del pod -> false"
  return false
}

def obtenerLogsYEstadoContainer(String pod, String clusterName){
  env.POD_NAME = pod
  env.CLUSTER_NAME = clusterName
  sh ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${NAMESPACE} logs -p ${POD_NAME} -c ${DEPLOY_NAME}-node
    echo "--------------------------------------------------------------------------------------------------------------"
    /usr/local/bin/kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].state.waiting.reason}'
    echo ""
  '''
}

def validacionEstadoContainer(String clusterName){
  println "Validacion mediante eureka"
  def estadoRunning = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].state.running}'", clusterName)
  def estadoReinicio = obtenerValorJSONPath("jsonpath='{.items[*].status.containerStatuses[*].lastState.terminated.reason}'", clusterName)

  if (!estadoRunning.equalsIgnoreCase("") && estadoReinicio.equalsIgnoreCase("''")){
    //Pod esta en estado Running y sin reinicios
    return true
  } else {
    return false
  }
}

def obtenerValorJSONPath(String jsonpath, String clusterName){
  env.JSONPATH=jsonpath
  env.CLUSTER_NAME = clusterName
  def valor = sh (script: ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o ${JSONPATH}
  ''',
  returnStdout: true).trim()
  return valor
}

def obtenerPod(String clusterName){
  env.CLUSTER_NAME = clusterName
  def nombrePod = sh (script: ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
    if gitCommand=$(/usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}); then
      dummy="dummy"
    fi

    /usr/local/bin/kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} --sort-by=.metadata.creationTimestamp | awk '{print $1}' | tail -n1
  ''',
  returnStdout: true).trim()
  return nombrePod
}

return this;
