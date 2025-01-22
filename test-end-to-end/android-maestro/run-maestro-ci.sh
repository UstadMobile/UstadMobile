#!/bin/bash

# Run Ustad Mobile Maestro tests in CI Environment. See README.md for background info.
# Creates, starts, waits for, and then deletes multiple emulators.
# The test controller port should be in the environment variable TESTCONTROLLER_PORT

SCRIPTDIR=$(realpath $(dirname $0))
cd $SCRIPTDIR

if [ "$ANDROID_HOME" == "" ]; then
    echo "run-maestro-ci: Please set ANDROID_HOME variable (eg. ~/Android/Sdk) then run again"
    exit 1
fi

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8075
fi

if [ "$EMULATOR_BIN" == "" ]; then
    EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
fi

if [ "$AVDMANAGER_BIN" == "" ]; then
    AVDMANAGER_BIN="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
fi


if [ "$TESTAPK" == "" ]; then
    TESTAPK=$SCRIPTDIR/../../app-android/build/outputs/apk/release/app-android-release.apk
fi

if [ ! -e "$TESTAPK" ]; then
    echo "run-maestro-ci: Test APK not found: $TESTAPK"
    exit 2
fi

# The next AVD port to use. See find_free_emulator_port function
AVD_PORT=5554

EMULATOR_CONFIG="system-images;android-33;google_apis;x86_64"
TESTCONTROLLER_PID=""

TESTCONTROLLER_URL=http://localhost:$TESTCONTROLLER_PORT/

NUM_EMULATORS=1
ANDROID_SERIAL=""
EMULATOR_SERIALS=()
AVD_NAMES=()
APP_PACKAGE_ID="com.toughra.ustadmobile"
AVDPACKAGE="system-images;android-33;google_apis;x86_64"

if [ "$MAESTRO_SPEC" == "" ]; then
    MAESTRO_SPEC="$SCRIPTDIR/e2e-tests"
fi

# Find a free emulator port
# As per https://developer.android.com/studio/run/emulator-commandline (-port option)
# Valid emulator ports are from 5554 to 5682
function find_free_emulator_port() {
    while [ 1 ]; do
        isfree=$(netstat -alpn 2> /dev/null | grep "\:$AVD_PORT" | grep "LISTEN")
        if [ "$isfree" == "" ]; then
            break
        fi

        AVD_PORT=$((AVD_PORT+2))

        if [ $AVD_PORT -gt 5682 ]; then
            echo "run-maestro-ci: No emulator ports available"
            exit 3
        fi
    done
}


function wait_for_emulator_ready() {
    RETVAL=1
    adb -s "$ANDROID_SERIAL" wait-for-device

    # Even after wait-for-device returns, the emulator won't really be ready (commands will still
    # fail. Attempt to run the shell pm list packages command repeatedly until successful.
    while [ "$RETVAL" != "0" ]; do
        adb -s $ANDROID_SERIAL shell pm list packages > /dev/null
        RETVAL=$?
        sleep 1
    done
}

function cleanup() {
    echo "run-maestro-ci: cleaning up"
    for serial in ${EMULATOR_SERIALS[@]}; do
        adb -s $serial emu kill
        echo "run-maestro-ci: Stopped emulator $serial"
    done

    for avdname in ${AVD_NAMES[@]}; do
        $AVDMANAGER_BIN delete avd -n $avdname
    done

    if [ "$TESTCONTROLLER_PID" != "" ]; then
        wget -qO- "${TESTCONTROLLER_URL}stop"
        kill $TESTCONTROLLER_PID
    fi
}

trap cleanup EXIT

if [ ! -e build/results ]; then
    mkdir -p build/results
fi

if [ ! -e build/reports/maestro ]; then
    mkdir -p build/reports/maestro
fi

if [ ! -e build/avds ]; then
    mkdir -p build/avds
fi

