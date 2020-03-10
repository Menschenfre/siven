pipeline {
  agent any
  
  stages {
    stage ('Main') {
      steps {
        script {
          def metodo = "Sin métodos"
          println metodo
          
          def pipe = "Sin pipe"
          println pipe
        }
      }
    }
    stage ('Test') {
      steps {
        script {
          def estado = "Pruebas exitosas"
          println estado
          
          def fin = "Finalizado"
          println fin
        }
      }
    }
      stage ('Valida métodos jenkins') {
      steps {
        script {
          def pipe = fileLoader.fromGit(/, 'https://mhsa@bitbucket.org/mhsa/jenkins.git', 'master', null,'')
          pipe.call()
        }
      }
    }
  }
}


