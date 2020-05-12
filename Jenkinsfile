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

/**
* Pipeline Stages
*/
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
        echo 'Building ' + artifactName + '-' + artifactVersion
		withEnv(["MVN_HOME=$mvnCmd"]) {
			// Run the maven build
			sh '''
				${MVN_HOME} -DskipTests clean package
			   '''
		}
    }
    
    stage ('Test and Static Code Analysis') {
    	withEnv(["MVN_HOME=$mvnCmd"]) {
	        parallel (
	           'Unit Test': {
	            	echo '### Running Unit Test ###'
		            sh '''
		            	${MVN_HOME} -DskipTests=false -Dmaven.test.failure.ignore=false test
		               '''	
	           },
	           'Static Code Analysis': {
	                echo '### Running SonarQuebe on Source Code ###'
			        def scannerHome = tool 'SonarQubeScanner'
			        
			        withSonarQubeEnv('SonarQube') {
			           sh ''' 
			           		${MVN_HOME} sonar:sonar -Dsonar.java.coveragePlugin=jacoco -Dsonar.junit.reportsPath=target/surefire-reports  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml 
	                      '''
			        }
	           }
	         )
         } 
    }
	
	stage('Build Image') {
		withEnv(["namespace=$params.namespace", "appName=$params.appName", "tag=$tag", "artifactName=$artifactName", "artifactVersion=$artifactVersion"]) {
			
            script {
                openshift.withCluster() {
                	openshift.withProject(env.namespace) {
                    def bcExists = openshift.selector("bc", "${appName}").exists()
                    def dcExists = openshift.selector("dc", "${appName}").exists()
                    if (!bcExists) {
                    	echo '### Creating BuildConfig in Namespace: ' + env.namespace + ' ###'
            	    	sh '''
		                   		oc new-build --name=${appName} --image-stream=openjdk:8 --binary=true -n ${namespace}
		                   '''    
                   	} else {
                   	      echo '### The BuildConfig already exists in namespace: ' + env.namespace + ' ###'
                   	}
                   	
                   	if (!dcExists) {
                   	    echo '### Creating DeploymentConfig ###' 
                   		sh '''
								oc new-app --name=${appName} --image-stream=${namespace}/${appName}:0.0-0 --allow-missing-imagestream-tags=true -n ${namespace}
								oc set resources dc ${appName} --limits=memory=800Mi,cpu=1000m --requests=memory=600Mi,cpu=500m
								oc set triggers dc/${appName} --remove-all -n ${namespace}
		                   '''
		               echo '### Creating Service ###' 
                   	   sh '''
                   	   			oc expose dc ${appName} --port 8080 -n ${namespace}
                   	      '''
                   	   echo '### Creating Route ###' 
                   	   sh '''
                   	   		 	oc expose svc ${appName} -n ${namespace}
                   	      '''
                   	} else {
                   	      echo '### The DeploymentConfig already exists in namespace: ' + env.namespace + ' ###'
                   	  }
                  }
                }
            }
            
            echo '### Starting Build ###'
            script {
                openshift.withCluster() {
                  openshift.withProject(env.namespace) {
                    openshift.selector("bc", "${appName}").startBuild("--from-dir=.", "--wait=true", "--follow=true")
                  }
                }
            }
            
            echo '### Tagging Image ###'
            sh '''
            		oc tag ${appName}:latest ${appName}:${tag} -n ${namespace}
               '''
		}	
    }
    
	stage('Deploy to DEV') {
		withEnv(["DEV_PROJECT=$params.namespace", "APP_NAME=$params.appName"]) {
			echo '### Creating a new app in Namespace: ' + env.DEV_PROJECT + ' ###'
			sh "oc set image dc/${params.appName} ${params.appName}=${params.namespace}/${params.appName}:${devTag} --source=imagestreamtag -n ${params.namespace}"
	        sh "oc rollout latest dc/${params.appName} -n ${params.namespace}"
	        
	        def dc_version = sh(script:"oc get dc/${params.appName} -o=yaml -n ${params.namespace} | grep 'latestVersion'| cut -d':' -f 2" , returnStdout:true).trim();
	        echo "Version de DeploymentConfig Actual ${dc_version}"
	        
	        def rc_replicas = sh(returnStdout: true, script: "oc get rc/${params.appName}-${dc_version} -o yaml -n ${params.namespace} |grep -A 5  'status:' |grep 'replicas:' | cut -d ':' -f2").trim()
	        def rc_replicas_ready = sh(returnStdout: true, script: "oc get rc/${params.appName}-${dc_version} -o yaml -n ${params.namespace} |grep -A 5  'status:' |grep 'readyReplicas:' | cut -d ':' -f2").trim()
	
	        echo "Replicas Deseadas ${rc_replicas} - Replicas Listas ${rc_replicas_ready}"
		}
	}
	
}
