def call() {

    sh (script: 'sed -i -e "s|\\.\\.|${JENKINS_HOME}/scp|g" build.properties', returnStdout: false)
    sh ''' set +x
  /opt/gradle/gradle-4.1/bin/gradle clean-codegen --no-daemon -PmavenUser=${NEXUS_USR} -PmavenPass=${NEXUS_PSW}
    /opt/gradle/gradle-4.1/bin/gradle install --no-daemon -PmavenUser=${NEXUS_USR} -PmavenPass=${NEXUS_PSW}

    set +e
    taskExists=$(/opt/gradle/gradle-4.1/bin/gradle tasks --all -PmavenUser=${NEXUS_USR} -PmavenPass=${NEXUS_PSW} | grep uploadArchives)
    set -e
    if [[ ! -z ${taskExists} ]]; then
      /opt/gradle/gradle-4.1/bin/gradle upload --no-daemon -PmavenUser=${NEXUS_USR} -PmavenPass=${NEXUS_PSW}
    else
      echo "No existe metodo para hacer upload a nexus del jar generado"
      exit -1
    fi

    /opt/gradle/gradle-4.1/bin/gradle -x test build --no-daemon -PmavenUser=${NEXUS_USR} -PmavenPass=${NEXUS_PSW}

  cd $(find . -type d -iname *Server)
    chmod +x build/libs/*.jar
    echo "*************************************************************************************"
    echo "jar file generated in custom workspace ${WORKSPACE}/build/libs"
    jarGenerado=$(ls build/libs/*.jar)
    echo "CHECKSUM JAR GENERADO -> $(sha1sum "${jarGenerado}" | awk '{print $1 }')"
    '''
}
return this;
