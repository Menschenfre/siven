def call() {
  sh 'set +x;./gradlew clean build -x test'
}
return this;
