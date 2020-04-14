pipeline {
  agent any
  parameters {
    string(name: 'Stage', defaultValue: '', description: 'Stage a ejecutar')
  }
  stages {
    stage ('Main') {
      steps {
        script {
          env.PROJECT_TYPE="PHP"
          env.REAL_PORT="80"
          env.PORT_FORWARDING="7070"
          def pipe = fileLoader.fromGit('MS/IC', 'https://mhsa@bitbucket.org/mhsa/jenkins.git', 'master', null,'')
          pipe.call()
        }
      }
    }
  }
}
