def ejecutar(){
    dir("fastlane"){
        sh "bundle exec fastlane tests"
    } 
}

return this;
