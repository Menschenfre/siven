def call(){
    dir("fastlane"){
        sh "bundle exec fastlane upload_hockeyapp"
        sh "bundle exec fastlane notify_end"
    }
}
return this;