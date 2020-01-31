def call() {
	env.PIPELINE_HOME="/opt/tools/pipelines-jenkins/"
	def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');
	def login = load(env.PIPELINE_HOME+'helpers/login.groovy');
	def rollback = load(env.PIPELINE_HOME+'helpers/rollbackKubernetes.groovy');

	def regApiConnect = common.obtenerParametro("api.published")    
    def data = "${params.listaComponentes}"
    Map listaComponentes = [:]
    def deployName, projectName, branchName

     data.splitEachLine(","){
       it.each{ x ->
         def object = x.split(":")
         listaComponentes.put(object[0], object[1])
       }
     }
	
	listaComponentes.each {
		item -> println "$item.key = $item.value"
		if("$item.value" == "True"){
			println "ROLLBACK DE COMPONENTE: " + "$item.key"

			deployName = "$item.key"
			projectName = "$item.key".split("-re")[0]
			branchName = deployName.replace(deployName.substring(0, deployName.indexOf('re-')),"")

			deployName = deployName.take(55).toLowerCase()
			if (deployName.endsWith("-")){
				deployName = deployName.take(54)
			}
			
			env.SPACE = "PROD_LOCAL"
			env.VISIBILITY = "True"
			env.PIPELINE_APP = projectName.split("-")[0].toUpperCase()
			env.BRANCH_NAME = branchName
			env.DEPLOY_NAME = deployName
			env.PROJECT_NAME = projectName

			println "BRANCH_NAME: " + env.BRANCH_NAME
			println "PIPELINE_APP: " + env.PIPELINE_APP
			println "PROJECT_NAME: " + env.PROJECT_NAME
			println "DEPLOY_NAME: " + env.DEPLOY_NAME\

			rollback.produccion(login, common, regApiConnect, env.PROJECT_NAME)
		}
	}
}
return this;