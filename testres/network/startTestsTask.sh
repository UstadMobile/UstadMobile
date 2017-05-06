#!/usr/bin/env bash

 #Created by kileha3 on 06/04/2017
 # You may run this script simply by passing
 # the serials of both server device and client device
 # command: sh testres/network/startTestsTask.sh SERVER_SERIAL CLIENT_SERIAL.



#export android SDK for ADB to work
export ANDROID_HOME=/Volumes/ImportantWorks/Android_Setups/sdk
export PATH=$ANDROID_HOME/platform-tools:$PATH
export PATH=$ANDROID_HOME/tools:$PATH


SERVER_SERIAL=$1; #server device serial ID
CLIENT_SERIAL=$2; #Cleint device serial ID
SERVER_FILE="serverLog.log"
CLIENT_FILE="clientLog.log"
FAILURE_TAG="FAILURES"
CLIENT_WAITING_TIME=30;

#start supernode device
adb -s $SERVER_SERIAL shell am instrument -w -r   -e debug false -e class com.toughra.ustadmobile.network.ServiceBroadcastTest com.toughra.ustadmobile.test/android.support.test.runner.AndroidJUnitRunner 2>&1 > $SERVER_FILE &
SUPER_NODE_PROCESS_ID=$!;

#Wait for CLIENT_WAITING_TIME seconds before starting client node
sleep $CLIENT_WAITING_TIME
#start client node device
adb -s $CLIENT_SERIAL shell am instrument -w -r   -e debug false -e class com.toughra.ustadmobile.network.DiscoverAndDownloadTest com.toughra.ustadmobile.test/android.support.test.runner.AndroidJUnitRunner 2>&1 > $CLIENT_FILE &
CLIENT_NODE_PROCESS_ID=$!;

wait $SUPER_NODE_PROCESS_ID $CLIENT_NODE_PROCESS_ID

if  grep -q  $FAILURE_TAG "$CLIENT_FILE" || grep -q  $FAILURE_TAG "$SERVER_FILE"
 then
    exit 1
 else
    exit 0
 fi