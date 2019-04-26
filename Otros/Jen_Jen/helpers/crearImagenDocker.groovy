def call(Object common, Object dockerStage, Object nexusStage) {
  def versionDespliegue = common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE)
  dockerStage.build(env.PROJECT_NAME, versionDespliegue, "imageninicial")
  if (env.SPACE == "PROD_LOCAL" || env.PIPELINE_APP in ["FRONTEND", "FRONTEND-AngularJS"]){
    bluemix(common, dockerStage, nexusStage)
  } else {
    parallel (
      'BLUEMIX': {
        bluemix(common, dockerStage, nexusStage)
      },
      'AZURE': {
        try {
          azure(common, dockerStage, nexusStage)
        } catch (Exception err) {
          common.notificarSlack("tempAzure")
          currentBuild.result = 'SUCCESS'
        }
      }
    )
  }

  dockerStage.delete(env.PROJECT_NAME, versionDespliegue, "imageninicial")
}

def bluemix(Object common, Object dockerStage, Object nexusStage) {
  def bluemixDockerRegistry = "registry.ng.bluemix.net/"+env."${SPACE}_NAMESPACE_VALUE"
  def tmp_prodlocal = "repository.bci.cl:8323/tmp_prodlocal"

  def versionDespliegue = common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE)

  if (env.SPACE == "PROD_LOCAL"){
    //dockerStage.build(env.PROJECT_NAME, versionDespliegue, tmp_prodlocal)
    dockerStage.tag(env.PROJECT_NAME, versionDespliegue, "imageninicial", tmp_prodlocal)
    nexusStage.upload(env.PROJECT_NAME, versionDespliegue, tmp_prodlocal)
    dockerStage.delete(env.PROJECT_NAME, versionDespliegue, tmp_prodlocal)
  } else {
    //dockerStage.build(env.PROJECT_NAME, versionDespliegue, bluemixDockerRegistry)
    dockerStage.tag(env.PROJECT_NAME, versionDespliegue, "imageninicial", bluemixDockerRegistry)
    try {
      nexusStage.uploadbx(env.PROJECT_NAME, versionDespliegue, bluemixDockerRegistry)
    } catch (err) {
      def login = fileLoader.fromGit('helpers/login','git@bitbucket.org:bancocreditoeinversiones/pipelines-jenkins.git', 'master', null, '')
      login.bxRegistryLogin()
      nexusStage.uploadbx(env.PROJECT_NAME, versionDespliegue, bluemixDockerRegistry)
    }
    dockerStage.delete(env.PROJECT_NAME, versionDespliegue, bluemixDockerRegistry)
  }
}

def azure(Object common, Object dockerStage, Object nexusStage){
  def registry
  def registryAzure = registryAzure(env.SPACE)
  def tmp_prodlocal = "repository.bci.cl:8323/tmp_prodlocal"

  def versionDespliegue = common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE)

  if (env.SPACE == "PROD_LOCAL"){
    registry = tmp_prodlocal
  } else {
    registry = registryAzure
  }

  //dockerStage.build(env.PROJECT_NAME, versionDespliegue, registry)
  dockerStage.tag(env.PROJECT_NAME, versionDespliegue, "imageninicial", registry + "/" + env."${SPACE}_NAMESPACE_VALUE")
  nexusStage.upload(env.PROJECT_NAME, versionDespliegue, registry)
  dockerStage.delete(env.PROJECT_NAME, versionDespliegue, registry + "/" + env."${SPACE}_NAMESPACE_VALUE")
}

def registryAzure(String space){
  def registry
  switch(space){
    case "INT":
      registry = 'bcirg3dsrcnr001.azurecr.io'
    break
    case "CERT":
      registry = 'bcirg3crtcnr001.azurecr.io'
    break
    default:
      error('No space defined')
    break
  }
  return registry
}

return this;
