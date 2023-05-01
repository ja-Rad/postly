pipeline {
  agent any
  stages {
    stage('Tools') {
      steps {
        echo 'Get Tools...'
        tool(name: 'maven', type: 'maven-3.9.1')
      }
    }

    stage('Clone Git Project') {
      steps {
        echo 'Clone Git Project...'
        git(branch: 'develop', changelog: true, poll: true, url: 'https://github.com/Radec24/postly.git')
      }
    }

    stage('Compile Project') {
      steps {
        echo 'Compile Project...'
        sh 'mvn clean install -DskipTests'
      }
    }

    stage('Project Tests') {
      parallel {
        stage('Unit Tests') {
          steps {
            echo 'Unit Tests...'
            sh 'mvn clean test'
          }
        }

        stage('Integration Tests') {
          steps {
            echo 'Integration Tests...'
            sh 'mvn clean verify'
          }
        }

      }
    }

    stage('SonarQube Analysis') {
      steps {
        echo 'SonarQube Analysis...'
        withSonarQubeEnv('SonarQube-localhost-9000') {
          sh 'mvn clean sonar:sonar'
        }

        waitForQualityGate true
      }
    }

    stage('SNAPSHOT Check') {
      steps {
        echo 'SNAPSHOT Check...'
        mavenSnapshotCheck(check: true)
      }
    }

    stage('Tag RELEASE') {
      steps {
        echo 'Tag RELEASE...'
      }
    }

    stage('Push To Master') {
      steps {
        echo 'Push To Master...'
      }
    }

  }
}