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
                    docker compose -f %COMPOSE_FILE% up -d
                    docker compose -f %COMPOSE_FILE% ps
                '''
            }
        }

        stage('Wait Appium Ready') {
            steps {
                powershell '''
                    $ok = $false
                    for($i=0; $i -lt 60; $i++){
                        try {
                            $resp = Invoke-RestMethod -Uri "http://127.0.0.1:4723/status" -Method Get -TimeoutSec 5
                            if($resp.value.ready -eq $true){
                                $ok = $true
                                break
                            }
                        } catch {
                        }
                        Start-Sleep -Seconds 5
                    }

                    if(-not $ok){
                        Write-Host "Appium no quedo listo a tiempo"
                        docker compose -f docker-compose.yml ps
                        docker compose -f docker-compose.yml logs --no-color
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
                docker compose -f %COMPOSE_FILE% logs --no-color > docker-compose.log
            '''

            junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml'

            archiveArtifacts allowEmptyArchive: true, artifacts: 'docker-compose.log, target/allure-results/**'

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