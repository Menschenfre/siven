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
        #/opt/gradle/gradle-4.1/bin/gradle build

        /opt/gradle/gradle-4.1/bin/gradle clean-codegen  jtest -I /opt/tools/jtest/jtest/integration/gradle/init.gradle -Pjtest.excludes=path:**/*-api/**,path:**/*-client/** -Pjtest.config="jtest.dtp://BCI_BaseLine" -Pproperty.dtp.project=$projectName
    fi

    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS MS-IG **********"
    java -jar /opt/tools/jtest/ValidaSeveridades.jar "/opt/tools/jtest/rangos.xml" "/build/reports/jtest/report.xml" > check-dtp.txt

  '''

  def resultadoDTP = sh (script: 'set -x;cat check-dtp.txt', returnStdout: true).trim().toString()

    if (!resultadoDTP.contains("OK")) {
        println "RESULTADO DTP: " + resultadoDTP
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
      /opt/gradle/gradle-4.4/bin/gradle -Dhttps.proxyHost=172.16.98.245 -Dhttps.proxyPort=3128 clean assembleIntegration --stacktrace
    else 
      /opt/gradle/gradle-4.4/bin/gradle -Dhttps.proxyHost=172.16.98.245 -Dhttps.proxyPort=3128 clean build -x test --stacktrace
    fi

    echo "********** ANALISIS CON LINT **********"
    /opt/gradle/gradle-4.4/bin/gradle task lint
    
    echo "********** PUBLICACION EN DTP **********"
    /opt/tools/DTP_engines/parasofttestcli.sh -Dsettings="/opt/tools/DTP_engines/etc/default-settings-android.properties" -Ddata.dir="${WORKSPACE}" -Dresults.file=${WORKSPACE}${DIR_EXTRA}"/build/reports/lint-results.xml"
    
    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS ANDROID **********"
    java -jar /opt/tools/jtest/ValidaSeveridades.jar "/opt/tools/jtest/rangos.xml" "/reports/report.xml" > check-dtp.txt
    
  '''
    def resultadoDTP = sh (script: 'set -x;cat check-dtp.txt', returnStdout: true).trim().toString()

    if (!resultadoDTP.contains("OK")) {
        println "RESULTADO DTP: " + resultadoDTP
        error ("Failure")
    }    
}

def ios() {   
    println "Checkout branch "
    checkout scm

  sh ''' set -x
    projectName=${PROJECT_NAME}-${BRANCH_NAME}
    echo "projecName: " $projectName
    echo "workspace: " ${WORKSPACE}
      
    echo "********** CREAR PROYECTO DTP **********"    
    /Users/ic/Parasoft/creaProyectoDTP-Swift.sh $projectName


    echo "********** PUBLICACION EN DTP **********"
    /Users/ic/Parasoft/Swift/multilanguage-pack.sh -Dsettings="/Users/ic/Parasoft/Swift/swift-settings.properties" -Dtool=Swiftlint -Dsource.dir="${WORKSPACE}" -Dresults.file="${WORKSPACE}/swiftlint/report.xml"


    echo "********** VALIDACION CUMPLIMIEMTO DE REGLAS iOS **********"
    #java -jar /opt/tools/jtest/ValidaSeveridades.jar "/opt/tools/jtest/rangos.xml" "/reports/report.xml" > check-dtp.txt
    
  '''
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
return this;