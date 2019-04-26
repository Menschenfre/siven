def call() {
    if (env.PROJECT_NAME != 'bff-mobile'){
        sh(script: 'set +x;echo "Ambiente -> "${AMBIENTE_PIPE}" ZUUL -> "${ZUUL_SERVER_URI}')
        def taskExists = sh(script: 'set +x;bash gradlew tasks --all', returnStdout: true)
        if (taskExists.contains("acceptanceTest")){
            def retstatus = sh(script: 'set +x;SPRING_PROFILES_ACTIVE=${AMBIENTE_PIPE} bash gradlew :app:acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'app/build/reports/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Integration Test', reportTitles: 'Integration Test'])
            if (retstatus == 1){
                currentBuild.result = 'FAILURE'
                println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Acceptance_Test/')
                error("FAILURE")
            }
        }
        else {
            println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
        }
    } else {
        println ("BFF-Mobile no contiene pruebas funcionales")
    }
}
return this;