echo "no" > build/no.tmp
for ((i = 1; i <= $NUM_EMULATORS; i++)); do
    #avdmanager will ask if you want to create a custom hardware profile (even if set to silent)
    #answer no using < no.tmp
    AVDNAME=maestro-ci-$TESTCONTROLLER_PORT-$i
    for ATTEMPT in {1..5}; do
        $AVDMANAGER_BIN create avd -n $AVDNAME --package "$AVDPACKAGE" --force \
            --path build/avds/$AVDNAME < build/no.tmp

        if [ -e build/avds/$AVDNAME ]; then
            echo "run-maestro-ci : Successfully created AVD $AVDNAME (attempt $ATTEMPT)"
            break 1
        elif [ "$ATTEMPT" == "5" ]; then
            echo "run-maestro-ci: Failed to create $AVDNAME after $ATTEMPT attempts"
            exit 5
        fi

        echo "run-maestro-ci: attempt $ATTEMPT to create AVD failed. Wait and retry"
        sleep 15
    done

    AVD_NAMES+=("$AVDNAME")
    find_free_emulator_port

    echo $EMULATOR_BIN -avd $AVDNAME -no-window -no-audio -wipe-data -port $AVD_PORT &
    $EMULATOR_BIN -avd $AVDNAME -no-window -no-audio -wipe-data -port $AVD_PORT &
    echo "run-maestro-ci: Started $AVDNAME"
    EMULATOR_SERIALS+=("emulator-$AVD_PORT")
    AVD_PORT=$((AVD_PORT+2))
done

for serial in ${EMULATOR_SERIALS[@]}; do
    ANDROID_SERIAL=$serial
    wait_for_emulator_ready
    echo "run-maestro-ci: $ANDROID_SERIAL ready"
done

# Still need a little extra time
sleep 15

for serial in ${EMULATOR_SERIALS[@]}; do
    for i in {1..5}; do
        echo "run-maestro-ci: Attempting to install on $serial attempt $i"
        adb -s $serial install $TESTAPK
        INSTALLSTATUS=$?
        PKGFOUND=$(adb -s $serial shell pm list packages | grep $APP_PACKAGE_ID)
        if [ "$INSTALLSTATUS" == "0" ] && [ "$PKGFOUND" != "" ]; then
            echo "run-maestro-ci: Install APK on $serial succeeded"
            break 1
        else
            echo "run-maestro-ci: Install APK on $serial failed"
            if [ "$i" == "5" ]; then
                echo "Failed to install APK $TESTAPK on $serial after $i attempts"
                exit 2
            fi

            sleep 15
        fi
    done

    adb -s $serial reverse tcp:$TESTCONTROLLER_PORT tcp:$TESTCONTROLLER_PORT

    for i in {1..5}; do
        adb -s $serial push ../test-files/content/* /sdcard/Download/
        PUSHSTATUS=$?
        if [ "$PUSHSTATUS" == "0" ]; then
            echo "run-maestro-ci: push files on $serial succeeded"
            break 1
        else
            echo "run-maestro-ci: push files on $serial failed"
            sleep 15
        fi
    done

done

# Ready to run maestro tests on created/ready devices

echo "run-maestro-ci: Time to run Maestro tests"

java -jar ../../testserver-controller/build/libs/testserver-controller-all.jar \
  -P:url=$TESTCONTROLLER_URL -P:srcRoot=../../ -P:mode=maestro &
TESTCONTROLLER_PID=$!

MAESTRO_DEVICE_ARG=""
for serial in ${EMULATOR_SERIALS[@]}; do
    if [ "$MAESTRO_DEVICE_ARG" != "" ]; then
        MAESTRO_DEVICE_ARG="$MAESTRO_DEVICE_ARG,"
    fi
    MAESTRO_DEVICE_ARG="$MAESTRO_DEVICE_ARG$serial"
done

# Could try using sharding here in future e.g. --shard-split=${#EMULATOR_SERIALS[@]}
maestro --device=$MAESTRO_DEVICE_ARG test -e TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
  $MAESTRO_SPEC \
  --format junit --output build/results/report.xml \
  --debug-output build/reports/maestro
TESTSTATUS=$?

exit $TESTSTATUS

