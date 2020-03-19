node('master') {
	//echo "Funcionó!";
	//Imprime el tipo de eliminación enviado mediante jenkins
    println "${params.Namespace}"
    def namespace = params.Namespace;
    def name = params.Name;
    sh '''

 	  kubectl logs '''+name+''' --namespace '''+namespace+'''
 	  
 	  '''


}
return this;