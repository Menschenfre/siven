def call() {
  echo 'Checking Coverage...'
  def coverageFile = "${env.WORKSPACE}" + "/coverage.gradle"
    if(fileExists(coverageFile)){
        echo 'Cambiando limite de cobertura en archivo coverage.gradle ...'
        sh "set +x;sed -i 's/minimum.*/minimum = 0.85/g' coverage.gradle"
        sh '''set +x
        cat >> build.gradle <<-EOF

jacocoTestCoverageVerification {
    violationRules {
        rule {
            enabled = true
            limit {
                counter = 'BRANCH'
                value = 'COVEREDRATIO'
                minimum = 0.85
            }
        }
        rule {
            enabled = true
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.85
            }
        }
    }
}
EOF'''
        sh 'set +x;/opt/gradle/gradle-4.1/bin/gradle clean build jacocoTestReport -Dfile.encoding=ISO-8859-1 --no-daemon'
        def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle jacocoTestCoverageVerification', returnStatus: true)
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'build/jacocoHtml/', reportFiles: 'index.html', reportName: 'HTML Report Coverage Test', reportTitles: 'Coverage Test'])
  
        if (retstatus == 1){
            currentBuild.result = 'FAILURE'
            println('Reportes de pruebas de cobertura: '+env.BUILD_URL+'HTML_Report_Coverage_Test/')
            error("FAILURE")
        }
    }else{
        currentBuild.result = 'FAILURE'
        error('Se requiere el archivo coverage.gradle para las pruebas de cobertura, por favor verificar su existencia.')
    }
}

return this;
