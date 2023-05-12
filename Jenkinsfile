pipeline {
    agent any
    tools {
        maven 'maven-3.9.1'
    }
    environment {
        JASYPT_ENCRYPTOR_PASSWORD_VALUE = credentials('JASYPT_ENCRYPTOR_PASSWORD')
        DOCKER_JENKINS_MYSQL_IP = '-DDOCKER_JENKINS_MYSQL_IP=mysql-postly'
    }

    stages {
        stage('Clone Git Project') {
            steps {
                echo 'Clone Git Project...'
                git(branch: 'main', changelog: true, poll: true, url: 'https://github.com/Radec24/postly.git')
            }
        }

        stage('Compile Project') {
            steps {
                sh 'mvn clean compile -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
            }
        }

        stage('Build Project') {
            steps {
                sh 'mvn clean package -DskipTests -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
            }
        }

        stage('Project Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
                    }
                }

                stage('Integration Tests') {
                    steps {
                        sh 'mvn failsafe:integration-test@it-tests -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-Server-localhost-9000') {
                    sh 'mvn clean install -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
                    sh 'mvn sonar:sonar -Dsonar.sources=src/main/java -Dsonar.java.binaries=target/classes -Dsonar.language=java -DargLine=" ${JASYPT_ENCRYPTOR_PASSWORD_VALUE} ${DOCKER_JENKINS_MYSQL_IP}"'
                }
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

    }
}
