pipeline {
    agent any

    tools {
        jdk 'jdk-170189'
        maven 'mvn-399'
        nodejs 'nodejs-24130'
    }

    stages {
        stage('Precheck') {
            steps {
                bat '''
                    where appium
                    where adb
                    where emulator
                    emulator -list-avds
                    appium driver list --installed
                '''
            }
        }

        stage('Start Appium') {
            steps {
                bat '''
                    start "appium" /B cmd /c "appium server --port 4723 --base-path /wd/hub"
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                bat '''
                    start "emulator" /B cmd /c "emulator @mobile_emulator -no-window -no-audio -no-boot-anim"
                    adb wait-for-device
                '''
            }
        }

        stage('Run Tests') {
            steps {
                bat '''
                    mvn clean test -Dcucumber.filter.tags="@mobile_emulator"
                '''
            }
        }
    }
}