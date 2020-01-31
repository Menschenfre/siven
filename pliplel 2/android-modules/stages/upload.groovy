def call() {
  sh 'set +x;./gradlew upload -PmavenUser=$NEXUS_USR -PmavenPass=$NEXUS_PSW'
}
return this;
