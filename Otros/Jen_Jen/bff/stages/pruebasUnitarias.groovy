def call() {
    sh ''' set +x
    #Creamos Dockerfile_test
    cat > Dockerfile_test <<-EOF
ARG project_name
FROM \\${project_name}/npm_preinstall:latest

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN npm install
RUN npm run compile
CMD ["npm", "test"]
EOF

    checksum_packagejson=$(sha1sum package.json | awk '{print $1 }')
    docker build -f Dockerfile_test -t ${PROJECT_NAME}/ng_test:${BRANCH_NAME} --build-arg parent_tag=${checksum_packagejson} --build-arg project_name="${PROJECT_NAME}" .


    set +e
    docker rm ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c
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
    echo TO-DO: quitar el set +e y el set -e al final. Esta para permitir temporalmente continuar con test unitarios y funcionales rotos
    # corre unit tests
    docker run -v $(pwd)/junitResults:/usr/src/app/junitResults -v $(pwd)/coverage:/usr/src/app/coverage --name ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c ${PROJECT_NAME}/ng_test:${BRANCH_NAME}
    echo "unit tests = $?"

    docker rm -f ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c
    docker rmi -f ${PROJECT_NAME}/ng_test:${BRANCH_NAME}
    '''
}
return this;
