def call(){
    deleteDir()
    checkout scm 
    dir("fastlane"){
        sh "bundle exec fastlane notify_begin"
        sh "bundle install"
        sh "bundle exec fastlane install_pods"
    } 
}

return this;
