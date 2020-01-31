def call() {   
    println "Checkout branch "
    checkout scm

  sh '''
    projectName=${PROJECT_NAME}-${BRANCH_NAME}
    echo "projecName: " $projectName
      
    echo "********** CREAR PROYECTO DTP **********"    
    /opt/tools/jtest/creaProyectoDTP.sh $projectName
      
    echo "Tipo Componente: " ${TIPO_COMPONENTE}
    
    echo "********** ANALISIS CON JTEST Y PUBLICACION EN DTP **********"
    if [[ "${TIPO_COMPONENTE}" == "ms" ]]; then
        echo "********** MICROSERVICO **********"
        rm -rf src-org
        mv src src-org        
        java -jar /opt/tools/jtest/Lombok/lombok.jar delombok src-org -d src
        
        /opt/gradle/gradle-4.1/bin/gradle jtest -I /opt/tools/jtest/jtest/integration/gradle/init.gradle -Pjtest.excludes=path:**/src/acceptance-test/**,path:**/src/test/** -Pjtest.config="jtest.dtp://BCI_BaseLine" -Pproperty.dtp.project=$projectName
      
        rm -rf src-org
    elif [[ "${TIPO_COMPONENTE}" == "ig" ]]; then
        echo "********** SERVICIO INTEGRACION **********"
        sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties

        /opt/gradle/gradle-4.1/bin/gradle clean-codegen  jtest -I /opt/tools/jtest/jtest/integration/gradle/init.gradle -Pjtest.excludes=path:**/*-api/**,path:**/*-client/** -Pjtest.config="jtest.dtp://BCI_BaseLine" -Pproperty.dtp.project=$projectName
    fi

    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS MS-IG **********"
    java -jar /opt/tools/jtest/ValidaSeveridades.jar "/opt/tools/jtest/rangos.xml" "/build/reports/jtest/report.xml" > check-dtp.txt

  '''

    def resultadoDTP = sh (script: 'set -x;cat check-dtp.txt', returnStdout: true).trim().toString()
    println "RESULTADO DTP: " + resultadoDTP
    if (resultadoDTP.contains("ERROR:")) {  
        println "Favor revisar el proyecto [" + projectName + "] en DTP."   
        error ("Failure")
    }  
}

def android() {   
    println "Checkout branch "
    checkout scm

  sh ''' set -x
    projectName=${PROJECT_NAME}-${BRANCH_NAME}
    echo "projecName: " $projectName
    echo "workspace: " ${WORKSPACE}
      
    echo "********** CREAR PROYECTO DTP **********"    
    /opt/tools/DTP_engines/creaProyectoDTP-Android.sh $projectName
    
    export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk
    export PATH=$JAVA_HOME:$PATH
    
    echo "Tipo Componente: " ${TIPO_COMPONENTE}
    
    echo "********** COMPILAR APP **********"
    if [[ ${DIR_EXTRA} == '/app' ]]; then
      bash gradlew -Dhttps.proxyHost=172.16.98.245 -Dhttps.proxyPort=3128 clean assembleIntegration --stacktrace
    else 
      bash gradlew -Dhttps.proxyHost=172.16.98.245 -Dhttps.proxyPort=3128 clean build -x test --stacktrace
    fi

    echo "********** ANALISIS CON LINT **********"
    bash gradlew task lint
    
    echo "********** PUBLICACION EN DTP **********"
    /opt/tools/DTP_engines/parasofttestcli.sh -Dsettings="/opt/tools/DTP_engines/etc/default-settings-android.properties" -Ddata.dir="${WORKSPACE}" -Dresults.file=${WORKSPACE}${DIR_EXTRA}"/build/reports/lint-results.xml"
    
    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS ANDROID **********"
    java -jar /opt/tools/jtest/ValidaSeveridades.jar "/opt/tools/jtest/rangos.xml" "/reports/report.xml" > check-dtp.txt
    
  '''
    def resultadoDTP = sh (script: 'set -x;cat check-dtp.txt', returnStdout: true).trim().toString()
    println "RESULTADO DTP: " + resultadoDTP
    if (resultadoDTP.contains("ERROR:")) {  
        println "Favor revisar el proyecto [" + projectName + "] en DTP."   
        error ("Failure")
    }    
}

def ios(Object common) {   
    env.BRANCH_NAME = common.branchName()
    
  sh ''' set -x
    PROJECT_NAME=$(git config --get remote.origin.url | awk -F'.' '{print $2}' | awk -F'/' '{print $2}')
    projectName=${PROJECT_NAME}-${BRANCH_NAME}
    echo "projecName: " $projectName
    echo "workspace: " ${WORKSPACE}
      
    echo "********** CREAR PROYECTO DTP **********"    
    /Users/ic/Parasoft/creaProyectoDTP-Swift.sh $projectName

    echo "********** PUBLICACION EN DTP **********"
    /Users/ic/Parasoft/Swift/multilanguage-pack.sh -Dsettings="/Users/ic/Parasoft/Swift/swift-settings.properties" -Dtool=Swiftlint -Dsource.dir="${WORKSPACE}" -Dresults.file="${WORKSPACE}/swiftlint/report.xml"

    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS iOS **********"    
    java -jar /Users/ic/Parasoft/Swift/ValidaSeveridades.jar "/Users/ic/Parasoft/Swift/rangos.xml" "/reports/swiftlint/report.xml" > check-dtp.txt
    
  '''

  def resultadoDTP = sh (script: 'set -x;cat check-dtp.txt', returnStdout: true).trim().toString()

    println "RESULTADO DTP: " + resultadoDTP
    if (resultadoDTP.contains("ERROR:")) {      
        println "Favor revisar el proyecto [" + projectName + "] en DTP."   
        error ("Failure")
    } 
}

def frontend() {
    println "Checkout branch "
    checkout scm

    sh ''' set -x
    projectName=${PROJECT_NAME}-${BRANCH_NAME}
    echo "projecName: " $projectName
    echo "workspace: " ${WORKSPACE}
      
    echo "********** CREAR PROYECTO DTP **********"    
    /opt/tools/DTP_engines/creaProyectoDTP-Angular.sh $projectName
    
    export NODEJS_HOME=/usr/local/lib/nodejs/bin
    export PATH=$NODEJS_HOME:$PATH
    
    echo "********** ANALISIS CON JSHINT **********"
    jshint --reporter=checkstyle . | tee jshint-results.xml
    
    export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk
    export PATH=$JAVA_HOME:$PATH
    
    echo "********** PUBLICACION EN DTP **********"
    /opt/tools/DTP_engines/parasofttestcli.sh -Dsettings="/opt/tools/DTP_engines/etc/default-settings-jshint.properties"  -Ddata.dir="${WORKSPACE}" -Dresults.file=${WORKSPACE}/"jshint-results.xml"

  '''
}

@NonCPS
def getProjectName() {
  def projectName = "${currentBuild.rawBuild.project.parent.name}".toLowerCase()
  return projectName
}

return this;