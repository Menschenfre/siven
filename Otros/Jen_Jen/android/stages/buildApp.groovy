def call() {
    sh 'bash gradlew assembleRelease --stacktrace'
}
return this;
