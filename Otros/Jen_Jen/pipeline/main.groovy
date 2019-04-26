def call() {
    stage ('Pipeline') {

      def workingPipeline = getWorkingPipeline()

      if(workingPipeline==""){
          workingPipeline = currentBuild.description
      }

      def pipelineType, pipelineApp, completePath
      def evitarCredenciales=false

      if (workingPipeline.contains("Release")){
        pipelineType = 'release'
        pipelineApp =workingPipeline.split('-Release')[0]
        if (!"${soloStage}"){
          error('Valor soloStage no puede ser vacio')
        }
      }
      else {
        pipelineType = 'integracionContinua'
        pipelineApp = workingPipeline.split('-IntegracionContinua')[0]
      }

      println "Stage invocado -> ${soloStage}"
      println "Pipeline invocado -> ${workingPipeline}"
      println "Aplicacion invocada -> ${pipelineApp}"

      switch(pipelineApp) {
        case "MS":
          completePath = 'microservicios/'+pipelineType+'/kubernetes'
        break
        case "SI":
          completePath = 'servicios-integracion/'+pipelineType+'/kubernetes'
        break
        case "MOBILE":
          completePath = 'mobile/'+pipelineType+'/kubernetes'
        break
        case "FRONTEND":
          completePath = 'front-end/'+pipelineType+'/kubernetes'
        break
        case "FRONTEND-AngularJS":
          completePath = 'front-end-angularjs/'+pipelineType+'/kubernetes'
        break
        case "BFF":
          completePath = 'bff/'+pipelineType+'/kubernetes'
        break
        case "BFF-MOBILE":
          completePath = 'bff-mobile/'+pipelineType+'/kubernetes'
        break
        case "SI-EJB":
          completePath = 'servicios-integracion-ejb/'+pipelineType+'/kubernetes'
        break
        case "ANDROID-MODULES":
          completePath = 'android-modules/'+pipelineType+'/kubernetes'
        break
        case "Android":
          completePath = 'android/'+pipelineType+'/kubernetes'
        break
        case "MODULOS-ANGULAR":
          completePath = 'modulos-angular/kubernetes'
        break
        case "MonitoreoSP":
          completePath = 'monitoreo-sp/'+pipelineType+'/monitoreo'
          evitarCredenciales=true
        break
        case "MS-CORELIBS":
          completePath = 'ms-corelibs/'+pipelineType+'/kubernetes'
        break
        case "IOS":
          completePath = 'ios/'+pipelineType+'/kubernetes'
        break
        case "IOS-MODULES":
          completePath = 'ios-modules/'+pipelineType+'/kubernetes'
        break
          default:
          error('No existe pipeline definido para '+workingPipeline)
        break
      }
      env.PIPELINE_APP = pipelineApp
      env.WORKING_PIPELINE=workingPipeline
      if(!evitarCredenciales){
        withCredentials([usernamePassword(credentialsId: 'Deploy_Integracion_bluemix', usernameVariable: 'INT_BLUEMIX_CREDENTIALS_USR', passwordVariable: 'INT_BLUEMIX_CREDENTIALS_PSW'),
          usernamePassword(credentialsId: 'Deploy_Certificacion_bluemix', usernameVariable: 'CERT_BLUEMIX_CREDENTIALS_USR', passwordVariable: 'CERT_BLUEMIX_CREDENTIALS_PSW'),
          usernamePassword(credentialsId: 'Deploy_Produccion_bluemix', usernameVariable: 'PROD_BLUEMIX_CREDENTIALS_USR', passwordVariable: 'PROD_BLUEMIX_CREDENTIALS_PSW'),
          usernamePassword(credentialsId: '0729512a-ddc3-4526-9079-229fa128ce66', usernameVariable: 'PASSLOCAL_PROD_USER', passwordVariable: 'PASSLOCAL_PROD_PASS'),
          usernamePassword(credentialsId: 'apiConnectPassLocal', usernameVariable: 'PASSLOCAL_APIC_USER', passwordVariable: 'PASSLOCAL_APIC_PASS'),
          usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW'),
          usernamePassword(credentialsId: 'AZURE_CLOUD_DSR', usernameVariable: 'INT_AZURE_USER', passwordVariable: 'INT_AZURE_PASS'),
          usernamePassword(credentialsId: 'AZURE_CLOUD_CRT', usernameVariable: 'CERT_AZURE_USER', passwordVariable: 'CERT_AZURE_PASS'),
          string(credentialsId: 'BLUEMIX_CREDENTIALS_ID', variable: 'BLUEMIX_CREDENTIALS_ID'),
          string(credentialsId: 'INT_KUBERNETES_TOKEN_VAULT', variable: 'INT_VAULT_TOKEN'),
          string(credentialsId: 'CERT_KUBERNETES_TOKEN_VAULT', variable: 'CERT_VAULT_TOKEN'),
          string(credentialsId: 'PROD_KUBERNETES_TOKEN_VAULT', variable: 'PROD_VAULT_TOKEN'),
          string(credentialsId: 'VAULT_PROD_LOCAL', variable: 'PROD_LOCAL_VAULT_TOKEN'), ])
      {
        def pipeline = fileLoader.fromGit(completePath, 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
        pipeline.call()
      }
    }
    else{
      def pipeline = fileLoader.fromGit(completePath, 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      pipeline.call()
    }
  }
}

@NonCPS
def getWorkingPipeline() {
  def proyecto = currentBuild.rawBuild.project
    def workingPipeline = "${proyecto.parent.description}"
  return workingPipeline
}

return this;
