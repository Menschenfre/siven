def call() {
    sh (script: 'sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties', returnStdout: false)
	def clientName = sh (script: 'set +x;find . -iname *Client | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
	def serverName = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
    def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle test -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
	publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: clientName+'/build/reports/tests/test/', reportFiles: 'index.html', reportName: 'HTML Report Client Unit Test', reportTitles: 'Unit Test'])
	publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: serverName+'/build/reports/tests/test/', reportFiles: 'index.html', reportName: 'HTML Report Server Unit Test', reportTitles: 'Unit Test'])
	if (retstatus == 1){
		currentBuild.result = 'FAILURE'
		println('Reportes de pruebas unitarias Client: '+env.BUILD_URL+'HTML_Report_Client_Unit_Test/')
		println('Reportes de pruebas unitarias Server: '+env.BUILD_URL+'HTML_Report_Server_Unit_Test/')
		error("FAILURE")
	}
}
return this;
