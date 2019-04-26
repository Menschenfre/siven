def call() {
    sh ''' set +x
        #Creamos archivos Dockerfile para imagenes previas
        set +e
        mkdir bff_docker_npm_preinstall
        cat > bff_docker_npm_preinstall/Dockerfile <<-EOF
FROM markadams/chromium-xvfb-js:8
RUN npm config set fetch-retry-maxtimeout 10000
RUN npm config set loglevel verbose
WORKDIR /usr/src/app
RUN echo 'Acquire { http::User-Agent "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36"; };' >> /etc/apt/apt.conf
RUN apt-key update; apt-get update;export no_proxy="localhost, repository.bci.cl";apt-get install -y --force-yes unzip wget libgconf-2-4;wget https://chromedriver.storage.googleapis.com/2.28/chromedriver_linux64.zip;unzip chromedriver_linux64.zip;mv chromedriver chromedriver2_28
EOF
        set -e
        # Reconstruir imagen de preinstall (npm install -g @angular/cli)
        # TODO: reactivar esto por si cambia el preinstall dentro de package.json. Actualmente es: npm install -g @angular/cli
        cd bff_docker_npm_preinstall
        conteo_imagenes=$(docker images --format "{{.ID}}" ${PROJECT_NAME}/npm_preinstall:latest | wc -l )
        if [ ${conteo_imagenes} -eq 0 ]; then
            docker build -t ${PROJECT_NAME}/npm_preinstall:latest .
        fi
        cd ..
    '''
}
return this;