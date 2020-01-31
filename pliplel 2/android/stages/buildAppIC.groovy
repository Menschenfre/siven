def call() {
    sh 'bash gradlew clean assembleIntegration --stacktrace'
}
return this;
