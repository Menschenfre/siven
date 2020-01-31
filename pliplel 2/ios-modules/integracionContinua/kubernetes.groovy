def call() {

    def checkoutAndSetup = load(env.PIPELINE_HOME+'ios-modules/stages/checkoutAndSetup.groovy');
    def build = load(env.PIPELINE_HOME+'ios-modules/stages/build.groovy');
    def dtp = load(env.PIPELINE_HOME+'helpers/dtp.groovy');
    def tests = load(env.PIPELINE_HOME+'ios-modules/stages/tests.groovy');
    def coverage = load(env.PIPELINE_HOME+'ios-modules/stages/coverage.groovy');
    def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
    def linting = load(env.PIPELINE_HOME+'ios-modules/stages/linting.groovy');

    node ('Macmini'){
        properties(
          [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
        )
        def soloStage = "${soloStage}"
        env.PATH ="/Users/ic/.rvm/gems/ruby-2.5.0/bin:/Users/ic/.rvm/gems/ruby-2.5.0@global/bin:/Users/ic/.rvm/rubies/ruby-2.5.0/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/share/dotnet:/Library/Frameworks/Mono.framework/Versions/Current/Commands:/Applications/Xamarin Workbooks.app/Contents/SharedSupport/path-bin:/Users/ic/.rvm/bin"
        env.LANG ="en_US.UTF-8"
        env.GEM_HOME = "/Users/ic/.rvm/gems/ruby-2.5.0"
        env.GEM_PATH = "/Users/ic/.rvm/gems/ruby-2.5.0:/Users/ic/.rvm/gems/ruby-2.5.0@global"
        env.MY_RUBY_HOME = "/Users/ic/.rvm/rubies/ruby-2.5.0"
        env.IRBRC = "/Users/ic/.rvm/rubies/ruby-2.5.0/.irbrc"
        env.RUBY_VERSION = "ruby-2.5.0"

        def fueEjecutado = false
        bitbucketStatusNotify(buildState: 'INPROGRESS')

        withCredentials([usernamePassword(credentialsId: 'MACMINI_CREDENTIALS', usernameVariable: 'MACMINI_USER', passwordVariable: 'MACMINI_PASS')]){
            sh '''security unlock-keychain -p ${MACMINI_PASS} /Users/ic/Library/Keychains/login.keychain-db'''
        }

        stage ("checkoutAndSetup"){
            if ("${soloStage}" == 'checkoutAndSetup' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                checkoutAndSetup.ejecutar()
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en checkoutAndSetup. Favor revisar.')
              }
            }
        }

        stage ("linting"){
            if ("${soloStage}" == 'linting' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                linting.ejecutar()
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en linting. Favor revisar.')
              }
            }
        }

        stage ("dtp"){
            if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                dtp.ios(common)
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en dtp. Favor revisar.')
              }
            }
        }

        stage ("build"){
            if ("${soloStage}" == 'build' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                build.compilar()
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en build. Favor revisar.')
              }
            }
        }

        stage ("tests"){
            if ("${soloStage}" == 'tests' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                tests.ejecutar()
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en tests. Favor revisar.')
              }
            }
        }

        stage ("coverage"){
            if ("${soloStage}" == 'coverage' || "${soloStage}" == '') {
              fueEjecutado=true
              try{
                coverage.ejecutar()
                bitbucketStatusNotify(buildState: 'SUCCESSFUL')
              }
              catch(Exception ex) {
                bitbucketStatusNotify(buildState: 'FAILED')
                currentBuild.result = 'FAILURE'
                println "Exception: " + ex.getMessage()
                error('Se presentan problemas en coverage. Favor revisar.')
              }
            }
        }

        if (!fueEjecutado) {
          currentBuild.result = 'ABORTED'
          error('Stage invalido')
        }
    }
}

return this;
