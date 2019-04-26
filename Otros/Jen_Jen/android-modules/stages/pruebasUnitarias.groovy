def call() {
  def retstatus = sh(script: 'set +x;./gradlew testDebugUnitTest', returnStatus: true)

  publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: env.DIR_EXTRA+'/build/reports/tests/testDebugUnitTest/', reportFiles: 'index.html', reportName: 'HTML Report Unit Test', reportTitles: 'Unit Test'])

  if (retstatus == 1){
    currentBuild.result = 'FAILURE'
    println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Unit_Test/')
    error("FAILURE")
  }
}
return this;
