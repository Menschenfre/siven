def site(String sitioDespliegue, String revision, Object common) {
  println "Rollback en sitio "+sitioDespliegue
  env.REVISION=revision
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  sh ''' set +x
  NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE

  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}

  if [[ "${VISIBILITY}" == "False" ]]; then
    privActivado="-priv"
  fi

  ############################################# ROLLBACK #############################################
  RETRY=1

  until [[ $RETRY -ge "10" ]]; do
    rolloutHistory=$(/usr/local/bin/kubectl -n ${!NAMESPACE} get replicaset --selector=app=${DEPLOY_NAME}-app${privActivado} -o json | python -c 'import json,sys;obj=json.load(sys.stdin);print len(obj["items"])')

    if [[ "${rolloutHistory}" == "0" ]]; then
      echo "Nothing to do here"
      exit 0
    elif [[ "${rolloutHistory}" == "1" ]] && [[ "${REVISION}" == "-1" ]]; then
      echo "No rollout history. Will delete"
      /usr/local/bin/kubectl -n ${!NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc
      exit 0
    elif [[ "${rolloutHistory}" -ge "1" ]] && [[ "${REVISION}" -ge "0" ]]; then
      echo "Rolling back to revision ${REVISION}"
      /usr/local/bin/kubectl -n ${!NAMESPACE} rollout undo deploy/${DEPLOY_NAME} --to-revision=${REVISION}
      exit 0
    else
      echo "Error. Rollout History = ${rolloutHistory}, Revision = ${REVISION}"
      exit -1
    fi

    let "RETRY++"
  done
  '''
}

def produccion(Object login, Object common){
  println "Rollback en paas local"
  parallel (
    'Providencia': {
      login.validacionConexion(common, 'PROVIDENCIA')
      eliminarDespliegue('PROVIDENCIA', common)
      if (env.PIPELINE_APP in ["MS", "MOBILE", "BFF", "BFF-MOBILE"]){
        eliminarRegistroIngress('PROVIDENCIA', common)
      }      
    },
    'San-Bernardo': {
      login.validacionConexion(common, 'SAN-BERNARDO')
      eliminarDespliegue('SAN-BERNARDO', common)
      if (env.PIPELINE_APP in ["MS", "MOBILE", "BFF", "BFF-MOBILE"]){
        eliminarRegistroIngress('SAN-BERNARDO', common)
      }
    }
  )
}

def eliminarDespliegue(String sitioDespliegue, Object common){
  env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
  sh '''
  NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE
  export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
  /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}
  /usr/local/bin/kubectl -n ${!NAMESPACE} delete deploy/${DEPLOY_NAME} service/${DEPLOY_NAME}-svc
  '''
}

def eliminarRegistroIngress(String sitioDespliegue, Object common){
  lock(resource: 'archivosIngress') {
    env.CLUSTER_NAME = common.obtenerNombreCluster(sitioDespliegue)
    def nombreArchivo = "ingress-"+env."${SPACE}_CLUSTER_NAME"+".yml"
    def nombreServicio = env.DEPLOY_NAME+"-svc"
    if (env.VISIBILITY == "False"){
      nombreServicio = nombreServicio+"-priv"
    }
    common.gitPullIngress()

    dir('/opt/kubernetes/archivos-ingress'){
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

          sh ''' set +x
          NAMESPACE=${SPACE}_NAMESPACE_DEPLOY_VALUE

          export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME}
          /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CLUSTER_NAME} use-context ${CLUSTER_NAME}
                              
          yamllint /opt/kubernetes/archivos-ingress/${ARCHIVO}
          
          if gitCommand=$(/usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${ARCHIVO}); then
            echo ${gitCommand}
          elif [[ "${gitCommand}" == *"Authentication required"* ]]; then
            sleep 30
            /usr/local/bin/kubectl -n ${!NAMESPACE} apply -f ${ARCHIVO}
          else
            echo ${gitCommand}
            exit -1
          fi

          set +e
          git add .
          git commit -m "rollback de ${SERVICIO} en ${ARCHIVO}"
          git push -u origin master
          set -e
          '''
        }
      }
    }
  }
}

@NonCPS
def eliminarRegistro(Object data, Object index){
  data.spec.rules[0].http.paths.removeAt(index)
  return data
}
return this;
