properties([pipelineTriggers([githubPush()])])

MAIL_FROM='no-reply@servientrega.com'
MAIL_TO='cdkjarkas@gmail.com'

node {
    try {
        pipeline()
    } catch (e) {
        postFailure(e)
    } finally {
        postAlways()
    }
}

def pipeline () {
    // Get the Maven tool.
    // ** NOTE: This 'M3' Maven tool must be configured
    // **       in the global configuration.           
    def MVN_HOME = tool 'maven-3'
    def DEV_PROJECT = 'camel-spring-boot'
    def APP_NAME = 'spring-boot-rest-example'
    
    echo 'Starting Pipeline'
    
    stage('Check Out Code') {
        // Get some code from a Git repository
        git branch: 'master', url: 'https://github.com/CarlosPerdomo/spring-boot-rest-example.git'
        
        IMAGE = readMavenPom().getArtifactId()
        VERSION = readMavenPom().getVersion()
        APP_NAME = IMAGE;
        ARTIFACT = IMAGE+'-'+VERSION
    }
     
    stage('Compilation Check') {
        echo 'Building ' + ARTIFACT
        // Run the maven build
        withEnv(["MVN_HOME=$MVN_HOME"]) {
            sh '"$MVN_HOME/bin/mvn" -DskipTests clean package'
        }
    }
    
    stage('Run Unit Tests') {
        // Run the maven build
        withEnv(["MVN_HOME=$MVN_HOME"]) {
            sh '"$MVN_HOME/bin/mvn" -DskipTests=false -Dmaven.test.failure.ignore=false test'
        }
    }
    
    stage('Static Code Analysis') {
        echo '### Running SonarQuebe on Source Code ###'
        def scannerHome = tool 'SonarQubeScanner'
        
        withSonarQubeEnv('SonarQube') {
            sh "${scannerHome}/bin/sonar-scanner -X"
        }
    }
    
    stage('Launch new app in DEV env') {
        withEnv(["DEV_PROJECT=$DEV_PROJECT", "APP_NAME=$APP_NAME", "ARTIFACT=$ARTIFACT"]) {
            echo '### Cleaning existing resources in DEV env ###'
            sh '''
                    oc delete all -l app=${APP_NAME} -n ${DEV_PROJECT}
                    oc delete all -l build=${APP_NAME} -n ${DEV_PROJECT}
                    sleep 5
                    oc new-build java:8 --name=${APP_NAME} --binary=true -n ${DEV_PROJECT}
               '''
            echo '### Creating a new app in DEV env ###'
            script {
                openshift.withCluster() {
                  openshift.withProject(env.DEV_PROJECT) {
                    openshift.selector("bc", "${APP_NAME}").startBuild("--from-file=target/${ARTIFACT}.jar", "--wait=true", "--follow=true")
                  }
                }
            }
            sh '''
                    oc new-app ${APP_NAME}:latest -n ${DEV_PROJECT}
                    oc expose svc/${APP_NAME} -n ${DEV_PROJECT}
               '''
        }
    }
	
    stage('Wait for deployment in DEV env') {
        withEnv(["DEV_PROJECT=$DEV_PROJECT", "APP_NAME=$APP_NAME"]) {
            script {
                openshift.withCluster() {
                    openshift.withProject( "${DEV_PROJECT}" ) {
                        openshift.selector("dc", "${APP_NAME}").related('pods').untilEach(1) {
                            return (it.object().status.phase == "Running")
                        }
                    }
                }
            }
        }
    }
    echo 'End Pipeline'
    
    mail to: MAIL_TO, 
     from: MAIL_FROM,
     subject: "Jenkins Build: ${env.JOB_NAME} - Successfully", 
     body: "Job - \"${env.JOB_NAME}\" build: ${env.BUILD_NUMBER}"
}

def postFailure(err) {
    echo "Failed because of: $err"
    echo "This will run only if failed. [FAILURE][JENKINS]  ${env.JOB_NAME} - Build # ${env.BUILD_NUMBER}. Error: $err"
    
    mail to: MAIL_TO, 
         from: MAIL_FROM,
         subject: "Jenkins Build: ${env.JOB_NAME} - Failed", 
         body: "Job Failed - \"${env.JOB_NAME}\" build: ${env.BUILD_NUMBER}\n\nView the log at:\n ${env.BUILD_URL}\n\nBlue Ocean:\n${env.RUN_DISPLAY_URL}"

    throw err
}

def postAlways() {
    echo 'This will always run'

}
