#!/bin/bash

if [ "$ANDROID_HOME" == "" ]; then
    echo "Please set ANDROID_HOME variable (eg. ~/Android/Sdk) then run again"
    exit 1
fi


if [ "$EMULATOR_BIN" == "" ]; then
    EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
fi

if [ "$AVDMANAGER_BIN" == "" ]; then
    AVDMANAGER_BIN="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
fi

# Run Ustad Mobile Maestro tests in CI Environment.
# Creates, starts, waits for, and then deletes multiple emulators.
# The test controller port should be in the environment variable TESTCONTROLLER_PORT

AVD_PORT=5554

EMULATOR_CONFIG="system-images;android-33;google_apis;x86_64"

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8075
fi

NUM_EMULATORS=3
ANDROID_SERIAL=""
EMULATOR_SERIALS=()
AVD_NAMES=()

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
            raise "No emulator ports available"
        fi
    done
}


function wait_for_emulator_ready() {
    RETVAL=1
    adb -s "$ANDROID_SERIAL" wait-for-device
    while [ "$RETVAL" != "0" ]; do
        adb -s $ANDROID_SERIAL shell pm list packages > /dev/null
        RETVAL=$?
        sleep 1
    done
}

function cleanup() {
    for serial in ${EMULATOR_SERIALS[@]}; do
        adb -s $serial emu kill
        echo "Stopped emulator $serial"
    done

    for avdname in ${AVD_NAMES[@]}; do
        $AVDMANAGER_BIN delete avd -n $avdname
    done
}


echo "no" > no.tmp
for ((i = 1; i <= $NUM_EMULATORS; i++)); do
    #avdmanager will ask if you want to create a custom hardware profile (even if set to silent)
    #answer no using < no.tmp
    AVDNAME=maestro-ci-$TESTCONTROLLER_PORT-$i
    $AVDMANAGER_BIN create avd -n $AVDNAME -k 'system-images;android-33;google_apis;x86_64' < no.tmp
    echo "Created $AVDNAME"
    AVD_NAMES+=("$AVDNAME")
    find_free_emulator_port

    $EMULATOR_BIN -avd $AVDNAME -no-audio -no-window -wipe-data -port $AVD_PORT &
    echo "Started $AVDNAME"
    EMULATOR_SERIALS+=("emulator-$AVD_PORT")
    AVD_PORT=$((AVD_PORT+2))
done

for serial in ${EMULATOR_SERIALS[@]}; do
    ANDROID_SERIAL=$serial
    wait_for_emulator_ready
    echo "$ANDROID_SERIAL ready"
done

# Ready to run maestro tests on created/ready devices

echo "Time to run Maestro tests"
sleep 20

trap cleanup EXIT
