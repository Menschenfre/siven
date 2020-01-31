def call() {
  println "--- CALLING VERACODE FILE ---"
  def type = env.PIPELINE_APP

  def new_type = verifyType(type)

  executeScan(type)
}

def verifyType(type){
  def new_type = type

  def build_gra = fileExists "${WORKSPACE}/build.gradle"
  def setting_build = fileExists "${WORKSPACE}/settings.gradle"

  def packages = fileExists "${WORKSPACE}/package.json"

  if(type == "BFF" && build_gra && setting_build)
    new_type = "MS"
  if(type == "BFF" && !build_gra && !setting_build && packages)
    new_type = "FRONTEND"

  return new_type
}

/**
 DESCRIPTION:
 1RO: VALIDA LA ETIQUETA VERACODE_APP CORRESPONDA
 2DO: MUESTRA EL MENSAJE DEPENDIENDO DEL TIPO DE COMPONENTE QUE SE analizamos
 3RO: DEVULEVE EL PATRON QUE NECESITA VERACODE PARA SUBIR EL BINARIO
 4TO: EJECUTA EL SCAN EN Veracode
 5TO: SI ES FRONT, BORRA EL ZIP CREADO EN EL WORKSPACE
 RETURN:

 */
def executeScan(type){

  def project = env.VERACODE_APP

  if (!validarProject(project)){
    error('Aplicación Veracode no se encuentra dentro de las habilitadas')
    currentBuild.result = 'UNSTABLE'
    return
  }
  println "EJECUCION VERACODE ${type}"

  def sandbox = env.PROJECT_NAME
  println "PROYECTO COMPLETO: " + project
  println "SANDBOX: " + sandbox

  def pattern = getPattern(type, "${WORKSPACE}")

  println "PATTERN: " +pattern


  if(type == "Android"){
    executeVeracodeInApp(project,pattern)
  }else {
    executeVeracodeInSandbox(project,sandbox,pattern)
  }
  
  if(type == 'FRONTEND' || type=='BFF'){
    sh '''
    # se borra zip del proyecto en carpeta zip
    rm -rf ${WORKSPACE}/${PROJECT_NAME}.zip
    '''

    println "ELIMINANDO ${WORKSPACE}/${PROJECT_NAME}.zip"
  }

}

/**
 DESCRIPTION:
 VALIDA QUE LA ETIQUETA VERACODE_APP CORRESPONDA A UNAS DE LAS APP CREADAS EN VERACODE
 RETURN:
 TRUE OR FALSE
 */
def validarProject(project){
  def retorno = false

  if (project in ["BCI_Mobile_Android_Personas", "Mobile_BCI_IOs_Personas",
                  "MS_Mobile", "WholeSale_BCI_360", "WholeSale_360_Connect",
                  "TD_Viajes_Pyme", "TD_Viajes_Chip", "TD_Viajes_Planes", "TD_Eco_Chip"]){
    retorno = true
  }
  return retorno
}

/**
 DESCRIPTION:
 EJECUTA EL ANALISIS DE VERACODE REALIZANDOSE EN EL SANDBOX
 PARAMETERS:
 PROJECT: NOMBRE DE LA APP EN VERACODE
 SANDBOX: NOMBRE DEL SANDBOX EN LA APP DE VERACODE
 PATTERN: RUTA O NOMBRE DEL BINARIO QUE SE VA A ANALIZAR
 RETURN:

 */
def executeVeracodeInSandbox(project,sandbox,pattern){

  veracode applicationName: project, canFailJob: false, createSandbox: true,
          criticality: 'VeryHigh', fileNamePattern: '', replacementPattern: '',
          sandboxName: sandbox, scanExcludesPattern: '', scanIncludesPattern: '',
          scanName: '$timestamp', teams: 'Default Team', timeout: 60,
          uploadExcludesPattern: '', uploadIncludesPattern: pattern,
          useIDkey: true,  vid: '76b5b39088fccaa93c6cd0ac08a244b4',
          vkey: '0dca41d576c7538f0f9de8e62dec38ca088fb0d3ca1449f75d460eb0972cf8c5dd97bab94168f45e4938b74e51c93e8deddfb20b572c5040c106f5ddeb4b3f91',
          vpassword: '', vuser: '', waitForScan: true, createProfile: true, debug: true
}

def executeVeracodeInApp(project,pattern){

  veracode applicationName: project, canFailJob: false,
          criticality: 'VeryHigh', fileNamePattern: '', replacementPattern: '',
          scanExcludesPattern: '', scanIncludesPattern: '',
          scanName: '$timestamp', teams: 'Default Team', timeout: 60,
          uploadExcludesPattern: '', uploadIncludesPattern: pattern,
          useIDkey: true,  vid: '76b5b39088fccaa93c6cd0ac08a244b4',
          vkey: '0dca41d576c7538f0f9de8e62dec38ca088fb0d3ca1449f75d460eb0972cf8c5dd97bab94168f45e4938b74e51c93e8deddfb20b572c5040c106f5ddeb4b3f91',
          vpassword: '', vuser: '', waitForScan: true, createProfile: true, debug: true
}

/**
 DESCRIPTION:
 DEVUELVE EL NOMBRE O LA RUTA DEL BINARIO QUE SE NECESITA ANALIZAR
 PARAMETERS:
 TYPE: TIPO DE COMPONENTE QUE SE ANALIZA: ANDROID, IOS, FRONT,...
 WORKSPACE: RUTA DE LAS FUENTES O DONDE SE ENCUENTRA EL BINARIO
 RETURN:
 PATTERN
 */
def getPattern(type, workSpace){
  def pattern

  switch(type) {
    case 'MS':
      pattern = '**/build/libs/**.jar'
      break
    case 'SI':
      pattern = '**/build/libs/**.jar'
      break
    case 'MOBILE':
      break
    case 'SI-EJB':
      break
    case 'BFF':
      zipCode()

      pattern = '**.zip'
      break
    case 'FRONTEND-AngularJS':
      break
    case 'ANDROID-MODULES':
      pattern = '**/build/outputs/aar/**release.aar'
      break
    case 'Android':
      pattern = '**/app/build/outputs/apk/integration/**.apk'
      break
    case 'ios':
      // CUAL ESTE WORKSPACE DE IOS, DE DONDE SE OBTIENE

      println "VAR WORKSPACE: " + workSpace
      sh "scp ic@161.131.137.239:$workSpace/*.ipa ${WORKSPACE}"
      pattern = '**.ipa'
      break
    case 'FRONTEND':
      zipCode()

      pattern = '**.zip'

      break
    default:
      error('Componente no analizable')
      break
  }

  return pattern
}

def zipCode(){
   sh '''
      zip -r ${PROJECT_NAME}.zip ${WORKSPACE}/*
      '''
}


return this;
