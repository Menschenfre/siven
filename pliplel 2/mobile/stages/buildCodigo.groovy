def call() {
    sh ''' set +x
    bash gradlew -x test :app:clean :app:build -Dfile.encoding=ISO-8859-1 --no-daemon

    chmod +x ${WORKSPACE}/app/build/libs/*.jar
    echo "*************************************************************************************"
    echo "jar file generated in custom workspace ${WORKSPACE}/app/build/libs"
    jarGenerado=$(ls ${WORKSPACE}/app/build/libs/*.jar)
    echo "CHECKSUM JAR GENERADO -> $(sha1sum "${jarGenerado}" | awk '{print $1 }')"
    '''
}
return this;
