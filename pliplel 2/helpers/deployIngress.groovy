def despliegueIngress(Object common, String sitioDespliegue){
  lock(resource: 'archivosIngress') {
    common.gitPullIngress()
    env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
    def nombreArchivo = "ingress-"+env."${SPACE}_CLUSTER_NAME"+".yml"
    def nombreServicio = env.DEPLOY_NAME+"-svc"
    def host = ""
    def mensajeGit = "agregamos "
    def rutaingress
    def port = obtenerPuertoAplicativo()

    if (env.VISIBILITY == "False"){
      nombreServicio = nombreServicio+"-priv"
    }

    if (env.SPACE == "PROD_LOCAL"){
      rutaingress = '/opt/kubernetes/archivos-ingress-prod'
    } else{
      rutaingress = '/opt/kubernetes/archivos-ingress'
    }

    if (env.PROJECT_NAME in ["fe-pfmmisfinanzaswebpersonas", "fe-certificadosdigitales", "fe-clientestarjetadebito"] && env.SPACE != "PROD_LOCAL"){
      nombreArchivo = "ingress-"+env."${SPACE}_CLUSTER_NAME"+"-personas.yml"
    }
    
    env.RUTA_INGRESS=rutaingress

    dir(rutaingress){
      if (nombreArchivo != "ingress-bci-front-prod01-pl.yml"){
        def data = readYaml file: nombreArchivo
        def index = obtenerIndexHost(data.spec.rules.host)[0].toInteger()
        host = data.spec.rules[index].host
        def amap = ['path': env.BASE_HREF, 'backend': ['serviceName': nombreServicio, 'servicePort': port]]

        if (encontrarElementoLista(data.spec.rules[index].http.paths.backend.serviceName, nombreServicio)){
          println "Servicio ya se encuentra en yml"
          def indexService = data.spec.rules[index].http.paths.backend.serviceName.findIndexValues { it == nombreServicio }
          if (data.spec.rules[0].http.paths[indexService].path[0] != env.BASE_HREF && env.PIPELINE_APP in ["MS", "MOBILE"]){
            println "Actualizamos el path del servicio"
            def dataMod = edicionPath(data, indexService[0].toInteger(), amap)
            sh "rm $nombreArchivo"
            writeYaml file: nombreArchivo, data: dataMod
            mensajeGit = "editamos "
          }
        } else if (encontrarElementoLista(data.spec.rules[index].http.paths.path, env.BASE_HREF)){
          error ("Path ya fue desplegado previamente")
        } else {
          println "Servicio no existe en archivo ingress, por lo tanto se agrega"
          def dataMod = edicionArchivo(data, obtenerIndexHost(data.spec.rules.host)[0].toInteger(), amap)
          sh "rm $nombreArchivo"
          writeYaml file: nombreArchivo, data: dataMod
        }
      } else {
        println "Ingress de FRONT-END produccion no fue editado, fue obtenido desde repositorio"
      }

      env.MENSAJE_GIT = mensajeGit + nombreServicio + " en " + nombreArchivo
      env.ARCHIVO = nombreArchivo

      sh ''' set +x
        export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
        /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

        yamllint ${RUTA_INGRESS}/${ARCHIVO}

        if gitCommand=$(/usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${ARCHIVO}); then
          echo ${gitCommand}
        elif [[ "${gitCommand}" == *"Authentication required"* ]]; then
          sleep 30
          /usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${ARCHIVO}
        else
          echo ${gitCommand}
          exit -1
        fi

        gitCommand=$(git status)
        if [[ "${gitCommand}" == *"Changes not staged for commit:"* ]]; then
          git add .
          git commit -m "${MENSAJE_GIT}"
          git push -u origin master
        else
          echo "Repositorio ingress sin cambios"
        fi
      '''
    }
    mostrarURL(host)
  }
}

@NonCPS
def edicionPath(Object data, Object index, Object amap){
  data.spec.rules[0].http.paths.removeAt(index)
  data.spec.rules[0].http.paths.add(index, amap)
  return data
}

@NonCPS
def edicionArchivo(Object data, Integer index, Object amap){
  data.spec.rules[index].http.paths << amap
  return data
}

def encontrarElementoLista(Object list, String valor){
  def elemento = list.find { it == valor }
  if (valor.equalsIgnoreCase(elemento)){
    return true
  } else {
    return false
  }
}

def obtenerIndexHost(Object list, Boolean templateAzure = false){
  if (templateAzure){
    if (env.PROJECT_NAME in ["fe-pfmmisfinanzaswebpersonas", "fe-certificadosdigitales", "fe-clientestarjetadebito"]){
      return list.findIndexValues { it =~ /(personas.bci.cl)/ }
    } else {
      return list.findIndexValues { it =~ /(01.bci.cl)/ }
    }
  } else if (env.PROJECT_NAME in ["fe-gestionclavecliente", "fe-creacionclavecliente", "fe-cambioclavecliente", "fe-clavewebcliente"]){
    return list.findIndexValues { it =~ /(clave-internet.bci.cl)/ }
  } else if (env.SPACE == "PROD_LOCAL") {
    return list.findIndexValues { it == 'bciapi.bci.cl' }
  } else if (env.PROJECT_NAME in ["fe-pfmmisfinanzaswebpersonas", "fe-certificadosdigitales", "fe-clientestarjetadebito"]){
    return list.findIndexValues { it =~ /(personas.bci.cl)/ }
  } else {
    return list.findIndexValues { it =~ /(us-south.containers)/ }
  }
}

def mostrarURL(String host){
  if (!host.equalsIgnoreCase("")){
    def htmlExtra = "planes_monoproducto.html"
    if (!env.FE_ANGULARJS){
      htmlExtra = ""
    }
    println "URL -> http://"+host+env.BASE_HREF+htmlExtra
  }
}

def obtenerPuertoAplicativo(){
  switch(env.PIPELINE_APP){
    case "MS":
    case "MOBILE":
    case "SI":
    case "SI-EJB":
      return 8080
    case "BFF":
      return 3000
    case "FRONTEND":
    case "FRONTEND-AngularJS":
      return 80
    default:
      error('obtenerPuertoAplicativo() -> Error en puerto de aplicativo')
    break
  }
}

return this;
