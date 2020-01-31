def ejecutar(){
    dir("fastlane"){
        sh "bundle exec fastlane linting"
    } 
}

return this;
