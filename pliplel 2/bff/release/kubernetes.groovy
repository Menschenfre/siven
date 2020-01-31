def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME = "${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.VISIBILITY = "${params.visibilidad}"
  env.OC_VALUE = "${params.oc}"
  def branchMerge = "${params.branchMerge}"
  env.VALIDAR_RAMA = "${params.validarRama}"

  def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
  def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
  def template = load(env.PIPELINE_HOME+'helpers/templates.groovy');
  def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');
  def docker = load(env.PIPELINE_HOME+'helpers/docker.groovy');
  def nexus = load(env.PIPELINE_HOME+'helpers/nexus.groovy');
  def validaciones = load(env.PIPELINE_HOME+'helpers/validaciones.groovy');

  //Dependiendo del tipo de pipeline analizamos los valores que deben estar en json
  def regIngress = common.obtenerParametro("kubernetes.ingress")
  def regApiConnect = common.obtenerParametro("api.published")
  def podSize = common.obtenerParametro("kubernetes.size")
  def webapp = common.obtenerParametro("api.product.apps")
  if (!common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  env.NAMESPACE='bci-api'

  env.PROD_LOCAL_CLUSTER_NAME='bci-api-prod01-pl'

  env.CERT_AZURE_CLUSTER_NAME="bci-api-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSAPI001"

  env.PROD_LOCAL_AZURE_CLUSTER_NAME="bci-api-prod001"

  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.obtenerPathBFF()
  common.modificarNpmrcNPMInstall()

  if(env.VALIDAR_RAMA != "null" && env.VALIDAR_RAMA != null && env.VALIDAR_RAMA == true){
    println "Se valida rama release respecto al master"
    if(!validaciones.ramaActualizada()){
      error("LA RAMA SE ENCUENTRA DESACTUALIZADA RESPECTO AL MASTER")
      currentBuild.result = 'FAILURE'
    }
  }else{
    println "No se valida rama release respecto al master"
  }

  stage ('prepararImagenesNpm') {
    if ("${soloStage}" == 'prepararImagenesNpm') {
      def construirImagenPre = load(env.PIPELINE_HOME+'bff/stages/construirImagenPre.groovy');
      construirImagenPre.generarPreinstall()
    }
  }

  stage ('pruebasUnitarias') {
    if ("${soloStage}" == 'pruebasUnitarias') {
      def pruebasUnitarias = load(env.PIPELINE_HOME+'bff/stages/pruebasUnitarias.groovy');
      pruebasUnitarias.invocar()
      sh (script: 'sudo chown -R jenkins:jenkins coverage/', returnStdout: false)
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
      def modificarPathbff = load(env.PIPELINE_HOME+'bff/stages/pruebasUnitarias.groovy');
      modificarPathbff.modificarPathBFF()
      crearImagenDocker.call(common, docker, nexus)
    }
  }

  stage ('deployKubernetes') {
    if ("${soloStage}" == 'deployKubernetes') {
      common.listadoDespleigues(env.PROJECT_NAME, env.BRANCH_NAME, env.WORKSPACE, "QA")
      def deployKubernetes = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      def azureKubernetes = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      template.generar()
      template.generar(true)
      common.deployKubernetes(deployKubernetes, azureKubernetes, common)
      common.respaldarYAML()
    }
  }

  stage ('deployIngressBFF') {
    if ("${soloStage}" == 'deployIngressBFF') {
      def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
      common.deployIngress(deployIngress, azureIngress, common)
    }
  }

  stage ('registroApiGee') {
    if ("${soloStage}" == 'registroApiGee') {
      def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');

      try {
        apiMngt.registrar(env.PROJECT_NAME, env.SPACE, webapp)
      } catch (Exception err) {
        println err
        common.notificarSlack("apigee")
        error ("Error en registroApiGee")
      }
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
      def deployment = load(env.PIPELINE_HOME+'helpers/deployKubernetes.groovy');
      def revisionProvidencia, revisionSanBernardo
      template.generar()
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            revisionProvidencia = common.getRevision('PROVIDENCIA')
            deployment.despliegueKubernetes(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            revisionSanBernardo = common.getRevision('SAN-BERNARDO')
            deployment.despliegueKubernetes(common, 'SAN-BERNARDO')
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
      def ingress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      try {
        login.validacionConexion(common, 'PROVIDENCIA')
        ingress.despliegueIngress(common, 'PROVIDENCIA')

        login.validacionConexion(common, 'SAN-BERNARDO')
        ingress.despliegueIngress(common, 'SAN-BERNARDO')
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Error al despliegar ingress en produccion")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage ('registroApiConnectProd') {
    if ("${soloStage}" == 'registroApiConnectProd' || "${soloStage}" == 'registroApiConnectPAASLocal') {
      def apiConnect = load(env.PIPELINE_HOME+'helpers/api-connect.groovy');
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

  stage ('apigeeProd') {
    if ("${soloStage}" == 'apigeeProd') {
      def apiMngt = load(env.PIPELINE_HOME+'helpers/apimanagement.groovy');
      try {
        apiMngt.registrar(env.PROJECT_NAME, "ACONCAGUA", webapp, common.obtenerNombreNodo("ACONCAGUA"))
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("apigee")
        currentBuild.result = 'SUCCESS'
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('deployProd') {
    if ("${soloStage}" == 'deployProd') {
      def deploy = load(env.PIPELINE_HOME+'helpers/deployAzureKubernetes.groovy');
      def revAconcagua, revLongovilo
      template.generar(true)
      stash includes: "az-"+env.DEPLOY_NAME+".yml", name: 'deployAzure'
      try {
        parallel (
          'Aconcagua': {
            node (common.obtenerNombreNodo("ACONCAGUA")){
              revAconcagua = common.getRevisionAzureProd()
              unstash 'deployAzure'
              deploy.despliegueKubernetes()
            }
          },
          'Longovilo': {
            node (common.obtenerNombreNodo("LONGOVILO")){
              revLongovilo = common.getRevisionAzureProd()
              unstash 'deployAzure'
              deploy.despliegueKubernetes()
            }
          }
        )
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        rollback.azureSite(common, "ACONCAGUA", revAconcagua)
        rollback.azureSite(common, "LONGOVILO", revLongovilo)
        common.notificarSlack("PAP_AZURE", false)
        //error ("Rollback realizado")
      }
    }
  }

  stage('ingressProd') {
    if ("${soloStage}" == 'ingressProd') {
      try {
        def ingress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
        ingress.despliegueIngress(common)
        common.notificarSlack("PAP_AZURE", true)
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP_AZURE", false)
        //error ("Error al despliegar ingress en produccion")
      }
    }
  }

  stage ('rollbackKubernetes') {
    if ("${soloStage}" == 'rollbackKubernetes') {
      rollback.produccion(login, common, regApiConnect, env.PROJECT_NAME)
    }
  }

  stage ('MergeDevelop') {
    if ("${soloStage}" == 'MergeDevelop') {
        common.mergeRama(branchMerge, nombreBranch)
    }
  }

  stage ('MergeMaster') {
    if ("${soloStage}" == 'MergeMaster') {
        common.mergeRama(branchMerge, nombreBranch)
    }
  }

}

return this;
