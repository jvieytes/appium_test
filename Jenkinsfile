pipeline {
    agent { label 'android' }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        ANDROID_HOME     = '/opt/android-sdk'
        JAVA_HOME        = '/usr/lib/jvm/temurin-17-jdk'
        PATH             = "${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/emulator:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${PATH}"

        AVD_NAME         = 'My_Device'
        EMULATOR_PORT    = '5554'
        APPIUM_PORT      = '4723'
        DEVICE_SERIAL    = 'emulator-5554'
        APPIUM_BASE_PATH = '/wd/hub'
    }

    stages {
        stage('Pre-check') {
            steps {
                sh '''
                    set -euxo pipefail

                    java -version
                    adb version
                    emulator -version
                    appium -v

                    # Verifica virtualización (útil en CI)
                    emulator -accel-check || true

                    # Verifica drivers Appium instalados
                    appium driver list --installed || true
                '''
            }
        }

        stage('Start Appium') {
            steps {
                sh '''
                    set -euxo pipefail

                    rm -f appium.log appium.pid

                    nohup appium server \
                      --port "${APPIUM_PORT}" \
                      --base-path "${APPIUM_BASE_PATH}" \
                      > appium.log 2>&1 &

                    echo $! > appium.pid

                    # Esperar Appium arriba
                    for i in $(seq 1 30); do
                      if curl -fsS "http://127.0.0.1:${APPIUM_PORT}${APPIUM_BASE_PATH}/status" >/dev/null; then
                        echo "Appium iniciado correctamente"
                        exit 0
                      fi
                      sleep 2
                    done

                    echo "Appium no levantó correctamente"
                    cat appium.log || true
                    exit 1
                '''
            }
        }

        stage('Start Emulator Headless') {
            steps {
                sh '''
                    set -euxo pipefail

                    adb start-server

                    rm -f emulator.log emulator.pid

                    nohup emulator @"${AVD_NAME}" \
                      -port "${EMULATOR_PORT}" \
                      -no-window \
                      -no-audio \
                      -no-boot-anim \
                      -gpu software \
                      -no-snapshot \
                      -wipe-data \
                      > emulator.log 2>&1 &

                    echo $! > emulator.pid

                    adb wait-for-device

                    # Esperar boot real del sistema
                    BOOT_COMPLETED=""
                    for i in $(seq 1 60); do
                      BOOT_COMPLETED=$(adb -s "${DEVICE_SERIAL}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
                      if [ "${BOOT_COMPLETED}" = "1" ]; then
                        break
                      fi
                      sleep 5
                    done

                    if [ "${BOOT_COMPLETED}" != "1" ]; then
                      echo "El emulador no terminó de iniciar"
                      adb devices || true
                      cat emulator.log || true
                      exit 1
                    fi

                    # Desbloquear pantalla
                    adb -s "${DEVICE_SERIAL}" shell input keyevent 82 || true

                    # Opcional: desactivar animaciones para estabilidad
                    adb -s "${DEVICE_SERIAL}" shell settings put global window_animation_scale 0 || true
                    adb -s "${DEVICE_SERIAL}" shell settings put global transition_animation_scale 0 || true
                    adb -s "${DEVICE_SERIAL}" shell settings put global animator_duration_scale 0 || true

                    adb devices
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    set -euxo pipefail

                    mvn clean test \
                      -Dappium.server.url="http://127.0.0.1:${APPIUM_PORT}${APPIUM_BASE_PATH}" \
                      -DdeviceName="${DEVICE_SERIAL}"
                '''
            }
        }
    }

    post {
        always {
            sh '''
                set +e

                echo "===== ADB DEVICES ====="
                adb devices || true

                echo "===== APPIUM LOG ====="
                tail -200 appium.log || true

                echo "===== EMULATOR LOG ====="
                tail -200 emulator.log || true

                adb -s "${DEVICE_SERIAL}" emu kill || true

                if [ -f emulator.pid ]; then
                  kill $(cat emulator.pid) || true
                fi

                if [ -f appium.pid ]; then
                  kill $(cat appium.pid) || true
                fi

                adb kill-server || true
            '''

            archiveArtifacts artifacts: 'appium.log, emulator.log', allowEmptyArchive: true
        }
    }
}