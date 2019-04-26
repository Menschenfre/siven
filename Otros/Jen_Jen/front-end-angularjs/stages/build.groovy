def call() {
  dir('webPublico') {
    nodejs(configId: 'repo-bci', nodeJSInstallationName: 'NodeJS-9.5.0') {
      sh 'npm install'
      sh 'grunt build'
    }
  }
}

return this;
