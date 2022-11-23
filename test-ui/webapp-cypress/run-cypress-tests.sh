#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'u:p:e:t:a:' --long 'serial1:,username:,password:,endpoint:,tests:,apk:' -n 'run-maestro-tests.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
TESTAPK=$SCRIPTDIR/../../app-android-launcher/build/outputs/apk/release/app-android-launcher-release.apk

# Alias for maestro with common variables

while true; do
        case "$1" in
             '-u'|'--username')
                	TESTUSER=$2
                        shift 2
                        continue
                ;;   
             '-p'|'--password')
                   	TESTPASS=$2
                        shift 2
                       continue
                ;;
              '-e'|'--endpoint')
                    ENDPOINT=$2
                      shift 2
                     continue
               ;;
               '-t'|'--tests')
                     echo "Set tests to $2"
                     TESTS=$2
                     shift 2
                     continue
               ;;
               '-a'|'--apk')
                     echo "Set APK to $2"
                     TESTAPK=$2
                     shift 2
                     continue
               ;;
               '--')
                        shift
                        break
                ;;
        
	esac
done

BASEDIR=$(pwd)

if [ "$TESTS" == "" ]; then
  TESTS=$(ls $SCRIPTDIR/runtest.sh)
fi

echo "Username=$TESTUSER"

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" == "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

echo "ENDPOINT=$ENDPOINT"
MAESTRO_BASE_OPTS="--device=$TESTSERIAL test -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER -e PASSWORD=$TESTPASS "


if [ ! -d $SCRIPTDIR/results ]; then
  mkdir $SCRIPTDIR/results
fi

if [ -e $SCRIPTDIR/results/results-summary.txt ]; then
  rm $SCRIPTDIR/results/results-summary.txt
fi


for TESTFILE in $TESTS; do
  TESTABSPATH=$(realpath $TESTFILE)
  export ANDROID_SERIAL=$TESTSERIAL
  adb shell rm /sdcard/Download/*

  if [ -e $TESTABSPATH/results ]; then
    rm -rf $TESTABSPATH/results
  fi

  TESTDIR=$(dirname $TESTABSPATH)
  cd $TESTDIR
  source $TESTABSPATH

  if [ "$(cat $TESTDIR/results/result)" == "pass" ]; then
    echo "$(basename $TESTDIR) : PASS" >> $SCRIPTDIR/results/results-summary.txt
  fi

  if [ "$(cat $TESTDIR/results/result)" == "fail" ]; then
    echo "$(basename $TESTDIR) : FAIL" >> $SCRIPTDIR/results/results-summary.txt
  fi

  cd $BASEDIR
done

cd $WORKDIR

