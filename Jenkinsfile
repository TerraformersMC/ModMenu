pipeline {
   agent any
   stages {
      stage ('Build') {
         when {
            branch '1.15'
         }
         steps {
            sh "rm -rf build/libs/"
            sh "chmod +x gradlew"
            sh "./gradlew clean --stacktrace"
            sh "./gradlew buildAndAttemptRelease publish --refresh-dependencies --stacktrace"

            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
         }
      }
   }
}
