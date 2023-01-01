#!/bin/bash

# Starts running ADB screenrecord in the background so we can record a video of the test
# Usage:
#  start-screenrecord.sh SERIAL VIDEOFILENAME

ADBPATH=$(which adb)
SERIAL=$1
VIDFILENAME=$2

if [ ! -e results ]; then
  mkdir results
fi

if [ "$ADBPATH" == "" ]; then
  echo "Android adb command not on path. Please add it to PATH environment variable"
  exit 1
fi

if [ "$SERIAL" == "" ];then
  echo "Must specify adb serial: usage start-screenrecord.sh SERIAL VIDEOFILENAME"
  exit 1
fi

if [ "$VIDFILENAME" == "" ]; then
  echo "Must specify filename for video (e.g. video.mp4). Will be saved in download dir: "
  echo "usage start-screenrecord.sh SERIAL VIDEOFILENAME"
  exit 1
fi

adb -s $SERIAL shell screenrecord /sdcard/Download/$VIDFILENAME &
PID=$!
echo $PID > results/screenrecord-$SERIAL.pid

