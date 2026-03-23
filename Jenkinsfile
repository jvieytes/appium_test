pipeline {
    agent any

    tools {
        jdk 'jdk-170189'
        maven 'mvn-399'
        nodejs 'nodejs-24130'
    }

    stages {
        stage('Validar tools') {
            steps {
                bat '''
                    echo JAVA_HOME=%JAVA_HOME%
                    where java
                    java -version
                    where mvn
                    mvn -version
                    where node
                    node -v
                    npm -v
                '''
            }
        }
    }
}