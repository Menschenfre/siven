def call() {
	echo 'Checking Coverage...'
	def serverName = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
	def taskExists = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle tasks --all', returnStdout: true)

	if (!taskExists.contains("jacocoTestCoverageVerification")){
		sh '''set +x
		  serverPath=$(find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g")
		  cat >> $serverPath/build.gradle <<-EOF

jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = 0.8
      }
    }
  }
}
EOF
	  '''
	}

	sh 'set +x;/opt/gradle/gradle-4.1/bin/gradle jacocoTestCoverageVerification'

	def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle test jacocoTestReport -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
	publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: serverName+'/build/jacocoHtml/', reportFiles: 'index.html', reportName: 'HTML Report Coverage Test', reportTitles: 'Coverage Test'])
	if (retstatus == 1){
		currentBuild.result = 'FAILURE'
		println('Reportes de pruebas de cobertura: '+env.BUILD_URL+'HTML_Report_Coverage_Test/')
		error("FAILURE")
	}
}
return this;
