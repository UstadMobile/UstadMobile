#!/bin/bash

# Stop screenrecording that was started with start-screenrecord.sh
# stop-screenrecord.sh SERIAL VIDEOFILENAME-ON-DEVICE VIDEO-FILE-DESTINATION
SERIAL=$1
VIDEODEVICEFILENAME=$2
FILEDEST=$3

if [ "$SERIAL" == "" ]; then
  echo "Must specify serial"
  exit 1
fi

if [ "$VIDEODEVICEFILENAME" == "" ];then
  echo "Must specify video filename on device"
  exit 1
fi

if [ "$FILEDEST" == "" ];then
  echo "must specify filename to save video"
  exit 1
fi

kill $(cat results/screenrecord-$SERIAL.pid)
rm results/screenrecord-$SERIAL.pid

#Need to wait a moment for the video file to be ready - otherwise we might pull a corrupt file
sleep 2
adb -s $SERIAL pull /sdcard/Download/$VIDEODEVICEFILENAME $FILEDEST
adb -s $SERIAL shell rm /sdcard/Download/$VIDEODEVICEFILENAME
