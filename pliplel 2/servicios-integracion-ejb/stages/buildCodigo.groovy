def call() {
  deleteDir()
  checkout scm
  sh '/opt/gradle/gradle-4.1/bin/gradle build -x test -Dfile.encoding=ISO-8859-1 --no-daemon'
}
return this;
