node {
    properties([
      parameters(
          [
            string(name: 'namespace', defaultValue: 'cicd', description:'Openshift default namespace'),
            string(name: 'appName', defaultValue: 'spring-boot-rest', description:'Application name')
          ]
        )
    ])
    pipeline()
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
    
    stage ('Test and Static Code Analysis') {
        parallel (
           'Unit Test': {
                withEnv(["MVN_HOME=$mvnCmd"]) {
                	echo '### Running Unit Test ###'
		            sh '''
		            	${MVN_HOME} -DskipTests=false -Dmaven.test.failure.ignore=false test
		               '''	
		        }
           },
           'Static Code Analysis': {
               echo '### Running SonarQuebe on Source Code ###'
           }
         )
     }
	
	stage('Build Image') {
		withEnv(["namespace=$params.namespace", "appName=$params.appName", "tag=$tag", "artifactName=$artifactName", "artifactVersion=$artifactVersion"]) {
			echo '### Cleaning existing resources in Namespace: ' + env.namespace + ' ###'
            sh '''
                    oc delete all -l app=${appName} -n ${namespace}
                    oc delete all -l build=${appName} -n ${namespace}
                    sleep 5
                    oc new-build openjdk:8 --name=${appName} --binary=true -n ${namespace}
               '''
               
            echo '### Starting Build ###'
            script {
                openshift.withCluster() {
                  openshift.withProject(env.namespace) {
                    openshift.selector("bc", "${appName}").startBuild("--from-dir=.", "--wait=true", "--follow=true")
                  }
                }
            }
            
            sh '''
            		oc tag ${appName}:latest ${appName}:${tag} -n ${namespace}
               '''
		}	
    }
	stage('Deploy to DEV') {
		withEnv(["DEV_PROJECT=$params.namespace", "APP_NAME=$params.appName"]) {
			 echo '### Creating a new app in Namespace: ' + env.DEV_PROJECT + ' ###'
			 sh '''
                    oc new-app ${APP_NAME}:latest -n ${DEV_PROJECT}
                    oc expose svc/${APP_NAME} -n ${DEV_PROJECT}
               '''
		}
	}
	
}
