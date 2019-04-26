def call() {
  println "Pipeline checkingQA -> ${env.PIPELINE_APP}" 
  sh ''' set +x
    echo ${DIR_EXTRA}
    
    cd  /opt/tools/integracion_ant/devops/
    if [[ ${PIPELINE_APP} == 'Android' ]]; then
      /Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}${DIR_EXTRA}/src/main/java -Dcanal=Android -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=FrontAndroid -Dprefix.area.projects=/Android
    elif [[ ${PIPELINE_APP} == 'ANDROID-MODULES' ]]; then
      /Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}${DIR_EXTRA}/src/main/java -Dcanal=AndroidModules -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=FrontAndroid -Dprefix.area.projects=/AndroidModules
    else 
      /Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}${DIR_EXTRA}/src/main/java -Dcanal=Microservicios -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=Microservicios -Dprefix.area.projects=/Microservicios
    fi
  '''
  if (!validarPorcentaje()){
    error ("Failure")
  }
}

def validarPorcentaje(){
  dir('/opt/tools/integracion_ant/devops/logs'){
    def porcentajeObtenido = sh (script: 'set +x;ls -t ${PROJECT_NAME}-${BRANCH_NAME}*.out | head -1 | xargs cat | grep -e "global confidence" | head -1 | awk \'{ print $1 }\' | sed -e "s/,/./g" -e "s/\\[//g" -e "s/\\]//g" -e "s/%//g"', returnStdout: true).trim().toFloat()
    def porcentajeAceptacion = sh (script: 'set +x;cat /opt/tools/integracion_ant/devops/porcentajeAceptacion.txt', returnStdout: true).trim().toFloat()
    
    if (porcentajeObtenido >= porcentajeAceptacion){  
      println "Porcentaje obtenido "+porcentajeObtenido+" superior a "+porcentajeAceptacion
      return true
    } else {
      println "Porcentaje obtenido "+porcentajeObtenido+" minimo a "+porcentajeAceptacion
      return false
    }
  }
}

def bffMobile(){
  def sumaTotal = 0
  sh "mkdir checkingQA"
  def listado = sh (script: 'find * -type d -name java | grep main | awk \'{split($0,a,"/"); print a[1]}\'', returnStdout: true).trim()
  String[] splitData = listado.split("\n");
  for (String eachSplit : splitData) {
    sh "mkdir -p checkingQA/$eachSplit/src/main/java; cp -r $eachSplit/src/main/java checkingQA/$eachSplit/src/main/java"
  }
  dir('/opt/tools/integracion_ant/devops/'){
    sh "/Maquina/tools/apache-ant-1.9.6/bin/ant -file build.xml -Did=${BRANCH_NAME} -Dsrc.dir=${WORKSPACE}/checkingQA -Dcanal=Microservicios -Dproveedor=${PROJECT_NAME} -Dingeniero=Jenkins110 -Dquality.model=Microservicios -Dprefix.area.projects=/Microservicios"
  }
  sh "sudo rm -r checkingQA"
  if (!validarPorcentaje()){
    error ("Failure")
  }
}

return this;