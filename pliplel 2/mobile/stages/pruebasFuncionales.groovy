def ejecucion(Object ambiente, Object common){
    try {
      azure(ambiente)
    }
    catch(Exception err) {
      println err
      error ("error en pruebas funcionales azure")
    }
}  

def bluemix(Object ambiente) {
    println "**********Pruebas de aceptacion en ambiente bluemix**********"
    env.AMBIENTE_PIPE='integracion'
    env.ZUUL_SERVER_URI='bci-api-ic01.us-south.containers.mybluemix.net'
    env.MS_VERSION=env.VERSION_COMPONENTE
    env.HOST_MICROSERVICIO='https://bci-api-ic01.us-south.containers.mybluemix.net'
    env.PORT='443'
    env.CONTEXT_PATH=env.BASE_HREF
    env.AMBIENTE='ic'
    if (ambiente == "qa"){
        env.AMBIENTE_PIPE='qa'
        env.ZUUL_SERVER_URI='bci-api-cer01.us-south.containers.mybluemix.net'
        env.HOST_MICROSERVICIO='https://bci-api-cer01.us-south.containers.mybluemix.net'
        env.AMBIENTE='qa'
    }
    if (env.PROJECT_NAME != 'bff-mobile'){
        def taskExists = sh(script: 'set +x;bash gradlew tasks --all', returnStdout: true)
        if (taskExists.contains("acceptanceTest")){
            def retstatus = sh(script: 'set +x;SPRING_PROFILES_ACTIVE=${AMBIENTE_PIPE} bash gradlew :app:acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'app/build/reports/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Integration Test', reportTitles: 'Integration Test'])
            if (retstatus == 1){
                //currentBuild.result = 'FAILURE'
                println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Acceptance_Test/')
                error("FAILURE")
            }
        }
        else {
            println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
        }
    } else {
        println ("BFF-Mobile no contiene pruebas funcionales")
    }
}

def azure(Object ambiente) {
    println "**********Pruebas de aceptacion en ambiente azure**********"
    env.AMBIENTE_PIPE='integracion'
    env.ZUUL_SERVER_URI='api-dsr01.bci.cl'
    env.MS_VERSION=env.VERSION_COMPONENTE
    env.HOST_MICROSERVICIO='http://api-dsr01.bci.cl'
    env.PORT='80'
    env.CONTEXT_PATH=env.BASE_HREF
    env.AMBIENTE='ic'
    if (ambiente == "qa"){
        env.AMBIENTE_PIPE='qa'
        env.ZUUL_SERVER_URI='api-crt01.bci.cl'
        env.HOST_MICROSERVICIO='http://api-crt01.bci.cl'
        env.AMBIENTE='qa'
    }
    if (env.PROJECT_NAME != 'bff-mobile'){
        def taskExists = sh(script: 'set +x;bash gradlew tasks --all', returnStdout: true)
        if (taskExists.contains("acceptanceTest")){
            def retstatus = sh(script: 'set +x;SPRING_PROFILES_ACTIVE=${AMBIENTE_PIPE} bash gradlew :app:acceptanceTest -Dfile.encoding=ISO-8859-1 --no-daemon', returnStatus: true)
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'app/build/reports/acceptanceTest', reportFiles: 'index.html', reportName: 'HTML Report Integration Test', reportTitles: 'Integration Test'])
            if (retstatus == 1){
                currentBuild.result = 'FAILURE'
                println('Reportes de pruebas unitarias: '+env.BUILD_URL+'HTML_Report_Acceptance_Test/')
                error("FAILURE")
            }
        }
        else {
            println("No existe la tarea acceptanceTest en el proyecto, ignoramos pruebas funcionales")
        }
    } else {
        println ("BFF-Mobile no contiene pruebas funcionales")
    }
}
return this;
