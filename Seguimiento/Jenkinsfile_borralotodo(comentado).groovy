node('master') {
//Se define la ruta de Jenkins 
    def jenkins = env.JENKINS_URL.replace("http://", "").replace(":8080/", "")
//Se define la variable ambiente como IC    
    def ambiente = "IC"
//Se define código del registry    
    def registry = "BCIRG3DSRCNR001"
//Se define variable repository como "reg_ig"    
    def repository = "reg_ic"

//Se crea la condición para jenkins de QA     
    if (jenkins.equalsIgnoreCase("172.16.98.112")){
        ambiente = "CERT"
        registry = "BCIRG3CRTCNR001"
        repository = "reg_qa"
    }
//Imprime el tipo de eliminación enviado mediante jenkins
    println "${params.TIPO_ELIMINACION}"

//Si el tipo de eliminación es igual a singular     
    if ("${params.TIPO_ELIMINACION}".equalsIgnoreCase("SINGULAR")){
      //se definen los inputs luego de ingresado el tipo de eliminación por web, ver imagen (1) en documento word
      def userInput = input(
        id: 'userInput', message: 'Ingresar valores para eliminacion:?',
        parameters: [
                string(defaultValue: 'bff-empresaslogin',
                        description: 'nombre del componente a eliminar, ej: bff-empresasgenericoms',
                        name: 'Nombre'),
                string(defaultValue: 'test-apiconnect',
                        description: 'nombre de la rama a eliminar, ej: feat-bff-base',
                        name: 'Rama'),
        ])
      // Recibe la data ingresada en los inputs, nombre y rama
      def nombre = userInput.Nombre?:''
      def rama = userInput.Rama?:''

      //Tira el componente y la rama a una variable
      def nombreComponente = nombre+"-"+rama
      def nodo = obtenerNombreNodo(ambiente, nombre)
      def mapaDatosIC
      println nombreComponente

      node (nodo){
	      stage ("Preparacion") {
          mapaDatosIC = prepararDatos(nombre, rama, repository, registry)
          input 'Se eliminaran los componentes indicados anteriormente'
	      }

	      stage ("Eliminacion") {
          eliminarDatos(mapaDatosIC, nombre, registry, repository)
          //def cluster = funciones.obtenerCluster(nombreComponente, "IC")
          build job: 'Utilitarios/eliminar_registro_ingress', parameters: [string(name: 'NOMBRE_PROYECTO', value: nombre), string(name: 'RAMA', value: rama)]
	      }
      }

    } else {
      def inputFile = input message: 'Upload file', parameters: [file(name: 'proyectos.csv')]
      new hudson.FilePath(new File("$workspace/proyectos.csv")).copyFrom(inputFile)

      def listado = readFile file:'proyectos.csv'
      String[] lineas = listado.split("\n")

      stage ("Eliminacion_masiva") {
        for (String linea : lineas) {
          String[] valores = linea.split(";")
          def nombre = valores[0].trim()
          def rama = valores[1].trim()
          def nodo = obtenerNombreNodo(ambiente, nombre)
          node (nodo){
            mapaDatos = prepararDatos(nombre, rama, repository, registry)
            eliminarDatos(mapaDatos, nombre, registry, repository)
            //def cluster = funciones.obtenerCluster(nombre+"-"+rama, "IC")
            build job: 'Utilitarios/eliminar_registro_ingress', parameters: [string(name: 'NOMBRE_PROYECTO', value: nombre), string(name: 'RAMA', value: rama)]
          }
        }
      }
    }
}

def eliminarDatos(Object mapaDatos, Object nombre, Object registry, Object repository){
    try {
      sh '''
        if [[ ! -z "'''+mapaDatos.componente+'''" ]] && [[ ! -z "'''+mapaDatos.service+'''" ]]; then
          kubectl -n '''+obtenerNamespace(mapaDatos.componente)+''' delete deploy/'''+mapaDatos.componente+''' svc/'''+mapaDatos.service+'''
        fi
        '''
    } catch (Exception err){
      println err
    }
    try {
      def listado = mapaDatos.image.split("\n")
      for (String tag:listado){
      	sh "az acr repository delete -n "+registry+" --image "+repository+"/"+nombre+":"+tag+" --yes"
      }
    } catch (Exception err){
      println err
   }
} 

def prepararDatos(Object nombre, Object rama, Object repository, Object registry){
    def componente = deployName(nombre+"-"+rama)
    def service = componente+"-svc"
    def image = obtenerImagenes(nombre, rama, repository, registry)

    if (componente?.trim()) {
      echo "Componente -> "+componente
    }
    if (service?.trim()) {
      echo "Service -> "+service
    }
    if (image?.trim()) {
      echo "Imagen -> "+image
    }

    def sampleMap = [componente:deployName(nombre+"-"+rama), service:componente+"-svc", image:image]
    return sampleMap
}

def obtenerImagenes(Object nombre, Object rama, Object repository, Object registry){
    if (repository != "reg_ic"){
        rama = rama + "-"
    }
    def imagenes = sh (script: '''az acr repository show-tags --name '''+registry+''' --repository '''+repository+"/"+nombre+''' -o table | grep '''+rama, returnStdout: true).trim()
    
    return imagenes
}

//Función para obtener el nombre del nodo, recibe 2 objetos
def obtenerNombreNodo(Object nodo, Object nombre){
  //Se le agrega -FRONT a los fe
  if(nombre.startsWith("fe-")){
    nodo = nodo+"-FRONT"
  //Se le agrega -API a los demás  
  } else if(nombre.startsWith("ms-") || nombre.startsWith("ig-") || nombre.startsWith("bff-") || nombre.startsWith("api-")){
    nodo = nodo+"-API"
  }

  def nombreNodo
  switch(nodo){
    //IC AZURE
    case "IC-API":
      nombreNodo = 'Slave Azure Deploy DSR'
    break
    case "IC-FRONT":
      nombreNodo = 'Slave Azure Deploy DSR FRONT'
    //CERT AZURE
    break
    case "CERT-API":
      nombreNodo = 'Slave Azure Deploy QA'
    break
    case "CERT-FRONT":
      nombreNodo = 'Slave Azure Deploy QA FRONT'
    default:
      error('Espacio '+nodo+' no definido en pipeline')
    break
  }

  if (Jenkins.instance.getNode(nombreNodo).toComputer().isOnline()){
    //Nodo esta online
    return nombreNodo
  } else {
    error("Nodo "+nombreNodo+" se encuentra offline, contactarse con el equipo devops")
  }
}

def obtenerNamespace(Object nombreComponente){
  if(nombreComponente.startsWith("ig-")){
    return "bci-integ"
  } else if(nombreComponente.startsWith("fe-")){
    return "bci-front"
  } else {
    return "bci-api"
  }
}

def deployName(deployRama){
  println "Obteniendo el nombre del despliegue"
  //Largo permitido en kubernetes para el nombre del deploy es de 55 caracteres, por lo tanto tenemos que cortarlo en caso de que exceda esa cantidad
  def deployName = deployRama
  deployName = deployName.take(55).toLowerCase()
  if (deployName.endsWith("-")){
    deployName = deployName.take(54)
  }
  return deployName
}