def call() {
  sh ''' set +x
    checksum_packagejson=$(sha1sum package.json | awk '{print $1 }')
    docker build -f Dockerfile_test -t ${PROJECT_NAME}/ng_test:${BRANCH_NAME} --build-arg parent_tag=${checksum_packagejson} --build-arg project_name="${PROJECT_NAME}" .
    set +e
    docker rm ${PROJECT_NAME}_ng_cucumber_test_${BRANCH_NAME}_c
    set -e
    # TODO: quitar sudo
    mkdir -p $(pwd)/junitResults
    sudo rm -Rf $(pwd)/junitResults/*
    # TODO: quitar sudo
    mkdir -p $(pwd)/coverage
    sudo rm -Rf $(pwd)/coverage/*
    mkdir -p dist
    sudo rm -Rf $(pwd)/dist/*
    #set +e
    mkdir -p dist_cucumber
    sudo rm -Rf $(pwd)/dist_cucumber/*
    docker run -v $(pwd)/junitResults:/usr/src/app/junitResults -v $(pwd)/coverage:/usr/src/app/coverage --name ${PROJECT_NAME}_ng_cucumber_test_${BRANCH_NAME}_c ${PROJECT_NAME}/ng_test:${BRANCH_NAME} /bin/bash -c "ng e2e --webdriver-update false --output-path dist_cucumber"
    echo "cucumber tests = $?"
    #set -e
  '''
}
return this;
