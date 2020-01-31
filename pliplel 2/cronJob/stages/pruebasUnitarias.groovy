def call() {
	env.AMBIENTE_PIPE='integracion'
    env.HOST_MICROSERVICIO='http://api-dsr01.bci.cl'
	
	if (env.SPACE == "qa"){
    	env.AMBIENTE_PIPE='qa'
    	env.HOST_MICROSERVICIO='http://api-crt01.bci.cl'
    }

    def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle test -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'build/reports/tests/test/', reportFiles: 'index.html', reportName: 'HTML Report Unit Test', reportTitles: 'Unit Test'])
    if (retstatus == 1){
        currentBuild.result = 'FAILURE'
        println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Unit_Test/')
        error("FAILURE")
    }
}
return this;
