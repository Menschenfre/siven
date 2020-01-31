def call() {
  def soloStage = "${soloStage}"
  env.PROJECT_NAME="${params.projectName.toLowerCase()}"
  env.SPACE = "${params.nombreAmbiente}"
  def componente = "${params.versionComponente}"
  def despliegue = "${params.versionDespliegue}"
  env.VISIBILITY = "${params.visibilidad}"
  env.OC_VALUE = "${params.oc}"
  def branchMerge = "${params.branchMerge}"
  env.VALIDAR_RAMA = "${params.validarRama}"

  // Tipo de pipeline
  env.FE_ANGULARJS = true

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
  if (!validaciones.validarArchivosTS() || !common.validaValoresJSON(regIngress, regApiConnect, podSize, "", "")){
    currentBuild.result = 'UNSTABLE'
    return
  }
  def nombreBranch = common.branchName()
  env.BRANCH_NAME = nombreBranch

  env.VERSION_COMPONENTE = common.obtenerValorTagDocker(componente, nombreBranch)
  env.VERSION_DESPLIEGUE = common.obtenerValorTagDocker(despliegue, nombreBranch)

  env.DEPLOY_NAME = common.deployName()
  env.BASE_HREF = common.obtenerPathFE()

  env.NAMESPACE='bci-front'
  env.PROD_LOCAL_CLUSTER_NAME='bci-front-prod01-pl'

  env.CERT_AZURE_CLUSTER_NAME="bci-fro-cert001"
  env.CERT_RESOURCE_GROUP="BCIRG3CRT-RG-AKSMSFRO001"

  env.PROD_LOCAL_AZURE_CLUSTER_NAME="bci-fro-prod001"

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
      common.modificarNpmrcNPMInstall()
      def build = load(env.PIPELINE_HOME+'front-end-angularjs/stages/build.groovy');
      build.call()
    }
  }

  stage ('crearImagenDocker') {
    if ("${soloStage}" == 'crearImagenDocker') {
      def ngBuild = load(env.PIPELINE_HOME+'front-end/stages/ngBuild.groovy');
      ngBuild.archivosImagenFrontEnd(common, "dist")
      def crearImagenDocker = load(env.PIPELINE_HOME+'helpers/crearImagenDocker.groovy');
      dir('webPublico') {
        crearImagenDocker.call(common, docker, nexus)
      }
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

  stage ('deployIngressFront') {
    if ("${soloStage}" == 'deployIngressFront') {
      def deployIngress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      def azureIngress = load(env.PIPELINE_HOME+'helpers/deployAzureIngress.groovy');
      common.deployIngress(deployIngress, azureIngress, common)
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

  stage('despliegueIngressPLProvidencia') {
    if ("${soloStage}" == 'despliegueIngressPLProvidencia') {
      def ingress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      try {
        login.validacionConexion(common, 'PROVIDENCIA')
        ingress.despliegueIngress(common, 'PROVIDENCIA')
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
      def ingress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      try {
        login.validacionConexion(common, 'SAN-BERNARDO')
        ingress.despliegueIngress(common, 'SAN-BERNARDO')
      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de Ingress en San Bernardo, por favor revisar de manera manual")
      }
      common.notificarSlack("PAP", true)
    }
  }

  stage('despliegueIngressProduccion') {
    if ("${soloStage}" == 'despliegueIngressProduccion') {
      def ingress = load(env.PIPELINE_HOME+'helpers/deployIngress.groovy');
      try {
        parallel (
          'Providencia': {
            login.validacionConexion(common, 'PROVIDENCIA')
            ingress.despliegueIngress(common, 'PROVIDENCIA')
          },
          'San-Bernardo': {
            login.validacionConexion(common, 'SAN-BERNARDO')
            ingress.despliegueIngress(common, 'SAN-BERNARDO')
          }
        )

      } catch (err) {
        println "Error: "+err
        common.notificarSlack("PAP", false)
        error ("Problema en el despliegue de ingress")
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
      rollback.produccion(login, common)
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
