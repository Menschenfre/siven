def call() {
  def soloStage = "${soloStage}"
  env.PORT_NUMBER = '3000'
  env.PROJECT_NAME = "${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  env.BLUEMIX_HOME = '.bluemix'
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.VISIBILITY = "${params.visibilidad}"
  env.OC_VALUE = "${params.oc}"

  def common, login, template, rollback, docker, nexus
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    login = fileLoader.load('helpers/login');
    template = fileLoader.load('helpers/templates');
    rollback = fileLoader.load('helpers/rollbackKubernetes');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
  }

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  env.INT_NAMESPACE_VALUE='reg_int'
  env.INT_CLUSTER_NAME='bci-api-int01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-api'

  env.CERT_NAMESPACE_VALUE='reg_qa'
  env.CERT_CLUSTER_NAME='bci-api-cer01'
  env.CERT_ORGANIZATION='Bci API'
  env.CERT_TARGET_WORKSPACE='qa'
  env.CERT_NAMESPACE_DEPLOY_VALUE='bci-api'

  env.PROD_LOCAL_NAMESPACE_VALUE='reg_prod_local'
  env.PROD_LOCAL_CLUSTER_NAME='bci-api-prod01-pl'
  env.PROD_LOCAL_ORGANIZATION='Bci Produccion'
  env.PROD_LOCAL_TARGET_WORKSPACE='produccion'
  env.PROD_LOCAL_NAMESPACE_DEPLOY_VALUE='bci-api'

  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.modificacionPathBFF()
  common.modificarNpmrcNPMInstall()

  stage ('prepararImagenesNpm') {
    if ("${soloStage}" == 'prepararImagenesNpm') {
      def construirImagenPre = fileLoader.fromGit('bff/stages/construirImagenPre', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      construirImagenPre.call()
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      def pruebasUnitarias = fileLoader.fromGit('bff/stages/pruebasUnitarias', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pruebasUnitarias.call()
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
    }
  }

  stage ('deployIngressBFF') {
    if ("${soloStage}" == 'deployIngressBFF') {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }

  stage ('registroApiConnect') {
    if ("${soloStage}" == 'registroApiConnect') {
      def apiConnect = fileLoader.fromGit('helpers/api-connect', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')

      login.validacionConexion(common, '')
      apiConnect.obtenerSpecJson(login, common)
      apiConnect.registrar(env.PROJECT_NAME)
    }
  }

  stage ('gitDiff') {
    if ("${soloStage}" == 'gitDiff') {
      lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
    }
  }

  stage ('imagenProdNexus') {
    if ("${soloStage}" == 'imagenProdNexus') {
      common.imagenProdANexus(common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE), nexus, docker)
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
      common.notificarSlack("PAP", true)
    }
  }

  stage('despliegueIngressProduccion') {
    if ("${soloStage}" == 'despliegueIngressProduccion') {
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

  stage ('registroApiConnectPROD') {
    if ("${soloStage}" == 'registroApiConnectPAASLocal') {
      def apiConnect = fileLoader.fromGit('helpers/api-connect', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      try {
        login.validacionConexion(common, 'PROVIDENCIA')
        apiConnect.obtenerSpecJson(login, common)
        apiConnect.registrar(env.PROJECT_NAME)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ('Error al registrar en api connect')
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
