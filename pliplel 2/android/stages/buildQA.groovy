def call() {
    sh 'bash gradlew clean assembleQA --stacktrace'
}
return this;
