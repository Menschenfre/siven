def upload(String name, String version, String registry) {
  def registryUrl, registryUser
  switch(registry){
    case "repository.bci.cl:8323/tmp_prodlocal":
      registryUrl = "http://repository.bci.cl:8323"
      registryUser = "nexus-dev-admin"
    break
    case "repository.bci.cl:8089/reg_prod":
      registryUrl = "http://repository.bci.cl:8089"
      registryUser = "nexus-dev-admin"
      registry = registry + "/" + env.OC_VALUE
    break
    case "bcirg3dsrcnr001.azurecr.io":
      registryUrl = "http://bcirg3dsrcnr001.azurecr.io"
      registryUser = "AZURE_REGISTRY_DSR"
      registry = registry + "/" + env."${SPACE}_NAMESPACE_VALUE"
    break
    case "bcirg3crtcnr001.azurecr.io":
      registryUrl = "http://bcirg3crtcnr001.azurecr.io"
      registryUser = "AZURE_REGISTRY_CRT"
      registry = registry + "/" + env."${SPACE}_NAMESPACE_VALUE"
    break
    default:
      error('No registry defined')
    break
  }
  docker.withRegistry(registryUrl, registryUser) {
    def image = docker.image("$registry/$name:$version")
    image.push()
  }
}

def uploadbx(String name, String version, String registry){
  def image = docker.image("$registry/$name:$version")
  image.push()
}

def push2prod(String name, String version){
  sh "docker pull repository.bci.cl:8323/tmp_prodlocal/$name:$version"

  sh "docker tag repository.bci.cl:8323/tmp_prodlocal/$name:$version repository.bci.cl:8089/reg_prod/${OC_VALUE}/$name:$version"
  upload(name, version, "repository.bci.cl:8089/reg_prod")
}

def deleteTMPImages(String name, String version) {
  println "Borrando imagenes temporales de nexus"
  sh''' set +x
    /opt/tools/nexus-cli/nexus-cli configure << EOF
http://172.16.98.167:8081
k8s-tmp
${NEXUS_USR}
${NEXUS_PSW}
EOF
  '''
  eliminarImagenes(name, version, "tmp_prodlocal")
}

def eliminarImagenes(String name, String version, String repositorio){
  def listado = sh(script: "set +x;/opt/tools/nexus-cli/nexus-cli image tags -name $repositorio/$name | grep $version", returnStdout: true).split()
  for (i = 0; i < listado.length; i++) {
    def listadoIndex = listado[i]
    sh "/opt/tools/nexus-cli/nexus-cli image delete -name $repositorio/$name -tag $listadoIndex"
  }
}

return this;
