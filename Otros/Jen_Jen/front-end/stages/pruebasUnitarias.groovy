def call() {
  println "Generando imagen para pruebas unitarias"
  def content = "ARG parent_tag\n"
  content += "ARG project_name\n"
  content += "FROM \${project_name}/npm_install:\${parent_tag}\n"
  content += "WORKDIR /usr/src/app\n"
  content += "COPY . /usr/src/app\n"

  sh 'sudo rm -rf ${WORKSPACE}/dist/qa'

  if (usesJEST()){
    content += "CMD [ \"npm\", \"run\", \"coverage\" ]\n"
  } else {
    content += "CMD [ \"ng\", \"test\", \"--watch=false\", \"--code-coverage\", \"--single-run=true\" ]\n"
  }
  writeFile file: 'Dockerfile_test', text: content
  sh ''' set +x
    #Creamos directorio y dockerfile para imagenes previas
    checksum_packagejson=$(sha1sum package.json | awk '{print $1 }')
    docker build -f Dockerfile_test -t ${PROJECT_NAME}/ng_test:${VERSION_DESPLIEGUE} --build-arg parent_tag=${checksum_packagejson} --build-arg project_name="${PROJECT_NAME}" .

    docker rm ${PROJECT_NAME}_ng_test_${VERSION_DESPLIEGUE}_c || true

    mkdir -p $(pwd)/junitResults
    sudo rm -Rf $(pwd)/junitResults/*
    mkdir -p $(pwd)/coverage
    sudo rm -Rf $(pwd)/coverage/*
    mkdir -p dist
    sudo rm -Rf $(pwd)/dist/*

    docker run -v $(pwd)/junitResults:/usr/src/app/junitResults -v $(pwd)/coverage:/usr/src/app/coverage --name ${PROJECT_NAME}_ng_test_${VERSION_DESPLIEGUE}_c ${PROJECT_NAME}/ng_test:${VERSION_DESPLIEGUE}

  '''
}

def usesJEST() {
  def object = readJSON file:'package.json'

  if (object.scripts.test.contains("jest")) {
    return true
  } else {
    return false
  }
}

return this;
