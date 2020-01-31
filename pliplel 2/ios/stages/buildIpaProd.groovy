def call(){
	println "se realiza git pull por los cambios de version en archivo: Info.plist"
	comando = sh(script: "git pull origin ${BRANCH_NAME}", returnStdout: true).trim()
    
    if(comando.contains('Already up to date')){
        println "Rama actualizada"
        println "Comando: "+comando
    }else{
        println "directorio no estaba actualizado, se actualiza a ultimo commit..."
        println "Comando: "+comando
    }

    dir("fastlane"){
        sh "bundle exec fastlane build_ipa_prod"
    }
}

return this;