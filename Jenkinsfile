node {
    properties([
      parameters(
          [
            string(name: 'namespace', defaultValue: 'jenkins', description:'Openshift default namespace'),
            string(name: 'appName', defaultValue: 'spring-boot-rest-example', description:'Application name')
          ]
        )
    ])
    try {
        pipeline()
    } catch (e) {
		throw e
    } finally {
        echo 'Always'
    }
}

def pipeline() {
    echo "Namespace: ${params.namespace}"
    echo "Application name: ${params.appName}"
    def tag
	def artifactVersion
	def artifactName
	
    //Stage de Preparación y configuración de herramientas
    stage('Preparing'){
        // Maven
    	mvnHome = tool 'M3'
    	mvnCmd = "${mvnHome}/bin/mvn "
    
        // Java
    	env.JAVA_HOME=tool 'JDK8'
    	env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
    	sh 'java -version'
	}
	
	stage('Checkout Source') {
	    checkout scm
		def pom = readMavenPom file: 'pom.xml'
		artifactVersion = pom.version
		artifactName = pom.artifactId
		tag = "${artifactVersion}-" + currentBuild.number
		
		echo "La version del artefacto es: " + artifactVersion; 
		echo "El nombre de artefacto es: " + artifactName; 
		echo "tag: " + tag; 
	}
	
	stage('Compilation Check') {
        echo 'Building ' + artifactName
		withEnv(["MVN_HOME=$mvnCmd"]) {
			// Run the maven build
			sh '''
				${MVN_HOME} -DskipTests clean package
			   '''
		}
    }
	
	stage('Build Image') {
		withEnv(["DEV_PROJECT=$params.namespace", "APP_NAME=$params.appName", "tag=$tag", "ARTIFACT=$artifactName", "artifactVersion=$artifactVersion"]) {
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
}
