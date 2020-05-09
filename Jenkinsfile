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
        echo e
    } finally {
        echo 'Always'
    }
}

def pipeline() {
    echo "Namespace: ${params.namespace}"
    echo "Application name: ${params.appName}"

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
	}
	
	stage('Checkout Source') {
	    checkout scm  
	} 
	
	stage('cleanup') {
		withEnv(["namespace=$params.namespace", "appName=$params.appName"]) {
			script {
				openshift.withCluster() {
					openshift.withProject(env.namespace) {
						if (openshift.selector("bc", env.appName).exists()) { 
							echo "Exist: ${appName}"
						} else {
							echo "Not Exist: ${appName}"
						}
					}
				}
			}	
		}	
    }
}
