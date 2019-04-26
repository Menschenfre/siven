def compilar(){
    dir("fastlane"){
        sh "bundle exec fastlane build"
    } 
}

return this;
