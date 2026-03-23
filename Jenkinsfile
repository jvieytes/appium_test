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
        APPIUM_BASE_PATH = '/wd/hub'
    }

    stages {
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

        stage('Cleanup old emulators') {
            steps {
                bat '''
                    for /f "tokens=1" %%i in ('adb devices ^| findstr /R "^emulator-"') do (
                        echo Cerrando %%i
                        adb -s %%i emu kill
                    )
                '''
            }
        }

        stage('Start Appium') {
            steps {
                bat '''
                    start "appium" /B cmd /c "appium server --address 127.0.0.1 --port %APPIUM_PORT% --base-path %APPIUM_BASE_PATH% > appium.log 2>&1"

                    powershell -NoProfile -Command ^
                      "$ok=$false; ^
                      for($i=0; $i -lt 30; $i++){ ^
                        try { ^
                          Invoke-WebRequest -UseBasicParsing http://127.0.0.1:%APPIUM_PORT%%APPIUM_BASE_PATH%/status | Out-Null; ^
                          $ok=$true; break ^
                        } catch { ^
                          Start-Sleep -Seconds 2 ^
                        } ^
                      } ^
                      if(-not $ok){ exit 1 }"
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                bat '''
                    start "emulator" /B cmd /c "emulator @%EMULATOR_NAME% -port 5554 -no-window -no-audio -no-boot-anim -no-snapshot-load > emulator.log 2>&1"

                    set ANDROID_SERIAL=%EMULATOR_SERIAL%

                    adb -s %EMULATOR_SERIAL% wait-for-device

                    powershell -NoProfile -Command ^
                      "$ok=$false; ^
                      for($i=0; $i -lt 60; $i++){ ^
                        $boot=(adb -s %EMULATOR_SERIAL% shell getprop sys.boot_completed).Trim(); ^
                        if($boot -eq '1'){ $ok=$true; break } ^
                        Start-Sleep -Seconds 5 ^
                      } ^
                      if(-not $ok){ exit 1 }"

                    adb -s %EMULATOR_SERIAL% shell input keyevent 82
                    adb devices
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

    post {
        always {
            bat '''
                adb -s %EMULATOR_SERIAL% emu kill
                taskkill /F /IM node.exe /T
                adb kill-server
            '''
            archiveArtifacts allowEmptyArchive: true, artifacts: 'appium.log, emulator.log, allure-results/**'
            junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml'
        }
        success {
            allure results: [[path: 'allure-results']]
        }
    }
}