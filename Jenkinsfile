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
      stage ('Valida repo') {
      steps {
        script {
          def pipe = fileLoader.fromGit('helpers/'+metodo, 'https://github.com/Menschenfre/siven.git', 'master', null,'')
          pipe.call()
        }
      }
    }
  }
}


