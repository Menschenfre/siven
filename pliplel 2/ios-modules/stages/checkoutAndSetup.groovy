def ejecutar(){
    deleteDir()
    checkout scm 
    dir("fastlane"){
        sh "bundle install"
        sh "bundle exec fastlane install_pods"
    } 
}

return this;
