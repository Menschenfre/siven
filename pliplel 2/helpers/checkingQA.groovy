def ejecutar(Object extraDir = '') {
  dir('/opt/tools/integracion_ant/devops/'){
    if (env.PIPELINE_APP == "Android"){
      sh "/Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}"+extraDir+"/src/main/java -Dcanal=Android -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=FrontAndroid -Dprefix.area.projects=/Android"
    } else if (env.PIPELINE_APP == "ANDROID-MODULES"){
      sh "/Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}"+extraDir+"/src/main/java -Dcanal=AndroidModules -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=FrontAndroid -Dprefix.area.projects=/AndroidModules"
    } else {
      sh "/Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}"+extraDir+"/src/main/java -Dcanal=Microservicios -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=Microservicios -Dprefix.area.projects=/Microservicios"
    }
  }
  if (!validarPorcentaje()){
    error ("Error en revision de checkingQA")
  }
}

def validarPorcentaje(){
  dir('/opt/tools/integracion_ant/devops/logs'){
    def porcentajeObtenido = sh (script: 'set +x;ls -t ${PROJECT_NAME}-${BRANCH_NAME}*.out | head -1 | xargs cat | grep -e "global confidence" | head -1 | awk \'{ print $1 }\' | sed -e "s/,/./g" -e "s/\\[//g" -e "s/\\]//g" -e "s/%//g"', returnStdout: true).trim().toFloat()
    def porcentajeAceptacion = readFile '/opt/tools/integracion_ant/devops/porcentajeAceptacion.txt'
        
    if (porcentajeObtenido >= porcentajeAceptacion.trim().toFloat()){  
      println "Porcentaje obtenido "+porcentajeObtenido+" superior a "+porcentajeAceptacion.trim()
      return true
    } else {
      println "Porcentaje obtenido "+porcentajeObtenido+" minimo a "+porcentajeAceptacion.trim()
      return false
    }
  }
}

return this;