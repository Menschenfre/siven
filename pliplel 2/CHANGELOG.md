#Changelog

Todos los cambios para este proyecto seran documentados en este archivo.

## [2.6.0] - 2019-01-31
## Added
- Validacion de archivos enviroment para los front-end evitando que existan urls apuntando a api connect pre productivo

## Changed
- Script para crear las imagenes docker
- Script para ejecutar la compilacion de los front end

## Removed
- Edicion de archivos environment en front end para que no cree archivos apuntando a apigee pre productivo

## [2.5.19] - 2019-01-30
### Added
- Se agrega validacion en template.groovy para que el ms-nominasrecaudaciondebito-neg ocupe el template de ms-pagosmasivos-neg.

## [2.5.18] - 2019-01-28
### Added
- Se agrega validacion al flujo release para revisar que las ramas re-v contengan el ultimo commit de la master, en caso de no encontrar el pipeline fallara
- Se agrega variable que activa o desactiva dicha validacion

## [2.5.17] - 2020-01-27
### Added
- Se habilita revision de Vulnerabilidades con Veracode para BFF y Android en IC.

## [2.5.16] - 2020-01-17
### Changed
- Se habilita revision de vulnerabilidades con Veracode para MS, IG y Front-end, con previa configuración de componente.

## [2.5.15] - 2020-01-09

## Fixed
- Se elimina validacion de config file produccion para repositorio azure.

## [2.5.14] - 2020-01-06

## Changed
- Quiebre pipeline al encontrar error en declaracion de apps para apigee en container_params.json

## Fixed
- Se agrega excepcion en healthcheck de ig-nexus-servicios por ocupar libreria ms-core.
- Se corrige healthcheck true en archivo deployAzureKubernetes.groovy.

## [2.5.13] - 2020-01-03

## Changed
- Registry azure para imagenes preinstall de bff y front-end

## [2.5.12] - 2020-01-02

## Fixed
- Se corrige obtencion de nombre de proyecto para validacion de config files ic, qa y prod.

## [2.5.11] - 2019-12-27

## Added
- Stage de pruebas para apis de openbanking

## Changed
- Carga de archivo config desde properties para la ejecucion de las configuraciones sobre las apis de openbanking
- Se modifica llamado y validacion de config servers ic, qa y prod.

## [2.5.10] - 2019-12-16

## Changed
- Se modifica archivo de lectura de validacion proyecto bci.

## [2.5.9] - 2019-12-12

## Added
- Stage nuevo en pipeline de openbanking para ejecutar json de configuracion posterior al upload del api en apigee.

## [2.5.8] - 2019-12-11

## Changed
- Se modifica el merge a rama release, se actualiza primero la release contra master para evitar conflictos.

## [2.5.7] - 2019-12-11

## Added
- Se agrega jenkinsfile, metodos de rollback y merge para release agrupado.

## Fixed
- Se agrega validacion de dependencia igcore en componente server para healthcheck ig.

## [2.5.6] - 2019-12-10

## Changed
- Se modifican valores livenessprobe de healthcheck microservicios.

## [2.5.5] - 2019-12-09

## Changed
- Se modifica ejecucion de validacion de proyecto bci de obligatorio a stage (ic y qa).
- Se descomenta healthcheck ig.

## [2.5.4] - 2019-12-05

## Added
- Variables para config.json de pipeline openbanking
- Opcion de soloStage en pipeline de openbanking

### Fixed
- Se elimina fe-pfmmisfinanzaswebpersonas de host ingress cer-personas.bci.cl

## [2.5.3] - 2019-12-04

## Changed
- Se modifica orden de llamado a gradle de validacion proyecto Bci en pipeline ig (ic y release).
- Se modifica sed para reemplazar valores de ruta jeveris-codegen en buil.properties de proyecto ig.

## [2.5.2] - 2019-12-03

## Changed
- Comandos para desplegar en apigee productivo
- Nombres de nodos jumpbox para trabajar con apigee produccion

## [2.5.1] - 2019-12-03

## Added
- Se agrega en pipeline-jenkins/ios/stages/buildIpaProd.groovy un git pull para descargar commit con cambios para subir version app.

