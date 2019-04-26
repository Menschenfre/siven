def call() {

         stage('Limpiar Entorno'){
              sh """
              cd /var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/showplans/
              rm -rf *.txt
              """
            }
         stage('Copiar Shells'){
              ejecucion = build job: 'Push_Shells_Sybase', parameters: [[$class: 'StringParameterValue', name: 'BRANCH', value: env.BRANCH]]
          }
         stage('Monitoreo'){
              ejecucion = build job: 'inicio_monitoreo_sp', parameters: [[$class: 'StringParameterValue', name: 'BRANCH', value: env.BRANCH]]

              archivo = 'resumen_monitoreo_build_'+ejecucion.number+'.txt'
            }
         stage('Generar Resumen y Show Plans'){
              sh """
                  cd /var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/
                  cat $archivo
                  cp $archivo /var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/showplans/
              """
          }
          stage ('Check Errores'){
                  env.archivo=archivo
                  sh """
                      #echo "estoy en : "
                      cd /var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/showplans/
                      #pwd
                      echo "archivo a escanear: "$archivo
                  """
                  def retstatus=sh(script:'''
                    cd /var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/showplans/
                    grep -q 'Error:' $archivo''',returnStatus: true)

                  if(retstatus == 0){
                    println("Pipeline falló, existen errores")
                    currentBuild.result='FAILURE'
                  }
                  else {
                  println("Pipeline exitoso")
                  currentBuild.result='SUCCESS'
                  }
                }
         stage('Publicar ZIP'){
              numero = env.BUILD_NUMBER+".zip"
              zip archive: true, dir: '/var/lib/jenkins/workspace/monitoreo_automatico_sp/resultado_monitoreo_sp/reports/showplans', glob: '*.txt', zipFile: numero

            }
  }
  return this;
