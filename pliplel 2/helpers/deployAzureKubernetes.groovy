def despliegueKubernetes() {
  env.privActivado = ""
  if (env.VISIBILITY == "False"){
    env.privActivado="-priv"
  }
  def nombreArchivo = "az-"+env.DEPLOY_NAME+".yml"
  def podAntiguo = obtenerPod()
  def deployment = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} apply -f '''+nombreArchivo+''' --record --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin|| true
    ''', returnStdout: true).trim()

  if (deployment.contains("deployment.apps/"+env.DEPLOY_NAME) && deployment.contains("service/"+env.DEPLOY_NAME)){
    //Despliegue correcto
    print deployment
  } else if (!deployment.contains("deployment.apps/"+env.DEPLOY_NAME) && deployment.contains("service/"+env.DEPLOY_NAME)){
    //Falla en el deployment, lo mas probable por el tema del tz-data
    //Cortamos el archivo para que solo vaya con el deployment y no el service y generamos archivo nuevo
    def data = readFile nombreArchivo
    writeFile file: "deployment-"+nombreArchivo, text: data.split("---")[1]
    //Ejecutamos el comando replace para el archivo deployment
    sh ''' set +x
    kubectl -n ${NAMESPACE} replace -f deployment-'''+nombreArchivo+''' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin
    '''
  } else {
    //Error de otro tipo, simplemente lo lanzamos como error
    error ("Error al intentar ejecutar el comando kubectl apply: "+deployment)
  }

  println "Desplegando contenedor, por favor espere..."
  if (env.SPACE == "PROD_LOCAL" && env.PIPELINE_APP in ["MS", "MOBILE"]){
    sleep 240
  } else {
    sleep 120
  }

  def podNuevo = obtenerPod()

  if (validacionEstadoContainer(podNuevo)){
    //Preguntamos primero si el despliegue va a Eureka o no
    if(env.REG_EUREKA){
      //Cuando el despliegue no va a Eureka, hacemos validacion en base al healthcheck
      if (!validacionContainerHealthCheck(podNuevo)){
        //Luego del tiempo maximo de validacion el healthcheck sigue marcando el pod como not ready
        //Se imprimen los eventos del describe del pod
        sh ''' set +x
          kubectl -n ${NAMESPACE} describe pod '''+podNuevo+''' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin | grep -A200 Events
        '''
        error ("Contenedor desplegado incorrectamente")
      }
    } else {
      //Cuando el despliegue va a Eureka o no tiene relacion con eureka, validamos en base al estado del pod
      if (env.PIPELINE_APP in ["MS", "SI", "MOBILE", "SI-EJB"]){
        //En caso de que el despliegue sea un MS o IG que vaya a eureka, esperamos a que el pod antiguo
        //se borre del servidor de eureka y asi las consultas vayan sobre el nuevo despliegue
        registroEnEureka(podNuevo, podAntiguo)
      }
    }
  } else {
    def estadoReinicio = obtenerValorJSONPath(podNuevo, ".status.containerStatuses[*].lastState.terminated.reason")
    def flagLog = "-p"
    if (estadoReinicio == ""){
        flagLog = ""
    }
    println "Contenedor desplegado, pero fallo su ejecucion, obteniendo los logs correspondientes..."
    sh ''' set +x
      kubectl -n ${NAMESPACE} logs '''+flagLog+''' '''+podNuevo+''' -c ${DEPLOY_NAME}-node --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin
      echo "--------------------------------------------------------------------------------------------------------------"
      kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} -o jsonpath='{.items[*].status.containerStatuses[*].state.waiting.reason}' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin
    '''
    error ("Contenedor desplegado incorrectamente")
  }

  println "Contenedor OK, FECHA DE DESPLIEGUE -> "+ new Date().format( 'yyyy-MM-dd HH:MM:ss' )
}

def despliegueKubernetesCronJob(){
  def nombreArchivo = "az-"+env.DEPLOY_NAME+".yml"
  def deployment = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} apply -f '''+nombreArchivo+''' --record --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin|| true
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

def eliminarCronJob(){
  println "se elimina cronJob: "+env.DEPLOY_NAME
  def deployment = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin delete cronjob ${DEPLOY_NAME} || true
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



def registroEnEureka(String podNuevo, String podAntiguo){
  //Primero validamos que el pod nuevo se haya registrado en eureka
  println "Registrando en eureka"
  for (i = 0; i < 6; i++) {
    if(existePodEnEureka(podNuevo, podNuevo)){
      break
    } else {
      sleep 30
    }
  }

  //Si existe un pod antiguo, nos aseguramos de que el pod antiguo
  //se elimine de eureka para poder hacer las pruebas funcionales sobre el nuevo pod
  if (!podAntiguo.equalsIgnoreCase("''")){
    for (j = 0; j < 4; j++) {
      if(!existePodEnEureka(podNuevo, podAntiguo)){
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

def existePodEnEureka(String podExec, String pod){
  def podsEureka = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} exec '''+podExec+''' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin -- /usr/bin/curl -s http://eureka1-server-service.bci-infra:8762/eureka/apps | grep instanceId || true
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

def validacionContainerHealthCheck(Object pod){
  println "Validando healthcheck para el despliegue"
  def estadoHealthCheck = ''
  for (i = 0; i < 3; i++) {
    estadoHealthCheck = obtenerValorJSONPath(pod, ".status.containerStatuses[*].ready")
    if (!estadoHealthCheck.contains("false")){
      println "Estado ready del pod -> true"
      return true
    }
    sleep 120
  }
  println "Estado ready del pod -> false"
  return false
}

def validacionEstadoContainer(Object pod){
  println "Validacion estado pod desplegado"
  def estadoRunning = obtenerValorJSONPath(pod, ".status.containerStatuses[*].state.running")
  def estadoReinicio = obtenerValorJSONPath(pod, ".status.containerStatuses[*].lastState.terminated.reason")
  def estadoWaiting = obtenerValorJSONPath(pod, ".status.containerStatuses[*].state.waiting")

  if (!estadoRunning.equalsIgnoreCase("") && estadoReinicio == "" && estadoWaiting == ""){
    //Pod está en estado Running y sin reinicios
    return true
  } else {
    return false
  }
}

def obtenerValorJSONPath(Object pod, String path){
  def getPods = "get pods --selector=app="+env.DEPLOY_NAME+"-app"+env.privActivado
  def jsonpath = "jsonpath='{.items[*]"+path+"}'"
  if (env.SPACE != "PROD_LOCAL"){
    getPods = "get pod "+pod
    jsonpath = "jsonpath='{"+path+"}'"
  }

  def valor = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} '''+getPods+''' -o '''+jsonpath+''' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin'''
  , returnStdout: true).trim()
  return valor
}

def obtenerPod(){
  def nombrePod = sh (script: ''' set +x
    kubectl -n ${NAMESPACE} get pods --selector=app=${DEPLOY_NAME}-app${privActivado} --sort-by=.metadata.creationTimestamp --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin | awk '{print $1}' | tail -n1
  ''',
  returnStdout: true).trim()
  return nombrePod
}

return this;
