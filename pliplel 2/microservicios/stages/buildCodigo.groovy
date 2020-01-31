def call() {

  sh ''' set +x
  /opt/gradle/gradle-4.1/bin/gradle build -x test -Dfile.encoding=ISO-8859-1 --no-daemon

  chmod +x ${WORKSPACE}/build/libs/*.jar
  echo "*************************************************************************************"
  echo "jar file generated in custom workspace ${WORKSPACE}/build/libs"
  jarGenerado=$(ls ${WORKSPACE}/build/libs/*.jar)
  echo "CHECKSUM JAR GENERADO -> $(sha1sum "${jarGenerado}" | awk '{print $1 }')"
  '''
}
return this;
