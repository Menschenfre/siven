pipeline {
  agent any
  
  stages {
    stage ('Main') {
      steps {
        script {
          def metodo = "${params.METODO}"
          println metodo
          
          def pipe = fileLoader.fromGit('helpers/'+metodo, 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
          pipe.call()
        }
      }
    }
  }
}
