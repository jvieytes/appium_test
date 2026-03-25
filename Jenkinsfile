pipeline {
    agent any

    tools {
        jdk 'jdk-170189'
        maven 'mvn-399'
        nodejs 'nodejs-24130'
    }

    environment {
        APPIUM_URL = 'http://127.0.0.1:4723/'
        COMPOSE_FILE = 'compose.yml'
    }

    stages {
        stage('Precheck Docker') {
            steps {
                bat '''
                    docker version
                    docker compose version
                    docker compose -f %COMPOSE_FILE% config
                '''
            }
        }

        stage('Start Mobile Stack') {
            steps {
                bat '''
                    docker compose -f %COMPOSE_FILE% up -d --wait --wait-timeout 600
                    docker compose -f %COMPOSE_FILE% ps
                '''
            }
        }

        stage('Validate Emulator + ADB') {
            steps {
                powershell '''
                    Write-Host "Validando estado del emulador..."
                    docker exec android-emulator cat device_status

                    $ok = $false
                    for($i=0; $i -lt 24; $i++){
                        try {
                            $adb = docker exec appium-server adb devices
                            Write-Host $adb

                            $lines = $adb -split "`n"
                            foreach($line in $lines){
                                $trimmed = $line.Trim()
                                if(
                                    $trimmed -ne "" -and
                                    -not $trimmed.StartsWith("List of devices attached") -and
                                    $trimmed.EndsWith("device")
                                ){
                                    $ok = $true
                                    break
                                }
                            }

                            if($ok){
                                break
                            }
                        } catch {
                        }

                        Start-Sleep -Seconds 5
                    }

                    if(-not $ok){
                        Write-Host "Appium no detecto ningun dispositivo ADB"
                        docker compose -f %COMPOSE_FILE% ps
                        docker compose -f %COMPOSE_FILE% logs --no-color android-emulator appium
                        exit 1
                    }
                '''
            }
        }

        stage('Run Tests') {
            steps {
                bat '''
                    mvn clean test -Dgroups="regression" -Dappium.url=%APPIUM_URL%
                '''
            }
        }
    }

    post {
        always {
            bat '''
                docker compose -f %COMPOSE_FILE% ps > docker-compose-ps.log
                docker compose -f %COMPOSE_FILE% logs --no-color android-emulator > android-emulator.log
                docker compose -f %COMPOSE_FILE% logs --no-color appium > appium-server.log
            '''

            junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml'

            archiveArtifacts allowEmptyArchive: true, artifacts: '''
                docker-compose-ps.log,
                android-emulator.log,
                appium-server.log,
                target/allure-results/**
            '''

            allure(
                includeProperties: false,
                jdk: '',
                results: [[path: 'target/allure-results']]
            )

            bat '''
                docker compose -f %COMPOSE_FILE% down
            '''
        }
    }
}