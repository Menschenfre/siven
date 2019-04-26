def call() {
    node ('Macmini'){
        properties(
          [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '', artifactNumToKeepStr: '1', artifactDaysToKeepStr: '1']]]
        )
        def soloStage = "${soloStage}"
        env.PATH ="/usr/local/bin:/usr/bin:/bin:/"
        env.LANG ="en_US.UTF-8"
        def checkoutAndSetup, linting, build, dtp, tests, coverage, common
        fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
            checkoutAndSetup = fileLoader.load('ios-modules/stages/checkoutAndSetup');
            build = fileLoader.load('ios-modules/stages/build');
            dtp = fileLoader.load('helpers/dtp');
            tests = fileLoader.load('ios-modules/stages/tests');
            coverage = fileLoader.load('ios-modules/stages/coverage');
            common = fileLoader.load('helpers/commonMethods');
            linting = fileLoader.load('ios-modules/stages/linting');
        }

        env.PROJECT_NAME = getProjectName()
        env.BRANCH_NAME = common.branchName()
          
        stage ("checkoutAndSetup"){
            if ("${soloStage}" == 'checkoutAndSetup' || "${soloStage}" == '') {
                checkoutAndSetup.ejecutar()    
            }
        }
            
        stage ("linting"){
            if ("${soloStage}" == 'linting' || "${soloStage}" == '') {
                linting.ejecutar()    
            }
        }
        
        stage ("build"){
            if ("${soloStage}" == 'build' || "${soloStage}" == '') {
                build.compilar()
            }
        }

        stage ("dtp"){
            if ("${soloStage}" == 'dtp' || "${soloStage}" == '') {
                try {
                    dtp.ios()
                }
                catch(Exception e) {
                    println "Exception: " + e.getMessage()
                }                
            }
        }
            
        stage ("tests"){
            if ("${soloStage}" == 'tests' || "${soloStage}" == '') {
                tests.ejecutar()
            }
        }
        
        stage ("coverage"){
            if ("${soloStage}" == 'coverage' || "${soloStage}" == '') {
                coverage.ejecutar()
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
