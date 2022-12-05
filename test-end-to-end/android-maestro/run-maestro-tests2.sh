#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e:t:a:' --long 'serial1:,username:,password:,endpoint:,tests:,apk:' -n 'run-maestro-tests.sh' -- "$@")


eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
TESTAPK=$SCRIPTDIR/../../app-android-launcher/build/outputs/apk/release/app-android-launcher-release.apk
CONTROLSERVER=""

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

adb reverse tcp:8075 tcp:8075


maestro test -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER \
         -e PASSWORD=$TESTPASS -e CONTROLSERVER=$CONTROLSERVER \
         -e TESTSERIAL=$TESTSERIAL \
         --format junit \
         --output $SCRIPTDIR/results/report.xml \
         $SCRIPTDIR/e2e-tests

$SCRIPTDIR/../../testserver-controller/stop.sh


