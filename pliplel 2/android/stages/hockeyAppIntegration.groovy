def call() {

	env.APK_PATH = "@app/build/outputs/apk/integration/app-integration.apk"
	env.URL_ANDROID_UPLOAD = "-X POST https://rink.hockeyapp.net/api/2/apps/${ANDROID_APP_ID_DEV}/app_versions/upload"
	
	println 'Subiendo apk...'
	response = sh (script: ''' curl -H "X-HockeyAppToken:${ANDROID_API_TOKEN}" -F "ipa=${APK_PATH}" ${URL_ANDROID_UPLOAD}''', returnStdout: true).trim()
	
	if (response.contains('error')){
		error('Subida de apk incompleta, error: ' + response)
	} else {
		println 'Subida de apk completada con exito.'	
	}
}

return this;
