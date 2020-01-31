def call() {
  sh 'bash gradlew testDebugUnitTest'
  publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'app/build/reports/tests/testDebugUnitTest/', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: ''])
}
return this;
