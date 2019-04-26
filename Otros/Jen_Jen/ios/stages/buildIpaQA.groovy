def call(){
    dir("fastlane"){
        sh "bundle exec fastlane build_ipa_qa"
    }
}

return this;