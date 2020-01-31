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

      println "Stage invocado -> ${soloStage}\nPipeline invocado -> ${workingPipeline}\nAplicacion invocada -> ${pipelineApp}"

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
        case "OPENBANKING":
          completePath = 'openbanking/kubernetes'
        break
        case "CRONJOB":
          completePath = 'cronJob/'+pipelineType+'/kubernetes'
        break
          default:
          error('No existe pipeline definido para '+workingPipeline)
        break
      }
      env.PIPELINE_APP = pipelineApp
      env.WORKING_PIPELINE=workingPipeline

      //valida REG_EUREKA
      // si no existe o es true deja UNSTABLE el pipeline
      if (env.PIPELINE_APP in ["MS", "MOBILE", "SI", "SI-EJB", "BFF"]){
        if(!env.REG_EUREKA || env.REG_EUREKA == "true"){
          println "variable env.REG_EUREKA no encontrada en jenkinsfile o en estado true"
          currentBuild.result = 'UNSTABLE'
          return
        }
      }

      //Git pull sobre repositorio comun de pipeline
      env.PIPELINE_HOME="/opt/tools/pipelines-jenkins/"
      sh "git --git-dir=${PIPELINE_HOME}.git --work-tree=${PIPELINE_HOME} pull"

      readAzureFlag()
      readVeracodeParams()
      if(!evitarCredenciales){
        withCredentials([usernamePassword(credentialsId: '0729512a-ddc3-4526-9079-229fa128ce66', usernameVariable: 'PASSLOCAL_PROD_USER', passwordVariable: 'PASSLOCAL_PROD_PASS'),
          usernamePassword(credentialsId: 'apiConnectPassLocal', usernameVariable: 'PASSLOCAL_APIC_USER', passwordVariable: 'PASSLOCAL_APIC_PASS'),
          usernamePassword(credentialsId: 'nexus-dev-admin', usernameVariable: 'NEXUS_USR', passwordVariable: 'NEXUS_PSW'),
          usernamePassword(credentialsId: 'AZURE_CLOUD_DSR', usernameVariable: 'INT_AZURE_USER', passwordVariable: 'INT_AZURE_PASS'),
          usernamePassword(credentialsId: 'AZURE_CLOUD_CRT', usernameVariable: 'CERT_AZURE_USER', passwordVariable: 'CERT_AZURE_PASS'),
          usernamePassword(credentialsId: 'CREDENCIALES_APIGEE', usernameVariable: 'APIGEE_USR', passwordVariable: 'APIGEE_PSW')]){
            def pipeline = load(env.PIPELINE_HOME+completePath+".groovy")
            pipeline.call()
      }
    }
    else{
      def pipeline = load(env.PIPELINE_HOME+completePath+".groovy")
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

def readAzureFlag(){
  if (env.PIPELINE_APP in ["MS", "MOBILE", "SI", "SI-EJB", "FRONTEND", "BFF", "FRONTEND-AngularJS"]){
    def archivo = readJSON file: "container_params.json"
    def flag = archivo.kubernetes.azure
    if (flag == null){
      flag = false
    }
    figlet "FLAG  AZURE  ->  "+flag.toString().toUpperCase()
  }
}

def readVeracodeParams(){
  def archivo = [veracode:[app:'BCI_Mobile_Android_Personas']]
  if(env.PIPELINE_APP == 'Android'){
    env.VERACODE = archivo.veracode
  } else if(env.PIPELINE_APP in ["MS", "MOBILE", "SI", "SI-EJB", "FRONTEND", "BFF", "FRONTEND-AngularJS"]){
    archivo = readJSON file: "container_params.json"
    env.VERACODE = archivo.veracode
  }

  if(env.VERACODE != "null"){
    env.VERACODE_APP = archivo.veracode.app
    if (env.VERACODE_APP == "null"){
      error('Parametro veracode.app, no declarado en container_params.json')
      currentBuild.result = 'UNSTABLE'
      return
    }
  }

  /*if (env.PIPELINE_APP in ["MS", "MOBILE", "SI", "SI-EJB", "FRONTEND", "BFF", "FRONTEND-AngularJS", "Android"]){
    def archivo = readJSON file: "container_params.json"
    env.VERACODE = archivo.veracode
    if(env.VERACODE != "null"){
      env.VERACODE_APP = archivo.veracode.app
      if (env.VERACODE_APP == "null"){
        error('Parametro veracode.app, no declarado en container_params.json')
        currentBuild.result = 'UNSTABLE'
        return
      }
    }
  }*/
}

return this;
