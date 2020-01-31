import groovy.json.JsonSlurper

def modulosFrontCertified(){
  def packageJson = readJSON file:'package.json'
  def nombre = packageJson.name.replace("@bci/", "")
  def version = packageJson.version
        
  env.idAsset = obtenerIdAsset(nombre, version)
  def status = sh (script: '''
    curl -s -u ${NEXUS_USR}:${NEXUS_PSW} -X DELETE --header 'Accept: application/json' http://repository.bci.cl:8081/service/rest/beta/assets/${idAsset} --max-time 120 --connect-timeout 120
  ''', returnStatus: true)
}

def obtenerIdAsset(Object nombre, Object version){
  env.NAME = nombre
  env.VERSION = version
  def infoAsset = sh (script: '''
    curl -s -u ${NEXUS_USR}:${NEXUS_PSW} -X GET --header 'Accept: application/json' "http://repository.bci.cl:8081/service/rest/beta/search/assets?repository=npm-bci-neg-releases-certified&name=${NAME}&version=${VERSION}"
  ''', returnStdout: true)
  def parser = new JsonSlurper()
  def json = parser.parseText(infoAsset)
  return json.items[0].id
}

def site(String sitioDespliegue, String revision, Object common) {
  println "Rollback en sitio "+sitioDespliegue
  env.REVISION=revision
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  sh ''' set +x
  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

  if [[ "${VISIBILITY}" == "False" ]]; then
    privActivado="-priv"
  fi

  ############################################# ROLLBACK #############################################
  RETRY=1

  until [[ $RETRY -ge "10" ]]; do
    rolloutHistory=$(/usr/local/bin/kubectl -n ${NAMESPACE} get replicaset --selector=app=${DEPLOY_NAME}-app${privActivado} -o json | python -c 'import json,sys;obj=json.load(sys.stdin);print len(obj["items"])')

    if [[ "${rolloutHistory}" == "0" ]]; then
      echo "Nothing to do here"
      exit 0
    elif [[ "${rolloutHistory}" == "1" ]] && [[ "${REVISION}" == "-1" ]]; then
      echo "No rollout history. Will delete"
      /usr/local/bin/kubectl -n ${NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc
      exit 0
    elif [[ "${rolloutHistory}" -ge "1" ]] && [[ "${REVISION}" -ge "0" ]]; then
      echo "Rolling back to revision ${REVISION}"
      /usr/local/bin/kubectl -n ${NAMESPACE} rollout undo deploy/${DEPLOY_NAME} --to-revision=${REVISION}
      exit 0
    else
      echo "Error. Rollout History = ${rolloutHistory}, Revision = ${REVISION}"
      exit -1
    fi

    let "RETRY++"
  done
  '''
}

def produccion(Object login, Object common, Object regApiConnect = false, Object productoApiConnect = ''){
  println "Rollback en paas local"
  login.validacionConexion(common, 'PROVIDENCIA')
  login.validacionConexion(common, 'SAN-BERNARDO')
  rollback(common)
  if (env.PIPELINE_APP in ["MS", "MOBILE", "BFF"] && validarHotfix()){
    eliminarRegistroIngress(common)
    eliminarRegistroIngressAzure(common)
    //Eliminacion de proxy en api management
    if (regApiConnect){
      def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');
      apiMngt.eliminarApiDeProducto(productoApiConnect, common.obtenerNombreNodo('ACONCAGUA'), "ACONCAGUA")
      apiMngt.eliminarApiDeProducto(productoApiConnect, common.obtenerNombreNodo('LONGOVILO'), "LONGOVILO")
    }
  }      
}

def validarHotfix(){
  def largo = env.BRANCH_NAME.split("-")
  def hotfix = largo[largo.size()-1]
  if (hotfix.equalsIgnoreCase("0")){
    return true
  } else {
    return false
  }
}

def rollback(Object common){
  if (env.PROJECT_NAME in ["fe-empresasbase", "fe-pcomercialbase"]){
    parallel (
      'Providencia': {
        eliminarDespliegue('PROVIDENCIA', common)
      },
      'San-Bernardo': {
        eliminarDespliegue('SAN-BERNARDO', common)
      }
    )

    parallel (
      'Aconcagua': {
        eliminarDespliegueAzure('ACONCAGUA', common)
      },
      'Longovilo': {
        eliminarDespliegueAzure('LONGOVILO', common)
      }
    )
  } else {
    rollbackDesdeYaml(common)
  }
}

