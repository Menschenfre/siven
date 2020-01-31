def call(){
  def soloStage = "${soloStage}"  
  env.PROJECT_NAME ="${params.projectName.toLowerCase()}"
  def versionBuild =  sh (script: ''' set +x;/opt/gradle/gradle-4.1/bin/gradle properties | grep version | awk '{print $2}' ''', returnStdout: true).trim()

  if(versionBuild.contains('unspecified') || versionBuild ==''){
    error ('Error en el formato de la version, por favor corregir')
  }
  def tagCandidato =  env.PROJECT_NAME+"-"+versionBuild
  def tagExiste = verificarTag()    

  stage('buildCodigo') {
    if("${soloStage}" == 'buildCodigo'){
      if(!tagExiste){
        sh "/opt/gradle/gradle-4.1/bin/gradle checkVersion clean install upload -x test -Dfile.encoding=ISO-8859-1 --no-daemon -PmavenUser=$NEXUS_USR -PmavenPass=$NEXUS_PSW "
      } else {
        error ('El tag :'+tagCandidato+', ya existe. Se detiene Job')
      }
    }  
  }

  stage('taggingLib') {
    if("${soloStage}" == 'taggingLib'){
      if(!tagExiste){
        sh "git tag $tagCandidato"
        sh "git push --tags"
        println('El tag: '+tagCandidato+' ha sido creado.')
      } else {
        error ('El tag :'+tagCandidato+', ya existe. Se detiene Job')
      }
    }  
  }
}

def verificarTag(){
  println('Verificando si tagCandidato ya existe...')
  def existe = false
  //tagCandidato es una variable de ambiente
  def tagExiste = sh (script: ''' set +x;git tag | grep ${tagCandidato} -c''', returnStatus:true)
  if (tagExiste == 0){
    existe = true
  }
  return existe
}

return this;