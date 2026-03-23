pipeline {
    agent any 
    stages {
        stage('Setup') {
            steps {
                // Iniciar servidor Appium en segundo plano
                sh 'appium & >> appium.log'
            }
        }
        stage('Emulator') {
            steps {
                // Iniciar emulador (si no usas el plugin)
                sh 'emulator -avd My_Device -no-window -no-audio &'
                // Esperar a que el emulador esté listo (boot complete)
                sh 'adb wait-for-device'
            }
        }
        stage('Test') {
            steps {
                // Ejecutar tus pruebas (ej. con Maven o Gradle)
                sh 'mvn test'
            }
        }
    }
    post {
        always {
            // Limpieza: Detener procesos para no dejar zombis
            sh 'pkill -f appium'
            sh 'adb emu kill'
        }
    }
}