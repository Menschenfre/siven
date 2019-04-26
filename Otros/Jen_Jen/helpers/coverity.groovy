def call() {
  checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'jenkins-shared']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git']]])

  sh ''' set +x
    echo "**********Creando Stream Coverity**********"
    streamName=${PROJECT_NAME}-${BRANCH_NAME}
    covHost="161.131.140.253"
    iDir=${WORKSPACE}/cov-idir

    set +e
    /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-manage-im --host ${covHost} --mode streams --add --set name:${streamName}
    /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-manage-im --host ${covHost} --mode projects --update --name ${projectName} --insert stream:${streamName}
    set -e

    if [[ "${nombrePipeline}" == "Microservicios" ]]; then
      /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-build --dir ${iDir} bash /opt/gradle/gradle-4.1/bin/gradle clean compileJava
    elif [[ "${nombrePipeline}" == "Android" ]]; then
      /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-build --dir ${iDir} bash gradlew clean assembleIntegration
    fi

    /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-analyze --dir ${iDir} --distrust-all --webapp-security --strip-path=${WORKSPACE}
    /opt/tools/cov-analysis-linux64-2017.07-SP2/bin/cov-commit-defects --dir ${iDir} --host ${covHost} --port 8080 --stream ${streamName}

    rm -rf ${iDir}

    # java -jar "jenkins-shared/coverity/cov-analysis-check.jar" --host ${covHost} --user ${COV_USER} --password ${COVERITY_PASSPHRASE} --rules jenkins-shared/coverity/security-quality.json --pretty --streams ${streamName}
  '''
}
return this;
