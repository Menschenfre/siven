def ejecucion(Object ambiente, Object common){
    try {
      azure(ambiente)
    }
    catch(Exception err) {
      println err
      error ("error en pruebas funcionales azure")
    }
}  

def azure(Object ambiente) {
  println "**********Pruebas de aceptacion en ambiente azure**********"
  env.AMBIENTE_PIPE='integracion'
  env.ZUUL_SERVER_URI='api-dsr01.bci.cl'
  env.MS_VERSION=env.VERSION_COMPONENTE
  if (ambiente == "qa"){
      env.AMBIENTE_PIPE='qa'
      env.ZUUL_SERVER_URI='api-crt01.bci.cl'
  }
  sh (script: 'sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties', returnStdout: false)

  def serverName = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
	def taskExists = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle tasks --all', returnStdout: true)

  if (taskExists.contains("acceptanceTest")){
    def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: serverName+'build/reports/tests/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Integration Test', reportTitles: 'Integration Test'])

    if (retstatus == 1){
      currentBuild.result = 'FAILURE'
	    println('Reportes de pruebas funcionales: '+env.BUILD_URL+'HTML_Report_Integration_Test/')
	    error("FAILURE")
    }
	} else {
		println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
	}
}

def bluemix(Object ambiente) {
  println "**********Pruebas de aceptacion en ambiente bluemix**********"
  env.AMBIENTE_PIPE='integracion'
  env.ZUUL_SERVER_URI='bci-api-ic01.us-south.containers.mybluemix.net'
  env.MS_VERSION=env.VERSION_COMPONENTE
  if (ambiente == "qa"){
      env.AMBIENTE_PIPE='qa'
      env.ZUUL_SERVER_URI='bci-api-cer01.us-south.containers.mybluemix.net'
  }
  sh (script: 'sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties', returnStdout: false)

  def serverName = sh (script: 'set +x;find . -iname *Server | sed -e "s/\\///g" -e "s/\\.//g"', returnStdout: true).trim()
  def taskExists = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle tasks --all', returnStdout: true)

  if (taskExists.contains("acceptanceTest")){
    def retstatus = sh(script: 'set +x;/opt/gradle/gradle-4.1/bin/gradle acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: serverName+'build/reports/tests/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Integration Test', reportTitles: 'Integration Test'])

    if (retstatus == 1){
      //currentBuild.result = 'FAILURE'
      println('Reportes de pruebas funcionales: '+env.BUILD_URL+'HTML_Report_Integration_Test/')
      error("FAILURE")
    }
  } else {
    println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
  }
}

return this;
