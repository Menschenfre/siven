def generarPreinstall() {
  println "Generando imagen npm_preinstall"
  def registry = registryAzure(env.SPACE)
  dir('bff_docker_npm_preinstall') {
    def content = "FROM "+registry+"/chromium-xvfb-js:8\n"
    content += "RUN npm config set fetch-retry-maxtimeout 10000\n"
    content += "RUN npm config set loglevel verbose\n"
    content += "WORKDIR /usr/src/app\n"
    content += "RUN echo 'Acquire { http::User-Agent \"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36\"; };' >> /etc/apt/apt.conf\n"
    content += "RUN apt-get update;export no_proxy=\"localhost, repository.bci.cl\";apt-get install -y --force-yes git unzip wget libgconf-2-4;wget https://chromedriver.storage.googleapis.com/2.28/chromedriver_linux64.zip;unzip chromedriver_linux64.zip;mv chromedriver chromedriver2_28\n"
    writeFile file: 'Dockerfile', text: content

    def conteoImagenes = sh (script: 'docker images --format "{{.ID}}" ${PROJECT_NAME}/npm_preinstall:latest | wc -l', returnStdout: true).trim()
    if (conteoImagenes == "0"){
      sh "docker build -t ${PROJECT_NAME}/npm_preinstall:latest ."
    }
  }
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