def rollbackDesdeYaml(Object common){
  lock(resource:'archivosYaml'){
    dir('/opt/kubernetes/yamls/produccion/paas-local'){
      sh "git pull"
      String [] resFindCommits = sh (script: '''git log -- ${DEPLOY_NAME}.yaml | grep commit | awk \'{ print $2 }\'''', returnStdout: true).trim().split("\n")
      stash includes: env.DEPLOY_NAME+".yaml", name: 'rollback'
      if (resFindCommits.size() == 1){
        //Archivo es la primera version del aplicativo, se reversa el commit y se elimina despliegue
        println "Eliminamos despliegue en bluemix"
        parallel (
          'Providencia': {
            eliminarDespliegue('PROVIDENCIA', common)
          },
          'San-Bernardo': {
            eliminarDespliegue('SAN-BERNARDO', common)
          }
        )

        println "Eliminamos despliegue en azure"
        parallel (
          'Aconcagua': {
            eliminarDespliegueAzure('ACONCAGUA', common)
          },
          'Longovilo': {
            eliminarDespliegueAzure('LONGOVILO', common)
          }
        )
        sh "rm ${DEPLOY_NAME}.yaml"
        env.GIT_COMMAND = "git rm "+env.DEPLOY_NAME+".yaml"
      } else {
        //Archivo tiene varias versiones hotfix, se debe buscar el commit previo al primero del listado para hacer la reversa sobre ese
        def commit = resFindCommits[1]
        sh "git checkout $commit ${DEPLOY_NAME}.yaml"
        //Ejecutamos el kubectl para desplegar la version antigua del yaml
        println "Actualizamos despliegue en bluemix"
        parallel (
          'Providencia': {
            actualizarDespliegue('PROVIDENCIA', common)
          },
          'San-Bernardo': {
            actualizarDespliegue('SAN-BERNARDO', common)
          }
        )

        println "Actualizamos despliegue en azure"
        parallel (
          'Aconcagua': {
            actualizarDespliegueAzure('ACONCAGUA', common)
          },
          'Longovilo': {
            actualizarDespliegueAzure('LONGOVILO', common)
          }
        )
        env.GIT_COMMAND = "git add ."
      }
      try {
        sh '''
          ##Hacemos el git commit para que quede la version antigua en el repositorio yaml

          ${GIT_COMMAND}
          git commit -m "rollback ${DEPLOY_NAME}.yaml"
          git push -u origin master
        '''
      } catch (Exception err){
        println err
        currentBuild.result = 'SUCCESS'
      }
    }
  }
}

def actualizarDespliegue(String sitioDespliegue, Object common){
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  sh '''
  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${DEPLOY_NAME}.yaml --record
  sleep 120
  '''
}

def eliminarDespliegue(String sitioDespliegue, Object common){
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  sh '''
  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}
  /usr/local/bin/kubectl -n ${NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc
  '''
}

def eliminarRegistroIngress(Object common){
  lock(resource: 'archivosIngress') {
    def nombreArchivo = "ingress-"+env."${SPACE}_CLUSTER_NAME"+".yml"
    def nombreServicio = env.DEPLOY_NAME+"-svc"
    def rutaingress
    if (env.VISIBILITY == "False"){
      nombreServicio = nombreServicio+"-priv"
    }
    common.gitPullIngress()

    if (env.SPACE == "PROD_LOCAL"){
      rutaingress = '/opt/kubernetes/archivos-ingress-prod'
    }else{
      rutaingress = '/opt/kubernetes/archivos-ingress'
    }

    dir(rutaingress){
      if (nombreArchivo != "ingress-bci-front-prod01-pl.yml"){
        def data = readYaml file: nombreArchivo
        def list = data.spec.rules[0].http.paths.backend.serviceName
        def index = list.findIndexValues { it == nombreServicio }
        if (index.size()!=0){
          def dataMod = eliminarRegistro(data, index[0].toInteger())
          sh "rm $nombreArchivo"
          writeYaml file: nombreArchivo, data: dataMod

          env.ARCHIVO = nombreArchivo
          env.SERVICIO = nombreServicio

          parallel (
            'Providencia': {
              actualizarIngress('PROVIDENCIA', common)
            },
            'San-Bernardo': {
              actualizarIngress('SAN-BERNARDO', common)
            }
          )

          sh ''' set +x
          git add .
          git commit -m "rollback de ${SERVICIO} en ${ARCHIVO}"
          git push -u origin master
          '''
        }
      }
    }
  }
}

