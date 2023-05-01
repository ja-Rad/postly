pipeline {
    agent any
    tools {
        maven 'maven-3.9.1'
    }
    environment {
        JASYPT_ENCRYPTOR_PASSWORD_VALUE = credentials('JASYPT_ENCRYPTOR_PASSWORD')
    }

    stages {
        stage('Clone Git Project') {
            steps {
                echo 'Clone Git Project...'
                git(branch: 'master', changelog: true, poll: true, url: 'https://github.com/Radec24/postly.git')
            }
        }

        stage('Compile Project') {
            steps {
                sh 'mvn clean compile -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE}"'
            }
        }

        stage('Build Project') {
            steps {
                sh 'mvn clean install -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE}"'
            }
        }

        stage('Project Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE}"'
                    }
                }

                stage('Integration Tests') {
                    steps {
                        sh 'mvn failsafe:integration-test@it-tests -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE}"'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-localhost-9000') {
                    sh 'mvn clean sonar:sonar -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE}"'
                }

                waitForQualityGate true
            }
        }
    }
}
