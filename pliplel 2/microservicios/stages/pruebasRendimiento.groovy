def call() {
    echo 'Iniciando Pruebas de rendimiento...'
    env.JMETER_PATH = "./jmeter/"
    env.JMX_FILE = "${env.JMETER_PATH}${env.PROJECT_NAME}.jmx"

    if(fileExists(env.JMX_FILE)){
        lock(resource:'archivosJmeter'){
            modifyJmxFile(env.JMX_FILE)
            generateReports()
            publishReports()
            compareResults()
        }
        echo 'Pruebas de rendimiento finalizadas.'
    }else{
        println ("ADVERTENCIA: No existe el archivo de pruebas Jmeter ${env.JMX_FILE} o posee otro nombre para realizar las pruebas de rendimiento. En esta etapa no será considerado este error y continuará la ejecución del job.")
        //currentBuild.result = 'FAILURE'
        //return false
    }
}

def compareResults(){
    def testValues = sh(script: '''set +x;cat ${JMETER_PATH}aggregateResults.csv | awk '{print $1","$3","$10}' FS="," | sed -e '1d' -e '$d' ''', returnStdout: true).trim().split('\n')
    def requestCount = 0
    BigDecimal numLimitPorcError = env.PORC_ERROR.toBigDecimal()
    BigDecimal averageLimit = env.AVERAGE_TIME_LIMIT.toBigDecimal()
    println "Validando ejecuciones ..."
    for (String test : testValues){
        def metrics = test.split(",")
        def msName = metrics[0].toLowerCase()
        BigDecimal averageTime = ((metrics[1].toBigDecimal())/1000)
        BigDecimal porcError = (metrics[2].replace("%","")).toBigDecimal()
        println ("El llamado ["+msName+"] contiene un Average Time es de "+averageTime+"(seg) y un porcentaje de error es de "+porcError+"%.")
        if(!msName.contains("token") && !msName.contains("login") && (averageTime > averageLimit || porcError > numLimitPorcError)){
            println "ERROR: Las pruebas de carga no cumplen el standard definido, favor revisar."
            currentBuild.result = 'FAILURE'
        }
        requestCount++
    }
    if(requestCount < 2){
        println "Cantidad de Flujos minimo para pruebas de rendimiento: 2 "
        println "ERROR: no cumple con la cantidad minima de flujos para pruebas, Flujos encontrados: "+requestCount
            currentBuild.result = 'FAILURE'
    }
}

def publishReports(){
    echo 'Generando y disponibilizando archivo Zip con Reportes ...'
    def nomZip = "OC${OC_VALUE}.zip"
    sh "set +x; zip -r $nomZip OC${OC_VALUE}"
    step([$class:'ArtifactArchiver', artifacts:"$nomZip"])
}

def generateReports(){
    println "Ejecutando Jmeter ..."
    def statusReports = sh(script: '''set +x;
        if [[ -d ${WORKSPACE}/OC${OC_VALUE} ]]; then rm -Rf ${WORKSPACE}/OC${OC_VALUE}; fi
        if [[ -f ${JMETER_PATH}test_results.jtl ]]; then rm ${JMETER_PATH}test_results.jtl; fi
        echo  "HOST:" ${HOST}
        echo  "CONTEXT:" ${CONTEXT}
        echo  "VERSION:" ${VERSION}
        echo  "THREADS:" ${THREADS}
        /var/lib/jenkins/apache-jmeter-4.0/bin/jmeter.sh -n -t ${JMX_FILE} -JvHOST=${HOST} -JvCONTEXT=${CONTEXT} -JvVERSION=${VERSION} -JvTHREADS=${THREADS} -LDEBUG -l ${JMETER_PATH}test_results.jtl -e -o ${WORKSPACE}/OC${OC_VALUE}
        /var/lib/jenkins/apache-jmeter-4.0/bin/JMeterPluginsCMD.sh --generate-csv ${JMETER_PATH}aggregateResults.csv --input-jtl ${JMETER_PATH}test_results.jtl  --plugin-type AggregateReport
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
            sp.value = "${JMETER_PATH}"+csvFile
        }
    }

    def stringWriter = new StringWriter()
    def nodePrinter = new XmlNodePrinter(new PrintWriter(stringWriter))
    nodePrinter.setPreserveWhitespace(true)
    nodePrinter.print(jmeterTestPlan)
    def xmlString = stringWriter.toString()

    return xmlString
}

def validarJmxGroups(String jmxDir){
    dir(jmxDir){
        def threadGroup =  sh ( script:''' grep "ThreadGroup " ms-protestos-neg.jmx | grep "testname" | awk -F"=" '{print $4}' | awk -F'"' '{print $2}' ''', returnStdout: true).trim().split('\n')
        return threadGroup
    }
}

return this;
