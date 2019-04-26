def call() {
  step([$class: 'HockeyappRecorder', applications: [[apiToken: 'c77e0b47be114a7e99069ad59e024066', downloadAllowed: true,
   filePath: 'app/build/outputs/apk/integration/app-integration.apk', mandatory: false, notifyTeam: true, releaseNotesMethod: [$class: 'NoReleaseNotes'],
   uploadMethod: [$class: 'AppCreation', publicPage: false]]], debugMode: false, failGracefully: false])
}
return this;
