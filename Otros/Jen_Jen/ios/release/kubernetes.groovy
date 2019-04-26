def call() {
    node ('Macmini'){
        def soloStage = "${soloStage}"
        env.PATH ="/usr/local/bin:/usr/bin:/bin:/"
        env.LANG ="en_US.UTF-8"
        def checkoutAndSetup, buildIpaQA, uploadToQA, buildIpaProd
        fileLoader.withGit('git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '') {
            checkoutAndSetup = fileLoader.load('ios/stages/checkoutAndSetup');
            buildIpaQA = fileLoader.load('ios/stages/buildIpaQA');
            uploadToQA = fileLoader.load('ios/stages/uploadToQA');
            buildIpaProd = fileLoader.load('ios/stages/buildIpaProd');
            uploadToProd = fileLoader.load('ios/stages/uploadToProd');
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

return this;
