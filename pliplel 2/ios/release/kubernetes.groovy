def call() {

    def checkoutAndSetup = load(env.PIPELINE_HOME+'ios/stages/checkoutAndSetup.groovy');
    def buildIpaQA = load(env.PIPELINE_HOME+'ios/stages/buildIpaQA.groovy');
    def veracode = load(env.PIPELINE_HOME+'helpers/veracode.groovy');
    def uploadToQA = load(env.PIPELINE_HOME+'ios/stages/uploadToQA.groovy');
    def buildIpaProd = load(env.PIPELINE_HOME+'ios/stages/buildIpaProd.groovy');
    def uploadToProd = load(env.PIPELINE_HOME+'ios/stages/uploadToProd.groovy');
    def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');

    node ('Macmini'){
        def soloStage = "${soloStage}"
        env.PATH ="/usr/local/bin:/usr/bin:/bin:/"
        env.LANG ="en_US.UTF-8"
        env.GEM_HOME = "/Users/ic/.rvm/gems/ruby-2.5.0"
        env.GEM_PATH = "/Users/ic/.rvm/gems/ruby-2.5.0:/Users/ic/.rvm/gems/ruby-2.5.0@global"
        env.MY_RUBY_HOME = "/Users/ic/.rvm/rubies/ruby-2.5.0"
        env.IRBRC = "/Users/ic/.rvm/rubies/ruby-2.5.0/.irbrc"
        env.RUBY_VERSION = "ruby-2.5.0"
 
        //env.PROJECT_NAME = getProjectName()
        env.BRANCH_NAME = common.branchName() 
        //def workSpace = "${WORKSPACE}"

        withCredentials([usernamePassword(credentialsId: 'MACMINI_CREDENTIALS', usernameVariable: 'MACMINI_USER', passwordVariable: 'MACMINI_PASS')]){
            sh '''security unlock-keychain -p ${MACMINI_PASS} /Users/ic/Library/Keychains/login.keychain-db'''
        }

        stage ("checkoutAndSetup"){
            if ("${soloStage}" == 'checkoutAndSetup') {
                checkoutAndSetup.call()    
            }
        }
            
        stage ("buildIpaQA"){
            if ("${soloStage}" == 'buildIpaQA') {
                buildIpaQA.call()
            }
        }

        stage ("veracode"){            
            if ("${soloStage}" == 'veracode') {
                node('master'){
                    //veracode.ios(workSpace)
                }
            }
        }
            
        stage ("uploadToQA"){
            if ("${soloStage}" == 'uploadToQA') {
                uploadToQA.call()
            }
        }
        
        stage ("buildIpaProd"){
            if ("${soloStage}" == 'buildIpaProd') {
                buildIpaProd.call()
            }
        }
            
        stage ("uploadToProd"){
            if ("${soloStage}" == 'uploadToProd') {
                uploadToProd.call()
            }
        }          
    }
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;
