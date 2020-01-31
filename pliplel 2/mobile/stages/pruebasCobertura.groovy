def call() {
  echo 'Checking Coverage...'
  def taskExists = sh(script: 'set +x;bash gradlew tasks --all', returnStdout: true)

  if (!taskExists.contains("jacocoTestCoverageVerification")){
    sh '''set +x
      cat >> app/build.gradle <<-EOF

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

  sh 'set +x;bash gradlew jacocoTestCoverageVerification'

  def retstatus = sh(script: 'set +x;bash gradlew test jacocoTestReport -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
  publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'app/build/reports/jacoco/test/html', reportFiles: 'index.html', reportName: 'HTML Report Coverage Test', reportTitles: 'Coverage Test'])
  if (retstatus == 1){
      currentBuild.result = 'FAILURE'
      println('Reportes de pruebas de cobertura: '+env.BUILD_URL+'HTML_Report_Coverage_Test/')
      error("FAILURE")
  }
}
return this;
