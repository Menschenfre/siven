def validacionConexion(Object common, String sitioDespliegue = ''){
  def configPath = "/var/lib/jenkins/kubectl-config"
  def configFile = common.obtenerNombreCluster(sitioDespliegue)
  sh "mkdir -p $configPath"

  println "Validando conexion correcta a cluster "+configFile
  if (!fileExists(configPath+'/'+configFile)){
    reconexion(sitioDespliegue)
  } else {
    env.CONFIG_FILE=configFile
    def conexionViva = sh (script: ''' set +x
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CONFIG_FILE}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CONFIG_FILE} use-context ${CONFIG_FILE}
    if gitCommand=$(/usr/local/bin/kubectl -n ${NAMESPACE} get ingress); then
      echo "Validacion correcta"
    fi
    ''', returnStdout: true).trim()
    if(conexionViva.contains('Validacion correcta')){
      println "Conexion correcta"
    } else {
      println "Conexion expirada"
      reconexion(sitioDespliegue)
    }
  }
}

def reconexion(String sitioDespliegue = ''){
  println "Actualizando conexion a bluemix"
  if (env.SPACE == 'PROD_LOCAL'){
    paasLocal(sitioDespliegue)
  } else {
    println "Login a bluemix pre-productivo esta deprecado"
  }
}

def paasLocal(String sitioDespliegue){
  env.PROVIDENCIA_CLUSTER_CONFIG='bcicp1cluster'
  env.PROVIDENCIA_ACCOUNT_HOST='https://10.248.7.51:8443'
  env.PROVIDENCIA_TARGET_ACCOUNT='id-bcicp1cluster-account'
  env.PROVIDENCIA_BLUEMIX_HOME='.bluemix_provi'

  env.SAN_BERNARDO_CLUSTER_CONFIG='bcicp2cluster'
  env.SAN_BERNARDO_ACCOUNT_HOST='https://10.248.7.151:8443'
  env.SAN_BERNARDO_TARGET_ACCOUNT='id-bcicp2cluster-account'
  env.SAN_BERNARDO_BLUEMIX_HOME='.bluemix_snbk'

  try {
    scriptConexion(sitioDespliegue)
  } catch (err) {
    println "Error en la conexion al paas local, esperamos un tiempo e intentamos nuevamente"
    sleep 30
    scriptConexion(sitioDespliegue)
  }
}

def scriptConexion(String sitioDespliegue){
  env.SITIO_DESPLIEGUE=sitioDespliegue.replace("-","_")
  sh ''' set +x
    ACCOUNT_HOST=${SITIO_DESPLIEGUE}_ACCOUNT_HOST
    TARGET_ACCOUNT=${SITIO_DESPLIEGUE}_TARGET_ACCOUNT
    CLUSTER_CONFIG=${SITIO_DESPLIEGUE}_CLUSTER_CONFIG
    BLUEMIX_HOME=${SITIO_DESPLIEGUE}_BLUEMIX_HOME

    export BLUEMIX_HOME=${!BLUEMIX_HOME}
    CONFIG_PATH="/var/lib/jenkins/kubectl-config"
    rm -r ${CONFIG_PATH}/${!CLUSTER_CONFIG}/ ${CONFIG_PATH}/${!CLUSTER_CONFIG}-context || true

    yes | /usr/local/bin/ibmcloud plugin install /var/lib/jenkins/icp-linux-amd64
    /usr/local/bin/ibmcloud pr login -a ${!ACCOUNT_HOST} --skip-ssl-validation -c ${!TARGET_ACCOUNT} -u ${PASSLOCAL_PROD_USER} -p ${PASSLOCAL_PROD_PASS}
    /usr/local/bin/ibmcloud pr cluster-config ${!CLUSTER_CONFIG}

    /usr/local/bin/kubectl config view --minify >> ${CONFIG_PATH}"/"${!CLUSTER_CONFIG}"-context"
    cp -R ${WORKSPACE}/${BLUEMIX_HOME}/.bluemix/plugins/icp/clusters/${!CLUSTER_CONFIG} ${CONFIG_PATH}

    /usr/local/bin/kubectl config unset clusters.${!CLUSTER_CONFIG}
    /usr/local/bin/kubectl config unset users.${!CLUSTER_CONFIG}-user
    /usr/local/bin/kubectl config unset contexts.${!CLUSTER_CONFIG}-context
    /usr/local/bin/kubectl config unset current-context
  '''
}

return this;
