def call(Object common, Object dockerStage, Object nexusStage) {
  try{
    def versionDespliegue = common.agregarDesplieguePrivado(env.VISIBILITY, env.VERSION_DESPLIEGUE)
    def registry = obtenerRegistry(env.SPACE)

    dockerStage.build(env.PROJECT_NAME, versionDespliegue, registry)
    nexusStage.upload(env.PROJECT_NAME, versionDespliegue, registry)
    dockerStage.delete(env.PROJECT_NAME, versionDespliegue, registry)
  } catch (Exception err) {
    common.notificarSlack("tempAzure")
    println err
    error ("error al crearImagenDocker...")
  }
}

def obtenerRegistry(String space){
  switch(space){
    case "INT":
      return 'bcirg3dsrcnr001.azurecr.io/reg_ic'
    case "CERT":
      return 'bcirg3crtcnr001.azurecr.io/reg_qa'
    case "PROD_LOCAL":
      return 'repository.bci.cl:8323/tmp_prodlocal'
    default:
      error('obtenerRegistry() -> No space defined')
    break
  }
}

return this;