## [2.5.0] - 2019-11-27

## Changed
- Metodo para obtener los scripts del pipeline, ahora se utiliza metodo load a partir de directorio externo a jenkins
- Reemplazo de nomenclatura en variables comunes

## Removed
- Variables de ambiente bluemix para ambientes pre productivos

## [2.4.14] - 2019-11-26

### Fixed
-  Modificacion a metodo que valida resultados en pruebas de rendimiento.

## [2.4.13] - 2019-11-25

## Changed
- Se comenta comparacion de archivos QA y Prod.
- Se cambia ruta config file QA de Bluemix a Azure.
- Se cambia variable params.projectName a env.PROJECT_NAME

## [2.4.12] - 2019-11-19

## Changed
- Se cambian paths en modulos ios IC y se agrega validacion de keychain.

## [2.4.11] - 2019-11-19

## Changed
- Se cambia metodo angular8 para que siempre retorne false

## [2.4.10] - 2019-11-15

## Changed
- Se cambia forma buscar archivos collection.json para postman openbanking.

## [2.4.9] - 2019-11-15

## Changed
- Se cambia forma de ejecutar DTP para aplicaciones android (de gradle a bash gradlew).

## [2.4.8] - 2019-11-14

## Changed
- Se cambia forma de publicar aplicaciones Android en HockeyApp/AppCenter IC y QA.

## [2.4.7] - 2019-11-13

## Added
- Se agrega despliegue de cronJobs para bluemix y azure PROD
- Se agrega eliminacion de cronJobs para post validacion de deploy en prod.

## [2.4.6] - 2019-11-13

## Added
- Se agrega gradle de validacion de proyecto en ejecución de pipelines de MS e IG (IC y QA).

## Changed
- Se cambia la forma en como se arma el nombre con el que se busca el JMX para las pruebas de rendimiento.

## [2.4.5] - 2019-11-11

## Changed
- Se cambia validacion de pruebas de carga, se quiebra el pipeline con menos de 2 flujos (1 de login/token y al menos 1 de otra ejecucion) y se quita validacion de nombre de componente.

## [2.4.4] - 2019-11-11

## Added
- Se agrega buscador de URL's hardcode en los FE, se informa a los practitioners via slack la cantidad de URL comentadas, no comentadas y si pertenecen a APIGTW

## [2.4.3] - 2019-11-11

## Changed
- Se quita validacion para que medio de Pago se inscriba en el host front-dsr01.bci.cl

## [2.4.2] - 2019-11-07

## Changed
- Se eliminan cambios de colleccion

## [2.4.1] - 2019-11-06

## Changed
- Se aplica archivo de configuracion para llamado postman en open banking

## [2.4.0] - 2019-11-05

## Added
- Se agrega nuevo pipeline CronJob para despliegue en IC y QA.

## [2.3.2] - 2019-11-05

## Added
- Se agrega validadicion para obtener el largo de la version

## [2.3.1] - 2019-10-30

## Changed
- HOTFIX se cambian las rutas de host para pruebas de carga del cluster por las de ingress

## [2.3.0] - 2019-10-29

## Added
- Pipeline inicial para proyecto OPENBANKING

## [2.2.3] - 2019-10-29

## Changed
- Pruebas de carga con variables de ambiente y threads

## [2.2.2] - 2019-10-21

## Added
- Para ms-enrolamientowallet-orq se establece host especifico apicert.bci.cl

## [2.2.1] - 2019-10-21

## Added
- Agregamos reintento de 3 veces cuando haya problemas al obtener swagger del pod

## Changed
- Metodo para encontrar producto suscrito en app de
- Obligatoriedad de declaracion de app en container_params.json cuando se requiera registrar en apigee
- Generacion de zip para apigee a cargo del pom.xml

## [2.2.0] - 2019-10-11

## Changed
- Ruta para obtener valires de templates, limits y tempalte de apigee

## Removed
- Carpeta templates que se traslado a repositorio properties del pipeline
- Carpeta limits que se traslado a repositorio proerties del pipeline

## [2.1.5] - 2019-10-10

## Added
- Se agrega cambio en la etapa de creacion de imagen y se agrega el sudo al eliminar un directorio

