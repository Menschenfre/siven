def invocar() {
  modificarPathBFF()
  ejecutarPruebas()
}

def ejecutarPruebas(){
  println "Generando imagen para pruebas unitarias"
  def content = "ARG project_name\n"
  content += "FROM \${project_name}/npm_preinstall:latest\n"
  content += "WORKDIR /usr/src/app\n"
  content += "COPY . /usr/src/app\n"
  content += "RUN npm install --silent\n"
  content += "RUN npm run compile\n"
  content += "CMD [ \"npm\", \"test\" ]\n"
  writeFile file: 'Dockerfile_test', text: content
  sh ''' set +x
    checksum_packagejson=$(sha1sum package.json | awk '{print $1 }')
    docker build -f Dockerfile_test -t ${PROJECT_NAME}/ng_test:${BRANCH_NAME} --build-arg parent_tag=${checksum_packagejson} --build-arg project_name="${PROJECT_NAME}" .

    docker rm ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c || true

    mkdir -p $(pwd)/junitResults
    sudo rm -Rf $(pwd)/junitResults/* || true
    mkdir -p $(pwd)/coverage
    sudo rm -Rf $(pwd)/coverage/* || true
    mkdir -p dist
    sudo rm -Rf $(pwd)/dist/* || true

    docker run -v $(pwd)/junitResults:/usr/src/app/junitResults -v $(pwd)/coverage:/usr/src/app/coverage --name ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c ${PROJECT_NAME}/ng_test:${BRANCH_NAME}
    echo "unit tests = $?"

    docker rm -f ${PROJECT_NAME}_ng_test_${BRANCH_NAME}_c
    docker rmi -f ${PROJECT_NAME}/ng_test:${BRANCH_NAME}
  '''
}

def modificarPathBFF(){
  println "Modificando el path del BFF"
  def apiJsonPath = "server/common/swagger/Api.json"
  def apiYamlPath = "server/common/swagger/Api.yaml"
  def pathOriginal, pathModificado
  if(encontrarArchivoApi(apiJsonPath)){
    def json = readJSON file: apiJsonPath
    pathOriginal = json.basePath
  } else if(encontrarArchivoApi(apiYamlPath)){
    def yaml = readYaml file: apiYamlPath
    pathOriginal = yaml.basePath
  } else {
    error("No se encuentra archivo Api.json o Api.yaml")
  }

  if (env.VISIBILITY == "False"){
    pathModificado = "/bff/priv/"+env.DEPLOY_NAME
  } else {
    pathModificado = "/bff/"+env.DEPLOY_NAME
  }

  editarArchivos(pathOriginal, pathModificado)
}

def encontrarArchivoApi(String pathApi){
    def api = findFiles(glob: pathApi)
    if(api.size() == 1){
        return true
    } else {
        return false
    }
}

def editarArchivos(Object reemplazar, Object variable){
  def archivos = sh (script: "grep -rl $reemplazar .", returnStdout: true).trim().split("\n")
  for(String archivo: archivos){
    if(!archivo.contains("coverage")){
      def datos = readFile archivo
      datos = datos.replace(reemplazar, variable)
      writeFile file: archivo, text: datos
    }
  }
}
return this;