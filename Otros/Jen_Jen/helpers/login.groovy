def validacionConexion(Object common, String sitioDespliegue = ''){
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

def azure(Object common, String sitioDespliegue){
  env.AZ_USER = env."${SPACE}_AZURE_USER"
  env.AZ_PASS = env."${SPACE}_AZURE_PASS"
  env.AZ_CLUSTER = env."${SPACE}_AZURE_CLUSTER_NAME"
  env.AZ_RESOURCE_GROUP = env."${SPACE}_RESOURCE_GROUP"

  println "Validando conexion correcta a cluster "+env.AZ_CLUSTER
  def conexionViva = sh (script: ''' set +x
    /var/lib/jenkins/bin/az account list
  ''', returnStdout: true).trim()
  if(conexionViva.contains('to access your accounts')){
    sh " set +x; /var/lib/jenkins/bin/az login -u ${AZ_USER} -p ${AZ_PASS}"
  } else {
    println "Conexion correcta"
  }
  sh ''' set +x
  /var/lib/jenkins/bin/az aks get-credentials -n ${AZ_CLUSTER} -g ${AZ_RESOURCE_GROUP} --overwrite-existing
  /usr/local/bin/kubectx ${AZ_CLUSTER}
  '''
}

def bluemix(Object common, String sitioDespliegue){
  def configPath = "/var/lib/jenkins/kubectl-config"
  def configFile = common.obtenerNombreCluster(sitioDespliegue)
  sh "mkdir -p $configPath"

  println "Validando conexion correcta a cluster "+configFile
  if (!fileExists(configPath+'/'+configFile)){
    reconexion(sitioDespliegue)
  } else {
    env.CONFIG_FILE=configFile
    def conexionViva = sh (script: ''' set +x
    NAMESPACE_DEPLOY_VALUE=${SPACE}_NAMESPACE_DEPLOY_VALUE
    export KUBECONFIG=/var/lib/jenkins/kubectl-config/${CONFIG_FILE}
    /usr/local/bin/kubectl config --kubeconfig=/var/lib/jenkins/kubectl-config/${CONFIG_FILE} use-context ${CONFIG_FILE}
    if gitCommand=$(/usr/local/bin/kubectl -n ${!NAMESPACE_DEPLOY_VALUE} cluster-info); then
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
    sh ''' set +x
      BLUEMIX_CREDENTIALS_USR=${SPACE}_BLUEMIX_CREDENTIALS_USR
      BLUEMIX_CREDENTIALS_PSW=${SPACE}_BLUEMIX_CREDENTIALS_PSW
      ORGANIZATION=${SPACE}_ORGANIZATION
      TARGET_WORKSPACE=${SPACE}_TARGET_WORKSPACE
      CLUSTER_NAME=${SPACE}_CLUSTER_NAME

      export BLUEMIX_HOME=${JENKINS_HOME}

      pathArchivos="/var/lib/jenkins/.bluemix/plugins/container-service/clusters/"${!CLUSTER_NAME}

      /usr/local/bin/ibmcloud login -a api.ng.bluemix.net -c ${BLUEMIX_CREDENTIALS_ID} -o "${!ORGANIZATION}" -s ${!TARGET_WORKSPACE} -u ${!BLUEMIX_CREDENTIALS_USR} -p ${!BLUEMIX_CREDENTIALS_PSW}

      yes | /usr/local/bin/ibmcloud plugin install "container-service"
      /usr/local/bin/ibmcloud cs init
      `/usr/local/bin/ibmcloud cs cluster-config ${!CLUSTER_NAME} | grep KUBECONFIG`

      yamlConexion=$(find ${pathArchivos} -name "*.yml")
      certificado=$(find ${pathArchivos} -name "*.pem")

      cp ${yamlConexion} /var/lib/jenkins/kubectl-config/${!CLUSTER_NAME}
      cp ${certificado} /var/lib/jenkins/kubectl-config/
    '''
  }
}

def bxRegistryLogin(){
  println "Actualizando conexion a bluemix private registry"
    sh ''' set +x
      BLUEMIX_CREDENTIALS_USR=${SPACE}_BLUEMIX_CREDENTIALS_USR
      BLUEMIX_CREDENTIALS_PSW=${SPACE}_BLUEMIX_CREDENTIALS_PSW
      ORGANIZATION=${SPACE}_ORGANIZATION
      TARGET_WORKSPACE=${SPACE}_TARGET_WORKSPACE
      CLUSTER_NAME=${SPACE}_CLUSTER_NAME

      export BLUEMIX_HOME=${JENKINS_HOME}

      /usr/local/bin/ibmcloud login -a api.ng.bluemix.net -c ${BLUEMIX_CREDENTIALS_ID} -o "${!ORGANIZATION}" -s ${!TARGET_WORKSPACE} -u ${!BLUEMIX_CREDENTIALS_USR} -p ${!BLUEMIX_CREDENTIALS_PSW}

      yes | /usr/local/bin/ibmcloud plugin install "container-registry"
      /usr/local/bin/ibmcloud cr login
    '''
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
