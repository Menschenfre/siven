def call() {
    sh 'bash gradlew clean assembleRelease --stacktrace'
    archiveArtifacts artifacts: 'app/build/outputs/mapping/release/mapping.txt', excludes: null
}
return this;
