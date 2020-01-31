def despliegueIngress(Object common) {
  lock(resource: 'archivosIngress') {
    common.gitPullIngress()
    def nombreArchivo = "ingress-"+env."${SPACE}_AZURE_CLUSTER_NAME"+".yaml"
    def nombreServicio = env.DEPLOY_NAME+"-svc"
    def host = ""
    def mensajeGit = "agregamos "
    def rutaIngress
    def port = obtenerPuertoAplicativo()

    if (env.VISIBILITY == "False"){
      nombreServicio = nombreServicio+"-priv"
    }

    if (env.SPACE != "PROD_LOCAL"){
      if (env.PROJECT_NAME in ["fe-gestionclavecliente", "fe-creacionclavecliente", "fe-cambioclavecliente", "fe-clavewebcliente"]){
        nombreArchivo = "ingress-"+env."${SPACE}_AZURE_CLUSTER_NAME"+"-clave-internet.yaml"
      } else if (env.PROJECT_NAME in ["fe-certificadosdigitales"]){
        nombreArchivo = "ingress-"+env."${SPACE}_AZURE_CLUSTER_NAME"+"-personas.yaml"
      } else if (env.PROJECT_NAME == "ms-enrolamientowallet-orq"){
        nombreArchivo = "ingress-apicert.bci.cl.yml"
        env.BASE_HREF = "/"+env.DEPLOY_NAME+env.BASE_HREF
      }
    }

    if (env.SPACE == "PROD_LOCAL"){
      rutaIngress = '/opt/kubernetes/archivos-ingress-prod'
    } else {
      rutaIngress = '/opt/kubernetes/archivos-ingress'
    }

    dir(rutaIngress){
      if (nombreArchivo != "ingress-bci-fro-prod001.yaml"){
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

      mensajeGit = mensajeGit + nombreServicio + " en " + nombreArchivo

      sh "yamllint $nombreArchivo"

      stash includes: nombreArchivo, name: 'ingressModificado'
      if (env.SPACE == "PROD_LOCAL"){
        node (common.obtenerNombreNodo("ACONCAGUA")){
          unstash 'ingressModificado'
          sh "kubectl -n ${NAMESPACE} apply -f $nombreArchivo --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
        }

        node (common.obtenerNombreNodo("LONGOVILO")){
          unstash 'ingressModificado'
          sh "kubectl -n ${NAMESPACE} apply -f $nombreArchivo --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
        }
      } else {
        node (common.obtenerNombreNodo(env.SPACE)){
          common.loginAzure()
          unstash 'ingressModificado'
          sh "kubectl -n ${NAMESPACE} apply -f $nombreArchivo --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
        }
      }

      sh ''' set +x
        gitCommand=$(git status)
        if [[ "${gitCommand}" == *"Changes not staged for commit:"* ]]; then
          git add .
          git commit -m "'''+mensajeGit+'''"
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

def obtenerIndexHost(Object list){
  if (env.SPACE == "PROD_LOCAL"){
    return list.findIndexValues { it =~ /(apibackprod.bci.cl)/ }
  } else if (env.PROJECT_NAME in ["fe-certificadosdigitales"]){
    return list.findIndexValues { it =~ /(personas.bci.cl)/ }
  } else if (env.PROJECT_NAME in ["fe-gestionclavecliente", "fe-creacionclavecliente", "fe-cambioclavecliente", "fe-clavewebcliente"]){
    return list.findIndexValues { it =~ /(clave-internet.api-az-test.com)/ }
  } else if (env.PROJECT_NAME == "ms-enrolamientowallet-orq"){
    return list.findIndexValues { it =~ /(apicert.bci.cl)/ }
  } else {
    return list.findIndexValues { it =~ /(01.bci.cl)/ }
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
