#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e:t:a:c' --long 'serial1:,username:,password:,endpoint:,test:,apk:,console-output' -n 'run-maestro-tests.sh' -- "$@")


eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
TEST=""
SCRIPTDIR=$(realpath $(dirname $0))
TESTAPK=$SCRIPTDIR/../../app-android-launcher/build/outputs/apk/release/app-android-launcher-release.apk
CONTROLSERVER=""
USECONSOLEOUTPUT=0

while true; do
        case "$1" in
             '-s'|'--serial1')
                	TESTSERIAL=$2
                        shift 2
                        continue
                ;;
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
               '-t'|'--test')
                     echo "Set test to $2"
                     TEST=$2
                     shift 2
                     continue
               ;;
               '-a'|'--apk')
                     echo "Set APK to $2"
                     TESTAPK=$2
                     shift 2
                     continue
               ;;
               '-c'|'--console-output')
                     echo "Use console output"
                     USECONSOLEOUTPUT=1
                     shift 1
                     continue
               ;;
               '--')
                        shift
                        break
                ;;

	esac
done

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" = "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

if [ "$CONTROLSERVER" = "" ]; then
  CONTROLSERVER="http://localhost:8075/"
fi

if [ -e $SCRIPTDIR/results/report.xml ]; then
  echo "Delete previous report.xml"
  rm $SCRIPTDIR/results/report.xml
fi

if [ ! -e $SCRIPTDIR/results ]; then
  mkdir $SCRIPTDIR/results
fi

# Start control server
$SCRIPTDIR/../../testserver-controller/start.sh

export ANDROID_SERIAL=$TESTSERIAL
adb reverse tcp:8075 tcp:8075

if [ "$(adb shell pm list packages com.toughra.ustadmobile)" != "" ]; then
  adb shell pm uninstall com.toughra.ustadmobile
fi
adb install $TESTAPK

TESTARG=$TEST
if [ "$TEST" != "" ]; then
  TESTARG="$SCRIPTDIR/e2e-tests/$TEST.yaml"
else
  TESTARG="$SCRIPTDIR/e2e-tests"
fi

OUTPUTARGS=" --format junit --output $SCRIPTDIR/results/report.xml "
if [ "$USECONSOLEOUTPUT" == "1" ]; then
  OUTPUTARGS=""
fi

maestro test -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER \
         -e PASSWORD=$TESTPASS -e CONTROLSERVER=$CONTROLSERVER \
         -e TESTSERIAL=$TESTSERIAL \
         $OUTPUTARGS \
         $TESTARG

$SCRIPTDIR/../../testserver-controller/stop.sh


