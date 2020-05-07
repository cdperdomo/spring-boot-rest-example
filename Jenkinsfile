pipeline {
    
	agent any
	
	options {
        // set a timeout of 60 minutes for this pipeline
        timeout(time: 60, unit: 'MINUTES')
    }
    
    tools { 
        maven 'maven-3.6.3' 
        jdk 'jdk8' 
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
		 stage('Print ENV vars') {
            steps {
                echo '### Printing ENV Vars ###'
                sh 'printenv'
            }
        }
		
        stage('Compilation Check') {
            steps {
                echo '### Checking for compile errors ###'
                sh '''
                        mvn -s settings.xml -B clean compile
                   '''
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo '### Running unit tests ###'
                sh '''
                        mvn -s settings.xml -B clean test
                   '''
            }
        }

        stage('Static Code Analysis') {
            steps {
                echo '### Running pmd on code ###'
                sh '''
                        mvn -s settings.xml -B clean pmd:check
                   '''
            }
        }

        stage('Create fat JAR') {
            steps {
                echo '### Creating fat JAR ###'
                sh '''
                        mvn -s settings.xml -B clean package -DskipTests=true
                   '''
            }
        }
    }
}
