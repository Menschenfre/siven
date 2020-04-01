pipeline {
  agent any
  parameters {
    string(name: 'Stage', defaultValue: '', description: 'Stage a ejecutar')
  }
  stages {
    stage ('Main') {
      steps {
        script {
          def pipe = fileLoader.fromGit('MS/IC', 'https://mhsa@bitbucket.org/mhsa/jenkins.git', 'master', null,'')
          pipe.call()
        }
      }
    }
  }
}