## Changed
- Se modifica gradle dependencies en obtener version de librerias para healthcheck ig

## [2.1.4] - 2019-10-09

## Added
- Validacion de libreria actuator para healthcheck ig. Si no existe (o no existe la igcore), quiebra el pipeline.

## [2.1.3] - 2019-10-08

## Removed
- Se comenta validacion config file en bff y fe (ic y qa) por el momento.

## [2.1.2] - 2019-10-07

## Added
- Validacion de config file para integracion continua.

## [2.1.1] - 2019-10-07

## Added
- Validacion healthcheck para componentes de integracion.

## [2.1.0] - 2019-10-07

## Added
- Validacion de concordancia en paths entre el archivo swagger y las fuentes para los BFF en Integracion Continua
- Validacion de spec desde pod desplegado para MS, MOBILE, SI-EJB y BFF en Integracion Continua
- Envio de notificacion slack de merge branch a slack de everis

## [2.0.1] - 2019-10-01

### Fix
- Eliminación de validación config file QA para Bluemix.

## [2.0.0] - 2019-09-30

## Removed
- Se elimina login para IC-QA en BFF-MS-FE,FE-ANGULAR, MOBILE, SI, SI-EJB.
- Se elimina registroApiConnect y se deja solo registroApiGee para IC-QA.
- Se elimina validacion de flag Bluemix en deployKubernetes.groovy.
- Se elimina despliegueKubernetes, deployIngress para bluemix pre-prod, quiebre pipeline azure en las notificaciones y borrado de imagenes en registry, para IC-QA en commonMethods.groovy.
- Se elimina crearImagenDocker para bluemix pre-prod en crearImagenDocker.groovy.
- Se elimina pruebas funcionales para bluemix en MS,SI,MOBILE.

## [1.6.1] - 2019-09-26

## Added
 - Validacion de env.REG_EUREKA en main.groovy, si env.REG_EUREKA no existe o esta en true pipe queda como UNSTABLE

## Changed
 - Se quiebra pipeline en validacion de config files QA
 - Se valida yamllint en config files QA siempre y cuando existan
 - Se valida config file de repo azure prod

## Removed
 - se eliminan 2 IF que cambiaban el valor de env.EUREKA que no se estaban utilizando

## [1.6.0] - 2019-09-26

## Added
- Validacion de nodo offline para despliegues azure
- Ejecucion de yamllint para validacion de config files
- Try/Catch en rollback de azure, para capturar posibles errores en nodos productivos
- Mensaje en slack de jobs que no reciban como parametro desde XL Release registroApiMngmnt y se registren en api connect
- Se agrega validacion yamllint en verificacion de config files antes de paso a prod.

## Changed
- Invocacion a rollback de azure produccion
- Establecemos BLUEMIX_HOME en el workspace para validar conexion a registry bluemix

## Removed
- Pipeline BFF-MOBILE
- Referencias a pipeline bff-mobile en metodos

## [1.5.14] - 2019-09-25

## Added
- Se agrega stage de verificacion de config file en pipeline de Releases para MS, IG, BFF, FE
- Se agrega validacion de label bluemix para deployKubernetes
- Se controla error en caso de que deployIngress falle, si esta label azure=true no quiebra el pipeline.

## [1.5.13] - 2019-09-23

### Fix
- Eliminacion de comando actualizacion de dependencias.

## [1.5.12] - 2019-09-17

## Added
- Incorporamos fe-certificadosdigitales y fe-clientestarjetadebido a host personas.bci.cl

## [1.5.11] - 2019-09-16

## Added
- Comando para desbloquear keychain de la macmini

## [1.5.10] - 2019-09-13

## Added
- Se agrega timeout para stage de build para android y android-modules en IC.

## [1.5.9] - 2019-09-10

## Added
- Validacion para api.product.apps que sea JsonArray

## Changed
- Nombre correcto parallel Providencia
- Mejora en metodo de conexion a registry bluemix
- Metodos common en pipelines correspondientes
- Validacion de despliegue en azure preproductivo
- Git pull en respaldo de yamls

## [1.5.8] - 2019-09-10

