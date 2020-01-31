def call() {
    def retstatus = sh(script: 'set +x;bash gradlew test -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'app/build/reports/test', reportFiles: 'index.html', reportName: 'HTML Report Unit Test', reportTitles: 'Unit Test'])
    if (retstatus == 1){
        currentBuild.result = 'FAILURE'
        println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Unit_Test/')
        error("FAILURE")
    }
}
return this;
