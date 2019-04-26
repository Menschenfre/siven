def call() {
    sh(script: 'set +x;echo "Ambiente -> "${AMBIENTE_PIPE}" ZUUL -> "${ZUUL_SERVER_URI}')
    def taskExists = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle tasks --all', returnStdout: true)
    if (taskExists.contains("acceptanceTest")){
        def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle clean build acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'build/reports/tests/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Acceptance Test', reportTitles: 'Acceptance Test'])
        if (retstatus == 1){
            currentBuild.result = 'FAILURE'
            println('Reportes de pruebas funcionales: '+env.BUILD_URL+'HTML_Report_Acceptance_Test/')
            error("FAILURE")
        }
    } 
    else {
        println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
    }
}
return this;

