pipeline {
    options {
        // set a timeout of 60 minutes for this pipeline
        timeout(time: 60, unit: 'MINUTES')
    }
    agent {
      node {
        label 'maven'
      }
    }

    environment {
        DEV_PROJECT = "camel-spring-boot"
        STAGE_PROJECT = "youruser-movies-stage"
        APP_GIT_URL = "https://github.com/CarlosPerdomo/spring-boot-rest-example.git"
        NEXUS_SERVER = "http://maven.assertservices.com/repository/internal/"

        // DO NOT CHANGE THE GLOBAL VARS BELOW THIS LINE
        APP_NAME = "movies"
    }


    stages {

        stage('Compilation Check') {
            steps {
                echo '### Checking for compile errors ###'
                sh '''
                        cd ${APP_NAME}
                        mvn -s settings.xml -B clean compile
                   '''
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo '### Running unit tests ###'
                sh '''
                        cd ${APP_NAME}
                        mvn -s settings.xml -B clean test
                   '''
            }
        }

        stage('Static Code Analysis') {
            steps {
                echo '### Running pmd on code ###'
                sh '''
                        cd ${APP_NAME}
                        mvn -s settings.xml -B clean pmd:check
                   '''
            }
        }

        stage('Create fat JAR') {
            steps {
                echo '### Creating fat JAR ###'
                sh '''
                        cd ${APP_NAME}
                        mvn -s settings.xml -B clean package -DskipTests=true
                   '''
            }
        }
    }
}
