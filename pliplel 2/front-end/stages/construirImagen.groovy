def generarPreinstall(Object angular8 = false) {
  println "Generando imagen npm_preinstall"
  def registry = registryAzure(env.SPACE)
  dir('docker_npm_preinstall') {
    def content
    if (angular8){
      content = "FROM "+registry+"/chromium-xvfb-js:10\n"
    } else {
      content = "FROM "+registry+"/chromium-xvfb-js:8\n"
      if (env.PROJECT_NAME.contains("widget")) {
        content = "FROM "+registry+"/chromium-xvfb-js:9\n"
      }
    }
    content += "RUN npm config set fetch-retry-maxtimeout 10000\n"
    content += "RUN npm config set loglevel verbose\n"
    if (angular8){
      content += "RUN npm install -g @angular/cli --unsafe-perm\n"
    } else {
      content += "RUN npm install -g @angular/cli@1.7.4 --unsafe-perm\n"
    }
    content += "WORKDIR /usr/src/app\n"
    content += "RUN echo 'Acquire { http::User-Agent \"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36\"; };' >> /etc/apt/apt.conf\n"
    content += "RUN apt-get update;export no_proxy=\"localhost, repository.bci.cl\";apt-get install -y --force-yes git unzip wget libgconf-2-4;wget https://chromedriver.storage.googleapis.com/2.28/chromedriver_linux64.zip;unzip chromedriver_linux64.zip;mv chromedriver chromedriver2_28\n"
    writeFile file: 'Dockerfile', text: content

    def conteoImagenes = sh (script: 'docker images --format "{{.ID}}" ${PROJECT_NAME}/npm_preinstall:angular_cli | wc -l', returnStdout: true).trim()
    if (conteoImagenes == "0"){
        sh "docker build --no-cache -t ${PROJECT_NAME}/npm_preinstall:angular_cli ."
    }
  }
}

def generarNpminstall(Boolean moduloAngular = false) {
  println "Generando imagen npm_install"
  dir ('docker_npm_install'){
    def content = "ARG parent_tag\n"
    content += "ARG project_name\n"
    content += "FROM \${project_name}/npm_preinstall:\${parent_tag}\n"
    content += "WORKDIR /usr/src/app\n"
    content += "COPY package.json /usr/src/app/package.json\n"
    content += "COPY .npmrc /usr/src/app/.npmrc\n"
    if (env.PROJECT_NAME == "fe-empresasBase" || env.PROJECT_NAME == "fe-empresasbase" ){
      content += "ENV NODE_OPTIONS=--max_old_space_size=4096\n"
    }
    if (moduloAngular){
      content += "RUN npm install --silent && npm run postinstall\n"
    } else {
      content += "RUN npm install --silent\n"
    }
    writeFile file: 'Dockerfile', text: content
  }
  def checksumPackagejson = sh (script: 'sha1sum package.json | awk \'{print $1 }\'', returnStdout: true).trim()
  sh "docker build --no-cache -t ${PROJECT_NAME}/npm_install:$checksumPackagejson --build-arg parent_tag=\"angular_cli\" --build-arg project_name=\"${PROJECT_NAME}\" -f docker_npm_install/Dockerfile ."
}

def generarProdNpminstall() {
  println "Generando imagen npm_install imagen productiva"
  def configBase = "/opt/kubernetes/fe-canalesconfigbase"
  def directorio
  if (env.PROJECT_NAME.contains('fe-empresas')){
    directorio = "360connect"
  } else if (env.PROJECT_NAME.contains('fe-pcomercial')){
    directorio = "bci360"
  } else {
    error ("Invocacion de pipeline no corresponde a proyectos de plataforma CANALES")
  }
  sh "git --git-dir=${configBase}/.git --work-tree=${configBase} pull"
  sh "cp ${configBase}/${directorio}/package.json packageProd.json"
  npmrcProductivo()
  def content = "ARG parent_tag\n"
  content += "ARG project_name\n"
  content += "FROM \${project_name}/npm_preinstall:\${parent_tag}\n"
  content += "WORKDIR /usr/src/app\n"
  content += "COPY . /usr/src/app/\n"
  content += "COPY packageProd.json /usr/src/app/package.json\n"
  if (env.PROJECT_NAME == "fe-empresasBase" || env.PROJECT_NAME == "fe-empresasbase" ){
    content += "ENV NODE_OPTIONS=--max_old_space_size=4096\n"
  }
  content += "RUN npm install\n"
  writeFile file: 'Dockerfile_produccion', text: content
  sh "docker build --no-cache -t ${PROJECT_NAME}/npm_prodinstall:${VERSION_DESPLIEGUE} --build-arg parent_tag=\"angular_cli\" --build-arg project_name=\"${PROJECT_NAME}\" -f Dockerfile_produccion ."
}

def npmrcProductivo(){
  println "Configurando archivo .npmrc apuntando a repository.bci.cl:8081/repository/npm-group-rf"
  def content = "registry=http://repository.bci.cl:8081/repository/npm-group-rf/\n"
  content += "//repository.bci.cl:8081/repository/npm-group-rf/:_authToken=NpmToken.8e5cd9df-2260-35f3-b086-994eb74d540a\n"
  writeFile file: '.npmrc', text: content
}

def registryAzure(String space){
  switch(space){
    case "INT":
    case "IC":
      return 'bcirg3dsrcnr001.azurecr.io/devops'
    case "CERT":
    case "PROD_LOCAL":
      return 'bcirg3crtcnr001.azurecr.io/devops'
    default:
      error('registryAzure() -> No space defined')
    break
  }
}

return this;