## Added
- Se agregan parametros a comando gradle build, para resfrescar dependecias

## [1.5.7] - 2019-09-05

## Added
- se agregan variables de entorno para pipeline de ios, por problema despues del reinicio del 5 de septiembre 2019 a las 16 hrs.

## [1.5.6] - 2019-09-05

## Added
- Metodo para limpiar saltos de linea en apis descargadas por draft pull producto
- Reemplazo de \n\n por ------ para evitar problema en borrador de api
- Agregamos los dos virtual host productivos en api antes de subir a apigee

## [1.5.5] - 2019-09-04

## Added
- Se agrega label de commit para deploy.

## [1.5.4] - 2019-09-04

## Added
- Incorporacion de opcion para proyectos front end con angular 8

## Changed
- Mejora en creacion de imagenes npm install para proyectos front
- Modificacion de environments para proyectos con angular-cli.json

## [1.5.3] - 2019-09-03
## Fixed
- Correcion en la formacion del tagCandidato para librerias core.

## [1.5.2] - 2019-09-03
## Changed
- Se comenta llamado a veracode en todos los stages.

## [1.5.1] - 2019-08-29

## Added
- Se realiza merge en caperta limpia por conflictos con archivos generados por pipeline.
- Se agrega notificacion de Slack.

## [1.5.0] - 2019-08-27

## Added
- Validacion de que webapp declarada en json no coincide con las apps existentes en apigee
- Nombres de clusters para produccion azure
- Stages de despliegue ingress y kubernetes para azure produccion
- Eliminacion de api proxy en producto para apigee produccion
- Incorporacion de manejo de variables en despliegue azure produccion
- Rollback en azure produccion

## [1.4.2] - 2019-08-20

## Added
- se agrega en stage deployKubernetes,deployIngress y pruebasFuncionales, validacion de flag azure.


## [1.4.1] - 2019-08-15

## Added
- Se agrega validacion de flag para archivo container_params.json, en stage apiConnect.

## [1.4.0] - 2019-08-13

## Changed
- Mejora en ejecucion script apigee
- Obtencion en nombre de nodos para azure
- Mejora en notificacion slack

## Added
- Stage propio para despliegue apigee produccion
- Separacion y stage propio para registro api connect MS en CERT

## Removed
- Credenciales innecesarias en main.groovy
- Referencias a vault token

## [1.3.13] - 2019-08-08

## Added
- Se agrega variable branchMerge y stage de merge develop y master a mobile en kubernetes.groovy

## [1.3.12] - 2019-08-07

## Changed
- Se cambia logica para merge en develop y master, ademas de forzar el merge a master en caso de conflictos.

## [1.3.11] - 2019-08-06

## Added
- Se agrega error() en catch de stages de IC
- Para comandos kubectl relacionados a azure se agrega la opcion --context para indicar donde debe ejecutarse el comando

## Removed
- Comando kubectx para azure

## [1.3.10] - 2019-08-05

## Fix
- Se cambia validacion de resultado DTP

## [1.3.9] - 2019-08-05

## Changed
- Se vuelve a pool los nodos de dtp para todos los componentes en ic y release

## [1.3.8] - 2019-08-02

## Added
- Se agrega Veracode para front y modulos-front

## [1.3.7] - 2019-07-31

## Added
- Se agrega Veracode para ios y android

## [1.3.6] - 2019-07-31

## Added
- Se agrega Veracode para ms e ig en release

## [1.3.5] - 2019-07-29

## Added
- Metodo para informar si flag azure se encuentra activo o no
- Quiebre de pipeline en caso de que algun paso asociado a azure o apigee falle y flag se encuentre en true

## [1.3.4] - 2019-07-29

## Added
- Se agrega comando para el metodo mergeRama.

## [1.3.3] - 2019-07-26

## Added
- Se agrega comando en deployKubernetes.groovy para ver logs.
- Se agrega validacion en commonMehtods.groovy para la creacion de tag para mergeRama.

