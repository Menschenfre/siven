def ejecutar(){
    dir("fastlane"){
        sh "bundle exec fastlane coverage"
    } 
}

return this;
