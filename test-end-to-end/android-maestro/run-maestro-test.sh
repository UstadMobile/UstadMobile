#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'hs:u:p:e:t:a:c:r' --long 'help,serial1:,username:,password:,endpoint:,test:,apk:,console-output,result:' -n 'run-maestro-tests.sh' -- "$@")


eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
TEST=""
SCRIPTDIR=$(realpath $(dirname $0))
TESTAPK=$SCRIPTDIR/../../app-android/build/outputs/apk/release/app-android-release.apk
TESTRESULTSDIR=""
CONTROLSERVER=""
USECONSOLEOUTPUT=0
echo $SCRIPTDIR
while true; do
        case "$1" in
             '-h'|'--help')
                  echo "run-maestro-test.sh"
                  echo "-h | --help print this help message"
                  echo "-s | --serial1 (serial) the android device serial (as per adb devices) - required"
                  echo "-u | --username (username)  admin username"
                  echo "-p | --password (password) admin password"
                  echo "-e | --endpoint (http-endpoint) the endpoint of the server to connect to"
                  echo "-t | --test (testname) specify a specific test to run e.g. the filename of a test in e2e-tests (without .yaml extension)"
                  echo "-a | --apk (apk-path) apk to install (defaults to release apk from app-android module)"
                  echo "-c | --console-output use console output mode with Maestro"
                  echo "-r | --result (result-dir) directory to save junit test results"
                  exit 0
                  ;;

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
                '-r'|'--result')
                     echo "result"
                    TESTRESULTSDIR=$2
                    shift 2
                    continue
                ;;
                '--')

                        shift
                        break
                ;;

	esac
done

if [ "$TESTSERIAL" == "" ]; then
  echo "Please specify adb device serial usign --serial1 param or use --help to see all options"
  exit 1
fi

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" = "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

if [ "$CONTROLSERVER" = "" ]; then
  CONTROLSERVER="http://localhost:8075/"
fi

if [ "$TESTRESULTSDIR" == "" ]; then
  TESTRESULTSDIR="$SCRIPTDIR/build/results/$TESTSERIAL"
fi

if [ ! -e $TESTRESULTSDIR ]; then
  mkdir -p $TESTRESULTSDIR
fi

# Create a copy of common scripts that will work on the second app id (used to test interactions
# between users)
if [ ! -e $SCRIPTDIR/build/common-app2 ]; then
  mkdir -p $SCRIPTDIR/build/common-app2
fi

for COMMONFLOWFILE in $(ls $SCRIPTDIR/common); do
    FILEBASENAME=$(basename $COMMONFLOWFILE)
    sed 's/com.toughra.ustadmobile/com.toughra.ustadmobile2/g' $SCRIPTDIR/common/$FILEBASENAME > \
      $SCRIPTDIR/build/common-app2/$FILEBASENAME

done

# Start control server
$SCRIPTDIR/../../testserver-controller/start.sh --siteUrl $ENDPOINT --resultsDir $TESTRESULTSDIR

export ANDROID_SERIAL=$TESTSERIAL
adb reverse tcp:8075 tcp:8075

if [ "$(adb shell pm list packages com.toughra.ustadmobile)" != "" ]; then
  adb shell pm uninstall com.toughra.ustadmobile
fi

if [ "$(adb shell pm list packages com.toughra.ustadmobile2)" != "" ]; then
  adb shell pm uninstall com.toughra.ustadmobile2
fi

adb install $TESTAPK

TESTARG=$TEST
if [ "$TEST" != "" ]; then
  TESTARG="$SCRIPTDIR/e2e-tests/$TEST.yaml"
else
  TESTARG="$SCRIPTDIR/e2e-tests"
fi

OUTPUTARGS=" --format junit --output $TESTRESULTSDIR/report.xml "
if [ "$USECONSOLEOUTPUT" == "1" ]; then
  OUTPUTARGS=""
fi

maestro  --device=$TESTSERIAL  test -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER \
         -e PASSWORD=$TESTPASS -e CONTROLSERVER=$CONTROLSERVER \
         -e TESTSERIAL=$TESTSERIAL $TESTARG -e TEST=$TEST -e TESTRESULTSDIR=$TESTRESULTSDIR
         #$OUTPUTARGS


TESTSTATUS=$?

#$SCRIPTDIR/../../testserver-controller/stop.sh

#Uninstall when finished
#adb shell pm uninstall com.toughra.ustadmobile

exit $TESTSTATUS
