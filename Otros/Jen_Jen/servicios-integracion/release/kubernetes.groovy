def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.PORT_NUMBER = '8080'
  env.BLUEMIX_HOME = '.bluemix'
  env.OC_VALUE = "${params.oc}"

  def common, login, template, docker, nexus, rollback, validaciones
  fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
    common = fileLoader.load('helpers/commonMethods');
    login = fileLoader.load('helpers/login');
    template = fileLoader.load('helpers/templates');
    docker = fileLoader.load('helpers/docker');
    nexus = fileLoader.load('helpers/nexus');
    rollback = fileLoader.load('helpers/rollbackKubernetes');
    validaciones = fileLoader.load('helpers/validaciones');
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
      withCredentials([usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW')]) {
        def componenteCorregido = componente.replace("re-v","").replace("-",".")
        def versionBuild = common.obtenerVersionBuildIG()
        if (!versionBuild.contains(componenteCorregido) || versionBuild.toLowerCase().contains("snapshot")){
          println "El numero de la version ingresada en XL Release = "+componente+", no coincide con "+versionBuild+" establecida en el archivo build.gradle"
          currentBuild.result = 'FAILURE'
          error("FAILURE")
        }
        println "Generando "+versionBuild

        def buildCodigo = fileLoader.fromGit('servicios-integracion/stages/buildCodigo', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')

        if(validaciones.bootstrapProp("${env.WORKSPACE}/${env.PROJECT_NAME}-server/src/main/resources/bootstrap.properties","IG")){
          buildCodigo.call()
        }else{
          currentBuild.result = 'UNSTABLE'
          return
        }
      }
    }
  }

  stage ('checkingQA') {
    if ("${soloStage}" == 'checkingQA') {
      env.DIR_EXTRA=sh (script: 'set +x;find . -type d -iname *Server | sed -e "s/\\.//g"', returnStdout: true).trim()
      def checkingQA = fileLoader.fromGit('helpers/checkingQA', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')

      checkingQA.call()
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      def pruebasUnitarias = fileLoader.fromGit('servicios-integracion/stages/pruebasUnitarias', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pruebasUnitarias.call()
    }
  }

  stage ('pruebasCobertura') {
    if ("${soloStage}" == 'pruebasCobertura') {
      def pruebasCobertura = fileLoader.fromGit('servicios-integracion/stages/pruebasCobertura', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pruebasCobertura.call()
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def crearImagenDocker = fileLoader.fromGit('helpers/crearImagenDocker', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      def serverPath = sh (script: 'find . -type d -iname *Server', returnStdout: true).trim()

      dir ( serverPath ){
        crearImagenDocker.call(common, docker, nexus)
      }
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

  stage ('pruebasFuncionales') {
    if ("${soloStage}" == 'pruebasFuncionales') {
      env.AMBIENTE_PIPE='integracion'
      env.ZUUL_SERVER_URI='bci-api-ic01.us-south.containers.mybluemix.net'
      env.MS_VERSION=env.VERSION_COMPONENTE
      def pruebasFuncionales = fileLoader.fromGit('servicios-integracion/stages/pruebasFuncionales', 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pruebasFuncionales.call()
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
