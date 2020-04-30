node {
    // Get the Maven tool.
    // ** NOTE: This 'M3' Maven tool must be configured
    // **       in the global configuration.           
    def mvnHome = tool 'maven-3'
    def PROJECT_NAME = 'camel-spring-boot'
    def APP_NAME = 'spring-boot-rest-example'
    
    echo 'Starting Pipeline'
    
    stage('Check Out Code') { // for display purposes
        // Get some code from a GitHub repository
        git branch: 'master', url: 'https://github.com/CarlosPerdomo/spring-boot-rest-example.git'
    }
    
    stage('Compilation Check') {
        // Run the maven build
        withEnv(["MVN_HOME=$mvnHome"]) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore clean package'
        }
    }
    
    stage('Run Unit Tests') {
        // Run the maven build
        withEnv(["MVN_HOME=$mvnHome"]) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.skip.test=false -Dmaven.test.failure.ignore=true test'
        }
    }
    
    stage('Static Code Analysis') {
        echo '### Running SonarQuebe on code ###'
    }
    
    stage('OpenShift Commands') {
        echo '### Listing Pods in Project ###'
        sh '''
                oc get pods -n $PROJECT_NAME
           '''
           
        echo '### Creating a new app in DEV env ###'
        script {
            openshift.withCluster() {
              openshift.withProject(env.DEV_PROJECT) {
                openshift.selector("bc", "${APP_NAME}").startBuild("--from-file=target/springbootdemo-0.0.1-SNAPSHOT.jar", "--wait=true", "--follow=true")
              }
            }
        }
    }
    
    echo 'End Pipeline'
}
