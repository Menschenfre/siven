def call() {
    echo 'Iniciando Pruebas de rendimiento...'
    env.REPOSITORY = scm.userRemoteConfigs[0].url 
    env.TEST_NAME = sh (script: '[[ "${REPOSITORY}" =~ (git@)(.+):(.+)\\/(.+)(\\.git) ]] && echo -n "${BASH_REMATCH[4]}"', returnStdout: true)
    env.JMETER_DIR = env.WORKSPACE+'/jmeter/'
    def jmxFile = "${JMETER_DIR}${env.TEST_NAME}.jmx"
    
    if(fileExists(jmxFile)){
        lock(resource:'archivosJmeter'){
            env.JMX_MODIFIED = modifyJmxFile(jmxFile)
            generateReports()            
            if(env.SPACE == "CERT"){
                publishReports()
                compareResults()
            }
        }
        echo 'Pruebas de rendimiento finalizadas.'
    }else{
        println ("ADVERTENCIA: No existe el archivo de pruebas Jmeter "+env.TEST_NAME+".jmx o posee otro nombre para realizar las pruebas de rendimiento. En esta etapa no será considerado este error y continuará la ejecución del job.")
        //currentBuild.result = 'FAILURE'
        //return false
    }
}

def compareResults(){
    def averageLimit = env.AVERAGE_TIME_LIMIT.toInteger()
    def testValues = sh(script: '''set +x;cat ${JMETER_DIR}aggregateResults.csv | awk '{print $1","$3","$10}' FS="," | sed -e '1d' -e '$d' ''', returnStdout: true).trim().split('\n')
    
    for (String test : testValues){
        def metrics = test.split(",")
        def msName = metrics[0]
        def averageTime = (metrics[1].toInteger())/1000
        def porcError = metrics[2].replace("%","")  
        println "Validando llamado a '"+msName+"' ..."
        if(averageTime > averageLimit || porcError > env.PORC_ERROR){
            println ("Error: El llamado a '"+msName+"' no cumple condición de Pruebas de Rendimiento, ya que el Average Time es de "+averageTime+"(seg) y el porcentaje de error es de "+porcError+"%.")
            currentBuild.result = 'FAILURE'
        }
    }
}

def publishReports(){
    echo 'Generando y disponibilizando archivo Zip con Reportes ...'
    def nomZip = "OC${OC_VALUE}.zip"
    new File(env.WORKSPACE).eachDir() { dir ->
        sh "set +x; zip -r $nomZip OC${OC_VALUE}"
    }
    step([$class:'ArtifactArchiver', artifacts:"$nomZip"])  
}

def generateReports(){
    def statusReports = sh(script: '''set +x;
        echo ${JMX_MODIFIED} >> ${JMETER_DIR}${TEST_NAME}_modified.jmx
        if [[ -d ${WORKSPACE}/OC${OC_VALUE} ]]; then rm -R ${WORKSPACE}/OC${OC_VALUE}/*; fi
        /var/lib/jenkins/apache-jmeter-4.0/bin/jmeter.sh -n -t ${JMETER_DIR}${TEST_NAME}_modified.jmx -LDEBUG -l ${JMETER_DIR}test_results.jtl -e -o ${WORKSPACE}/OC${OC_VALUE}
        /var/lib/jenkins/apache-jmeter-4.0/bin/JMeterPluginsCMD.sh --generate-csv ${JMETER_DIR}aggregateResults.csv --input-jtl ${JMETER_DIR}test_results.jtl  --plugin-type AggregateReport
    ''', returnStatus:true)
    if(statusReports == 1){
        println('ERROR: Falla en la lectura de los archivos para realizar las pruebas de rendimiento.') 
        error ('FAILURE')
    }
}

def modifyJmxFile(String jmxFile){
    def jmxPath = readFile(jmxFile)
    def jmeterTestPlan = new XmlParser().parseText(jmxPath)
    
    for (Object csv:jmeterTestPlan['**']['CSVDataSet']){
        csv['stringProp'].findAll{sp ->
            sp['@name'] == 'filename'
        }.each{sp ->
            def csvPath = sp.text()
            def csvFile = csvPath.split("/").last()
            sp.value = env.JMETER_DIR+csvFile
        }
    }
    
    for (Object tg:jmeterTestPlan['hashTree']['hashTree']['ThreadGroup']){
        tg['stringProp'].findAll{sp ->
            sp['@name'] == "ThreadGroup.num_threads"
        }.each{sp ->
            sp.value = env.VU
        }
        tg['stringProp'].findAll{sp ->
            sp['@name'] == "ThreadGroup.ramp_time"
        }.each{sp ->
            sp.value = env.RAMP_UP
        }
    }   
    def stringWriter = new StringWriter()
    def nodePrinter = new XmlNodePrinter(new PrintWriter(stringWriter))
    nodePrinter.setPreserveWhitespace(true)
    nodePrinter.print(jmeterTestPlan)
    def xmlString = stringWriter.toString()

    return xmlString
}

return this;
