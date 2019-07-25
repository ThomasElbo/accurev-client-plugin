pipeline {
    agent any
    tools {
    gradle 'Standard'
    jdk 'JDK'
    }
    stages {
        stage('Build') {
            steps {
                bat 'gradlew hpi'
            }
        }
        stage ('Deploy') {
            steps {
                bat 'gradlew artifactoryPublish'
            }
        }
    }
}