def actualizarIngress(String sitioDespliegue, Object common){
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  def rutaingress

  if (env.SPACE == "PROD_LOCAL"){
    rutaingress = '/opt/kubernetes/archivos-ingress-prod'
  }else{
    rutaingress = '/opt/kubernetes/archivos-ingress'
  }

  env.RUTA_INGRESS=rutaingress
  sh '''
  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

  yamllint ${RUTA_INGRESS}/${ARCHIVO}

  /usr/local/bin/kubectl -n ${NAMESPACE} apply -f ${ARCHIVO}
  '''
}

@NonCPS
def eliminarRegistro(Object data, Object index){
  data.spec.rules[0].http.paths.removeAt(index)
  return data
}

def azureSite(Object common, String nodo, Object revision){
  if (env.VISIBILITY == "False"){
    env.privActivado="-priv"
  }

  try {
    node (common.obtenerNombreNodo(nodo)){
      def rolloutHistory = sh (script: ''' set +x
        kubectl -n ${NAMESPACE} get replicaset --selector=app=${DEPLOY_NAME}-app${privActivado} -o json --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin | jq '.items | length' '''
      , returnStdout: true).trim().toInteger()
      
      if (rolloutHistory == 0){
        println "No existen replicas para este despliegue"
      } else if (rolloutHistory == 1){
        println "Despliegue no contiene rollout history, se elimina el despliegue y el service"
        sh '''kubectl -n ${NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin'''
      } else if (rolloutHistory >= 1 && revision >= 0){
        println "Rollback a la version "+revision
        sh '''kubectl -n ${NAMESPACE} rollout undo deploy/${DEPLOY_NAME} --to-revision='''+revision+''' --context='''+env."${SPACE}_AZURE_CLUSTER_NAME"+'''-admin'''
      } else {
        error ("Error al hacer rollback. Rollout History = "+rolloutHistory+", Revision = "+revision)
      }
    }
  }
  catch(err) {
    println "Error: "+err
    currentBuild.result = 'SUCCESS'
  }
}

def eliminarDespliegueAzure(String sitioDespliegue, Object common){
  node (common.obtenerNombreNodo(sitioDespliegue)){
    sh "kubectl -n ${NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
  }
}

def actualizarDespliegueAzure(String sitioDespliegue, Object common){
  node (common.obtenerNombreNodo(sitioDespliegue)){
    unstash 'rollback'
    sh "kubectl -n ${NAMESPACE} apply -f ${DEPLOY_NAME}.yaml --record --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
  }
  sleep 120
}

def eliminarRegistroIngressAzure(Object common){
  lock(resource: 'archivosIngress') {
    def nombreArchivo = "ingress-"+env."${SPACE}_AZURE_CLUSTER_NAME"+".yaml"
    def nombreServicio = env.DEPLOY_NAME+"-svc"
    def rutaingress
    if (env.VISIBILITY == "False"){
      nombreServicio = nombreServicio+"-priv"
    }
    common.gitPullIngress()

    if (env.SPACE == "PROD_LOCAL"){
      rutaingress = '/opt/kubernetes/archivos-ingress-prod'
    }else{
      rutaingress = '/opt/kubernetes/archivos-ingress'
    }

    dir(rutaingress){
      if (nombreArchivo != "ingress-bci-fro-prod001-pl.yml"){
        def data = readYaml file: nombreArchivo
        def list = data.spec.rules[0].http.paths.backend.serviceName
        def index = list.findIndexValues { it == nombreServicio }
        if (index.size()!=0){
          def dataMod = eliminarRegistro(data, index[0].toInteger())
          sh "rm $nombreArchivo"
          writeYaml file: nombreArchivo, data: dataMod
          sh "yamllint "+nombreArchivo
          stash includes: nombreArchivo, name: 'rollbackIngress'
          parallel (
            'Aconcagua': {
              actualizarIngressAzure('ACONCAGUA', common, nombreArchivo)
            },
            'Longovilo': {
              actualizarIngressAzure('LONGOVILO', common, nombreArchivo)
            }
          )

          sh ''' set +x
          git add .
          git commit -m "rollback de '''+nombreServicio+''' en '''+nombreArchivo+'''"
          git push -u origin master
          '''
        }
      }
    }
  }
}

def actualizarIngressAzure(String sitioDespliegue, Object common, Object nombreArchivo){
  node (common.obtenerNombreNodo(sitioDespliegue)){
    unstash "rollbackIngress"
    sh "kubectl -n ${NAMESPACE} apply -f "+nombreArchivo+" --context="+env."${SPACE}_AZURE_CLUSTER_NAME"+"-admin"
  }
}

return this;
