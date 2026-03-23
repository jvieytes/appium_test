pipeline {
    agent any

    tools {
        jdk 'jdk-170189'
        maven 'mvn-399'
        nodejs 'nodejs-24130'
    }

    environment {
        EMULATOR_NAME = 'mobile_emulator'
        EMULATOR_SERIAL = 'emulator-5554'
        APPIUM_PORT = '4723'
    }

    stages {
        stage('Reset environment') {
            steps {
                bat '''
                    @echo off
                    adb kill-server
                    taskkill /F /IM qemu-system-x86_64.exe /T >nul 2>&1
                    taskkill /F /IM emulator.exe /T >nul 2>&1
                    taskkill /F /IM node.exe /T >nul 2>&1
                    exit /b 0
                '''
            }
        }

        stage('Precheck') {
            steps {
                bat '''
                    where appium
                    where adb
                    where emulator
                    emulator -list-avds
                    adb devices
                    appium driver list --installed
                '''
            }
        }

        stage('Start Appium') {
            steps {
                bat '''
                    start "appium" /B cmd /c "appium server --address 127.0.0.1 --port %APPIUM_PORT% > appium.log 2>&1"
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                bat '''
                    start "emulator" /B cmd /c "emulator @%EMULATOR_NAME% -port 5554 -no-window -no-audio -no-boot-anim -no-snapshot-load > emulator.log 2>&1"
                    adb -s %EMULATOR_SERIAL% wait-for-device
                '''
            }
        }

        stage('Run Tests') {
            steps {
                bat '''
                    mvn clean test -Dgroups="regression"
                '''
            }
        }
    }

    post {
        always {
            bat '''
                @echo off
                adb -s %EMULATOR_SERIAL% emu kill >nul 2>&1
                adb kill-server
                taskkill /F /IM qemu-system-x86_64.exe /T >nul 2>&1
                taskkill /F /IM emulator.exe /T >nul 2>&1
                taskkill /F /IM node.exe /T >nul 2>&1
                exit /b 0
            '''

            junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'appium.log, emulator.log, target/allure-results/**'

            allure(
                includeProperties: false,
                jdk: '',
                results: [[path: 'target/allure-results']]
            )
        }
    }
}