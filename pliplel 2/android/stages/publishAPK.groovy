def call() {
    sh 'bash gradlew publishApkRelease --stacktrace'
}
return this;
