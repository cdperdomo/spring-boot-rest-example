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
        echo 'Building ' + ARTIFACT
        // Run the maven build
        sh '''
			${mvnCmd} -DskipTests clean package
		   '''
    }
	
	stage('Build Image') {
		withEnv(["namespace=$params.namespace", "appName=$params.appName", "tag=$tag", "artifactName=$artifactName", "artifactVersion=$artifactVersion"]) {
			script {
				openshift.withCluster() {
					openshift.withProject(env.namespace) {
						if (openshift.selector("bc", env.appName).exists()) { 
							echo "Exist: ${appName}"
						} else {
							echo "Creating Build Config: ${appName}, tag: ${tag}"
							sh '''
									oc new-build openjdk:8 --name=${appName} --binary=true -n ${namespace}
									oc start-build ${appName} --from-file=./target/${artifactName}-${artifactVersion}.jar --wait=true -n ${namespace}
									oc tag ${appName}:latest ${appName}:${tag} -n ${namespace}
						       '''
						}
					}
				}
			}	
		}	
    }
}
