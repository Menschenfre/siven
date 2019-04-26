def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.PORT_NUMBER = '8080'
  env.BLUEMIX_HOME = '.bluemix'
  env.OC_VALUE = "${params.oc}"

  def common, login, template, docker, nexus, rollback
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    login = fileLoader.load('helpers/login');
    template = fileLoader.load('helpers/templates');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    rollback = fileLoader.load('helpers/rollbackKubernetes');
  }
  if(!common.labelsValidos()){
    echo 'Error: Por favor verificar valores asignados a los Labels.'
    currentBuild.result = 'UNSTABLE'
    return
  }
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch
  
  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)
  
  env.DEPLOY_NAME = common.deployName()

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def apiConnectProduct = common.obtenerParametro("api.product.name")
  def pathbase = common.obtenerParametro("context.pathbase")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, apiConnectProduct, pathbase)){
    currentBuild.result = 'UNSTABLE'
    return
  }
  env.BASE_HREF=pathbase+"/"+common.getApiVersion(env.VERSION_COMPONENTE)

  env.INT_NAMESPACE_VALUE='reg_int'
  env.INT_CLUSTER_NAME='bci-api-int01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-integ'

  env.CERT_NAMESPACE_VALUE='reg_qa'
  env.CERT_CLUSTER_NAME='bci-api-cer01'
  env.CERT_ORGANIZATION='Bci API'
  env.CERT_TARGET_WORKSPACE='qa'
  env.CERT_NAMESPACE_DEPLOY_VALUE='bci-integ'

  env.PROD_LOCAL_NAMESPACE_VALUE='reg_prod_local'
  env.PROD_LOCAL_ORGANIZATION='Bci Produccion'
  env.PROD_LOCAL_TARGET_WORKSPACE='produccion'
  env.PROD_LOCAL_NAMESPACE_DEPLOY_VALUE='bci-integ'
  
  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"

  stage ('buildCodigo') {
    if ("${soloStage}" == 'buildCodigo') {
      def buildCodigo = fileLoader.fromGit('servicios-integracion-ejb/stages/buildCodigo', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      buildCodigo.call()
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA') {
      env.DIR_EXTRA=sh (script: 'find . -type d -iname *Neg | sed -e "s/\\.//g"', returnStdout: true).trim()
      def checkingQA = fileLoader.fromGit('helpers/checkingQA', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')

      checkingQA.call()
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def crearImagenDocker = fileLoader.fromGit('helpers/crearImagenDocker', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      crearImagenDocker.call(common, docker, nexus)
    }
  }

  stage ('deployKubernetes') {
    if ("${soloStage}" == 'deployKubernetes') {
      def deployKubernetes = fileLoader.fromGit('helpers/deployKubernetes', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      template.generar()
      template.generar(true)
      login.validacionConexion(common)
      deployKubernetes.call(common)
      common.respaldarYAML()
      common.respaldarYAML(true)
      if(regApiConnect){
        login.validacionConexion(common, '')
        apiConnect.obtenerSpecJson(login, common)
        apiConnect.registrar(apiConnectProduct)
      }
    }
  }
  
  stage ('deployIngressMS') {
    if ("${soloStage}" == 'deployKubernetes' && env.REG_EUREKA) {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }
  
  stage ('registrarApiConnect') {
    if ("${soloStage}" == 'registrarApiConnect' && regApiConnect) {
      login.validacionConexion(common, 'PROVIDENCIA')
      apiConnect.obtenerSpecJson(login, common)
      apiConnect.registrar(apiConnectProduct)
    }
  }

  stage ('gitDiff') {
    if ("${soloStage}" == 'gitDiff') {
      lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
    }
  }

  stage ('imagenProdNexus') {
    if ("${soloStage}" == 'imagenProdNexus') {
      common.imagenProdANexus(despliegue, nexus, docker)
    }
  }

  stage('despliegueSitesProduccion') {
    if ("${soloStage}" == 'despliegueSitesProduccion') {
      def deployment = fileLoader.fromGit('helpers/deployKubernetes', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      def revisionProvidencia, revisionSanBernardo
      template.generar()
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            revisionProvidencia = common.getRevision('PROVIDENCIA')
            deployment.call(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            revisionSanBernardo = common.getRevision('SAN-BERNARDO')
            deployment.call(common, 'SAN-BERNARDO')
          }
        )
        common.respaldarYAML()
      } catch (err) {
        println "Error: "+err
        rollback.site("PROVIDENCIA", revisionProvidencia, common)
        rollback.site("SAN-BERNARDO", revisionSanBernardo, common)
        common.notificarSlack("PAP", false)
        error ("Rollback realizado")
      }
      try {
        if(regApiConnect){
          login.validacionConexion(common, 'PROVIDENCIA')
          apiConnect.obtenerSpecJson(login, common)
          apiConnect.registrar(apiConnectProduct)
        }
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Error al registrar en api connect")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('despliegueIngressProduccion') {
    if ("${soloStage}" == 'despliegueSitesProduccion' && env.REG_EUREKA) {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      try {
      parallel (
        'Providencia': {
          login.validacionConexion(common, 'PROVIDENCIA')
          deployIngress.call('PROVIDENCIA', common)
        },
        'San-Bernardo': {
          login.validacionConexion(common, 'SAN-BERNARDO')
          deployIngress.call('SAN-BERNARDO', common)
        }
      )
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de Ingress en paralelo, por favor revisar de manera manual")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage ('rollbackKubernetes') {
    if ("${soloStage}" == 'rollbackKubernetes') {
      rollback.produccion(login, common)
    }
  }
}

return this;
