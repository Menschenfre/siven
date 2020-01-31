def call() {
  echo 'Checking Coverage...'
  def taskExists = sh(script: 'set +x;./gradlew tasks --all', returnStdout: true)

  if (!taskExists.contains("jacocoTestCoverageVerification")){
    sh '''set +x
      cat >> build.gradle <<-EOF

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

  def retstatus = sh(script: 'set +x;./gradlew jacocoTestCoverageVerification', returnStatus: true)
  publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'build/jacocoHtml/', reportFiles: 'index.html', reportName: 'HTML Report Coverage Test', reportTitles: 'Coverage Test'])
  if (retstatus == 1){
      currentBuild.result = 'FAILURE'
      println('Reportes de pruebas de cobertura: '+env.BUILD_URL+'HTML_Report_Coverage_Test/')
      error("FAILURE")
  }
}
return this;
