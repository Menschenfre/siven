def call() {
  def soloStageArray = "${soloStage}".split(';')

  def apigeeProfile = "dsr"
  if (env.WORKING_PIPELINE.contains("Release")){
    apigeeProfile = "crt"
  }
  
  def proxyPaths = sh (script: 'find $(pwd) -type d -name proxy', returnStdout: true).trim().split("\n")
  def configPaths = sh (script: 'find $(pwd) -type d -name config', returnStdout: true).trim().split("\n")
  def testPaths = sh (script: 'find $(pwd) -type d -name test', returnStdout: true).trim().split("\n")

  sh "git --git-dir=/opt/kubernetes/properties_pipeline_jenkins/.git --work-tree=/opt/kubernetes/properties_pipeline_jenkins/ pull"
    
  stage('postmanJsonPRE'){
    if (soloStageArray.contains('postmanJsonPRE') || "${soloStage}" == ''){
      for (String path:configPaths){
        dir(path){
          try{
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
            postmanJson("PRE", apigeeProfile)
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
          } catch (Exception err){
              currentBuild.result = 'FAILURE'
              println "Exception: " + err.getMessage()
              error("Error en postmanJsonPRE")
          }
        }
      }
    }
  }

  stage('uploadToApigee'){
    if (soloStageArray.contains('uploadToApigee') || "${soloStage}" == ''){
      for (String path:proxyPaths){
        dir(path){
          //sh "apigeelint -s apiproxy -f table.js"
          sh "/opt/apache-maven-3.5.0/bin/mvn -q -f pom.xml install -P"+apigeeProfile+" -Dusername=${APIGEE_USR} -Dpassword=${APIGEE_PSW}"
        }
      }
    } 
  }

  stage('postmanJsonPOST'){
    if (soloStageArray.contains('postmanJsonPOST') || "${soloStage}" == ''){
      for (String path:configPaths){
        dir(path){
          try{
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
            postmanJson("POST", apigeeProfile)
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
          } catch (Exception err){
              currentBuild.result = 'FAILURE'
              println "Exception: " + err.getMessage()
              error("Error en postmanJsonPOST")
          }
        }
      }
    }
  }

  stage('postmanTest'){
    if (soloStageArray.contains('postmanTest') || "${soloStage}" == ''){
      for (String path:testPaths){
        dir(path){
          try{
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
            postmanJson("TEST", apigeeProfile)
            fileOperations([ fileDeleteOperation(includes:'**/fixed-collection.json, **/config.postman.json')])
          } catch (Exception err){
              currentBuild.result = 'FAILURE'
              println "Exception: " + err.getMessage()
              error("Error en postmanTest")
          }  
        }
      }
    }
  }
}

def postmanJson(String prefix, Object apigeeProfile){
  def jsonFile
  switch(prefix) {
    case "PRE":
      jsonFile = "CONFIG"
    break
    case "POST":
      jsonFile = "CONFIG_POST"
    break
    case "TEST":
      jsonFile = "TEST"
    break
    default:
      error('Prefijo '+prefix+' no es correcto -> postmanJson()')
    break
  }

  def archivos = findFiles(glob: "**/*"+jsonFile+".postman_collection.json")
  println "Collection: " + archivos
  println "Creando config.postman.json para "+apigeeProfile
  def jsonConfig = readJSON file: "/opt/kubernetes/properties_pipeline_jenkins/openBanking/"+apigeeProfile+"-config.json"
  jsonConfig.values[3].value = env.APIGEE_USR
  jsonConfig.values[4].value = env.APIGEE_PSW
  writeJSON file:'config.postman.json', json: jsonConfig

  for (Object elem:archivos){
    println "Creando fixed-collection.json"
    def json = readJSON file: elem.name            
    writeJSON file:'fixed-collection.json', json: json
    println "Ejecutando newman para " + elem.name
    sh "newman run fixed-collection.json -e config.postman.json --bail --insecure"
  }
}

return this;