## Changed
- Se cambia de lugar comando para cambio de permisos en /dist/* para los front-end en kubernetes.groovy
- Fix a android-modules para variable en checkingQA.

## [1.3.2] - 2019-07-25

## Added
- Se agrega validacion de cumplimiento de stages mediante notificacion por bitbucket y campo booleano en IC.

## [1.3.1] - 2019-07-25

## Added
- se agrega comando a buildApp.groovy para que deje artefacto de mapping.txt

## [1.3.0] - 2019-07-25

## Changed
- Mejora en ejecucion de checkingQA
- Respaldo de yaml en un solo commit
- Mejora en metodo ngBuild para los front-end
- Incorporacion de mas informacion en notificaciones slack
- Utilizacion de readProperties para validacion en bootstrap.properties
- Mejora en pipeline ms-corelibs en cuanto a mensajes de error

## Added
- Pipelines de front-end angular js para que se desplieguen en azure
- Validacion de hotfix en rollback produccion para que solo las versiones con hotfix 0 se eliminen de ingress
- Agregamos tiempo limite de 5 minutos en registro de api-connect

## Removed
- Metodo obtenerParametroTXT debido a que solamente se leen archivos json a traves del pipeline

## [1.2.11] - 2019-07-23
### Fixed
-  Correccion de helper veracode para evitar fallas en las ejecuciones.

## [1.2.10] - 2019-07-22
### Fixed
-  Correccion de invocacion inexistente a helper "apiMngt".

## [1.2.9] - 2019-07-19

## Changed
- Se modifica ruta jeveris en helper/dtp para IG.

## [1.2.8] - 2019-07-17

## Changed
- Se modifica metodo MergeRama para que se cree el tag solo en la rama master.

## [1.2.7] - 2019-07-17

## Added
- Se agrega metodo mergeRama a commonMethods.groovy y se agrega stage MergeMaster y MergeDevelop en pipelines.

## [1.2.6] - 2019-07-15

## Changed
- Correccion para metodo que respalda yamls en carpetas azure

## [1.2.5] - 2019-07-15

## Removed
- Se quita carpeta Coverity y helper de coverity.groovy.

## [1.2.4] - 2019-07-11

## Added
- Incorporacion de host personas.bci.cl en metodo de ingress para bluemix y azure

## [1.2.3] - 2019-07-11
### Fixed
- Agregar Metodo para agregar path en la creacion de imageen de FE-Angularjs.
- Modificacion al html expuesto por pipeline a solicitud de Angelo Figueroa Rodriguez (celula).

## [1.2.2] - 2019-07-10

## Changed
- Jtest en IC para nodo1 y Release en nodo2

## [1.2.1] - 2019-07-08

## Changed
- Nombres de cluster name para front azure
- Metodo y validacion login azure y bluemix

## [1.2.0] - 2019-07-03

## Added
- Incorporacion de suscripcion automatica de productos a webapp en apigee
- En caso de existir webapp en container_params.json se valida que coincida con el listado de apps que existe en apigee

## [1.1.1] - 2019-07-01

## Added
- Host \*integracionbci se agrega en opciones para archivo nginx.conf-header

## Removed
- Linea que reemplaza valor proxy-http-url debido a actualizacion en template de apigee

## [1.1.0] - 2019-06-28

## Added
- Se agrega helper de veracode para analisis de codigo
- Se agrega stage veracode a los microservicios en integracion continua


## [1.0.2] - 2019-06-24

## Added
- Invocacion de metodo para crear dockerfile en pipelines FRONTEND-AngularJS
- Agregamos virtual host para reemplazar en template de apigee
- Validamos que el deploy name no termine en "-"

## Changed
- URL de apigee dsr y crt por el nombre del host en reemplazo de URL con IPs
- Homologacion de templates ms-pagosmasivos-neg.yml.j2 y ms-documentosempresa-neg.yml.j2 con template base de MS


## [1.0.1] - 2019-06-21

## Added
- Se  agrega metodo de modificar path bff en stage de crearImagenDocker

## [1.0.0] - 2019-06-21

## Added
- Se agrega CHANGELOG.md para ir registrando los cambios en el proyecto.

## Changed
- Se cambia logica en api-connect.groovy, commonMethods.groovy, deployIngress.groovy, rollbackKubernetes.groovy para identificar ruta de archivos ingress de pre-productivo y produccion.
