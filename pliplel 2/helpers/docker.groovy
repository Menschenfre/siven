def build(String name, String version, String registry){
  def image = docker.build("$registry/$name:$version")
}

def delete(String name, String version, String registry){
  sh "docker rmi -f $registry/$name:$version"
}

def tag(String name, String version, String registryOrigen, String registryDestino){
  sh "docker tag $registryOrigen/$name:$version $registryDestino/$name:$version"
}

return this;
