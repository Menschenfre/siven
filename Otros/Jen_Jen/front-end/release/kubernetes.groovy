def call() {
  def soloStage = "${soloStage}"
  env.PORT_NUMBER = '80'
  env.PROJECT_NAME="${params.projectName.toLowerCase()}"
  env.BLUEMIX_HOME = '.bluemix'
  env.SPACE = "${params.nombreAmbiente}"
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
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  env.DEPLOY_NAME = common.deployName()

  env.INT_NAMESPACE_VALUE='reg_int'
  env.INT_CLUSTER_NAME='bci-front-int01'
  env.INT_ORGANIZATION='Bci API'
  env.INT_TARGET_WORKSPACE='integracion'
  env.INT_NAMESPACE_DEPLOY_VALUE='bci-front'
  env.INT_ENVIRONMENT_IMAGE='integration'

  env.CERT_NAMESPACE_VALUE='reg_qa'
  env.CERT_CLUSTER_NAME='bci-front-cer01'
  env.CERT_ORGANIZATION='Bci API'
  env.CERT_TARGET_WORKSPACE='qa'
  env.CERT_NAMESPACE_DEPLOY_VALUE='bci-front'
  env.CERT_ENVIRONMENT_IMAGE='qa'

  env.PROD_LOCAL_NAMESPACE_VALUE='reg_prod_local'
  env.PROD_LOCAL_CLUSTER_NAME='bci-front-prod01-pl'
  env.PROD_LOCAL_ORGANIZATION='Bci Produccion'
  env.PROD_LOCAL_TARGET_WORKSPACE='produccion'
  env.PROD_LOCAL_NAMESPACE_DEPLOY_VALUE='bci-front'
  env.PROD_LOCAL_ENVIRONMENT_IMAGE='prodpl'

  env.BASE_HREF = common.modificacionPathFRONTEND()

  stage ('prepararImagenesNpm') {
    if ("${soloStage}" == 'prepararImagenesNpm') {
      common.modificarNpmrcNPMInstall()
      def construirImagen = fileLoader.fromGit('front-end/stages/construirImagen', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      construirImagen.generarPreinstall()
      construirImagen.generarNpminstall()
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      def pruebasUnitarias = fileLoader.fromGit('front-end/stages/pruebasUnitarias', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pruebasUnitarias.call()
      sh (script: 'sudo chown -R jenkins:jenkins coverage/*', returnStdout: false)
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def ngBuild = fileLoader.fromGit('front-end/stages/ngBuild', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      def crearImagenDocker = fileLoader.fromGit('helpers/crearImagenDocker', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      ngBuild.compilar()
      crearImagenDocker.call(common, docker, nexus)
      sh (script: 'sudo chown -R jenkins:jenkins dist/*', returnStdout: false)
    }
  }

  stage ('deployKubernetes') {
    if ("${soloStage}" == 'deployKubernetes') {
      def deployKubernetes = fileLoader.fromGit('helpers/deployKubernetes', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')

      template.generar()
      login.validacionConexion(common)
      deployKubernetes.call(common)
      common.respaldarYAML()
    }
  }

  stage ('deployIngressFront') {
    if ("${soloStage}" == 'deployIngressFront') {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      login.validacionConexion(common)
      deployIngress.call("", common)
    }
  }

  stage ('gitDiff') {
    if ("${soloStage}" == 'gitDiff') {
      lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE', matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION', specificRevision: '', synchronisedScroll: true
    }
  }

  stage ('buildImagenProd') {
    if ("${soloStage}" == 'buildImagenProd') {
      def construirImagen = fileLoader.fromGit('front-end/stages/construirImagen', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      construirImagen.generarProdNpminstall()
    }
  }

  stage ('generarImagenProd') {
    if ("${soloStage}" == 'generarImagenProd') {
      def ngBuild = fileLoader.fromGit('front-end/stages/ngBuild', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      def crearImagenDocker = fileLoader.fromGit('helpers/crearImagenDocker', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      ngBuild.compilar(true)
      crearImagenDocker.call(common, docker, nexus)
      sh (script: 'sudo chown -R jenkins:jenkins dist/*', returnStdout: false)
    }
  }

  stage ('imagenProdNexus') {
    if ("${soloStage}" == 'imagenProdNexus') {
      common.imagenProdANexus(common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE), nexus, docker)
    }
  }

  stage('despliegueSitesProduccion') {
    if ("${soloStage}" == 'despliegueSitesProduccion') {
      def deployKubernetes = fileLoader.fromGit('helpers/deployKubernetes', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      def revisionProvidencia, revisionSanBernardo

      template.generar()
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            revisionProvidencia = common.getRevision('PROVIDENCIA')
            deployKubernetes.call(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            revisionSanBernardo = common.getRevision('SAN-BERNARDO')
            deployKubernetes.call(common, 'SAN-BERNARDO')
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

  stage('despliegueIngressPLProvidencia') {
    if ("${soloStage}" == 'despliegueIngressPLProvidencia') {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      try {
        login.validacionConexion(common, 'PROVIDENCIA')
        deployIngress.call('PROVIDENCIA', common)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de Ingress en Providencia, por favor revisar de manera manual")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('despliegueIngressPLSanBernardo') {
    if ("${soloStage}" == 'despliegueIngressPLSanBernardo') {
      def deployIngress = fileLoader.fromGit('helpers/deployIngress', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      try {
        login.validacionConexion(common, 'SAN-BERNARDO')
        deployIngress.call('SAN-BERNARDO', common)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de Ingress en San Bernardo, por favor revisar de manera manual")
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
