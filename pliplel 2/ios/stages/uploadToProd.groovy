def call(){
    dir("fastlane"){
        sh "bundle exec fastlane upload_testflight"
        sh "bundle exec fastlane notify_end"
    }
}
return this;