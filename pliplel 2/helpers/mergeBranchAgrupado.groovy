def call() { 
	env.PIPELINE_HOME="/opt/tools/pipelines-jenkins/"
	def common = load(env.PIPELINE_HOME+'helpers/commonMethods.groovy');

    def data = "${params.listaComponentes}"
    Map listaComponentes = [:]
    def deployName, projectName, branchName, branchMerge

     data.splitEachLine(","){
       it.each{ x ->
         def object = x.split(":")
         listaComponentes.put(object[0], object[1])
       }
     }
	
	listaComponentes.each {
		item -> println "$item.key = $item.value"
		if("$item.value" == "False"){
			println "MERGE DE COMPONENTE: " + "$item.key"

			deployName = "$item.key"
			projectName = "$item.key".split("-re")[0]
			branchName = deployName.replace(deployName.substring(0, deployName.indexOf('re-v')),"")
			
			env.BRANCH_NAME = branchName
			env.PROJECT_NAME = projectName

			println "BRANCH_NAME: " + env.BRANCH_NAME
			println "PROJECT_NAME: " + env.PROJECT_NAME

			if(!projectName.contains("base")){
				common.mergeRama("develop", branchName)
            }
			common.mergeRama("master", branchName)
		}
	}
}
return this;


