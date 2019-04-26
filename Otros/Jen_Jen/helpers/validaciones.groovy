//dir ms = "${env.WORKSPACE}/src/main/resources/bootstrap.properties"
//dir ig = "${env.WORKSPACE}/${env.PROJECT_NAME}-server/src/main/resources/bootstrap.properties"
//dir api = "${env.WORKSPACE}/app/src/main/resources/bootstrap.properties"

def bootstrapProp(String dir, String type){

  def dirGradle = ""
  if (type == "MS") {
    dirGradle = "${env.WORKSPACE}/build.gradle"
  }
  if (type == "IG") {
    dirGradle = "${env.WORKSPACE}/${env.PROJECT_NAME}-server/build.gradle"
  }
  if (type == "API") {
    dirGradle = "${env.WORKSPACE}/app/build.gradle"
  }

  println dirGradle + " dir gradle"
  String [] linesGradle = readFile(dirGradle).split("\n")

  String [] scopeSpring
  for (value in linesGradle){
    if (value.contains("springBootVersion") && value.contains("=")) {
      println value + " Version en el build.gradle"
      version = value.replace(" ","")
      stringAux = version.split("=")
      version = stringAux[1]
      version = version.replace("\'","")
      version = version.replace(".","-")
      scopeSpring = version.split("-")
    }
  }

  def sprinConfigProperti = ""
  if ( scopeSpring[0].toInteger() < 2 ) {
    sprinConfigProperti = "spring.cloud.config.failFast"
  }else if ( scopeSpring[0].toInteger() >= 2 ) {
    sprinConfigProperti = "spring.cloud.config.fail-fast"
  }

  String [] lines = readFile(dir).split("\n")
  String text = readFile(dir)

  if(text.contains(sprinConfigProperti)){
    for (value in lines){
      if (value.trim() == sprinConfigProperti + "=true"){
        println value
        return true
      }else{
        if (value.trim() == sprinConfigProperti + "=false"){
          println "la configuracion " + sprinConfigProperti + " debe estar en true para ejecutar el pipeline en el archivo bootstrap.properties"
          return false
        }else{
          if (value.contains(sprinConfigProperti)){
            println value + " la configuracion " + sprinConfigProperti + " contiene una configuracion incorrecta en el archivo bootstrap.properties"
            return false
          }
        }
      }
    }
  }else{
    println "la configuracion " + sprinConfigProperti + " no se encontro en el archivo bootstrap.properties"
    return false
  }

  if(text.contains("spring.cloud.vault.fail-fast")){
    for (value in lines){
      if (value.trim() == "spring.cloud.vault.fail-fast=true"){
        println value
        return true
      }else{
        if (value.trim() == "spring.cloud.vault.fail-fast=false"){
          println "la configuracion spring.cloud.vault.fail-fast debe estar en true para ejecutar el pipeline en el archivo bootstrap.properties"
          return false
        }else{
          if (value.contains("spring.cloud.vault.fail-fast")){
            println value + "la configuracion spring.cloud.vault.fail-fast contiene una configuracion incorrecta en el archivo bootstrap.properties"
            return false
          }
        }
      }
    }
  }else{
    println "la configuracion spring.cloud.vault.fail-fast no se encontro en el archivo bootstrap.properties"
    return false
  }
}

return this
