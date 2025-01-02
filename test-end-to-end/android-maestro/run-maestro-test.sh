#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'hs:u:p:t:a:c:r:e:l' --long 'help,serial1:,username:,password:,test:,apk:,console-output,result:,testserverControllerUrl:,learningSpaceUrl:' -n 'run-maestro-tests.sh' -- "$@")


eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
TEST=""
SCRIPTDIR=$(realpath $(dirname $0))
TESTAPK=$SCRIPTDIR/../../app-android/build/outputs/apk/release/app-android-release.apk
TESTRESULTSDIR=""
TESTSERVER_URL=""
USECONSOLEOUTPUT=0
LEARNING_SPACE_URL=""

echo $SCRIPTDIR
while true; do
        case "$1" in
             '-h'|'--help')
                  echo "run-maestro-test.sh"
                  echo "-h | --help print this help message"
                  echo "-s | --serial1 (serial) the android device serial (as per adb devices) - required"
                  echo "-u | --username (username)  admin username"
                  echo "-p | --password (password) admin password"
                  echo "-t | --test (testname) specify a specific test to run e.g. the filename of a test in e2e-tests (without .yaml extension)"
                  echo "-a | --apk (apk-path) apk to install (defaults to release apk from app-android module)"
                  echo "-c | --console-output use console output mode with Maestro"
                  echo "-r | --result (result-dir) directory to save junit test results"
                  echo "-e | --testserverControllerUrl url that run the testserver controller"
                  echo "-l | --learningSpaceUrl (http-endpoint)url for the learning space server to connect to"
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
              '-e'|'--testserverControllerUrl')
                    echo "Set testserver Controller URL to $2"
                    TESTSERVER_URL=$2
                    shift 2
                    continue
              ;;
              '-l'|'--learningSpaceUrl')
                    echo "Learning Space Url"
                    LEARNING_SPACE_URL=$2
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
  echo "Please specify adb device serial using --serial1 param or use --help to see all options"
  exit 1
fi

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$LEARNING_SPACE_URL" = "" ]; then
    LEARNING_SPACE_URL="http://$IPADDR:8087/"
fi

# Set default TESTSERVER_URL if not explicitly provided
if [ "$TESTSERVER_URL" == "" ]; then
    TESTSERVER_URL="http://localhost:8075/"
fi

# Check if the URL provided (or default) is valid
if [[ ! "$TESTSERVER_URL" =~ ^http://.*$ && ! "$TESTSERVER_URL" =~ ^https://.*$ ]]; then
    echo "Error: Invalid testserver Controller URL format. Ensure it starts with http:// or https://."
    exit 1
fi

if [ "$TESTRESULTSDIR" == "" ]; then
  TESTRESULTSDIR="$SCRIPTDIR/build/results/$TESTSERIAL"
fi

if [ ! -e $TESTRESULTSDIR ]; then
  mkdir -p $TESTRESULTSDIR
fi

# Check if APK exists
if [ ! -f "$TESTAPK" ]; then
    echo "Error: Release APK file not found at $TESTAPK."
    echo "Please ensure the APK is built (This should normally be the release APK)."
    echo "Refer to the README for build instructions."
    exit 1
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
$SCRIPTDIR/../../testserver-controller/start.sh --siteUrl $LEARNING_SPACE_URL --resultsDir $TESTRESULTSDIR

export ANDROID_SERIAL=$TESTSERIAL

# Extract the port from the URL
if [[ "$TESTSERVER_URL" =~ :([0-9]+) ]]; then
    TESTSERVER_PORT="${BASH_REMATCH[1]}"
else
    echo "Error: Invalid testserver Controller URL format. Ensure it includes a port (e.g., http://ip:port/)."
    exit 1
fi

# Set up adb reverse using the extracted port
echo "Setting up adb reverse on port $TESTSERVER_PORT"
adb reverse tcp:$TESTSERVER_PORT tcp:$TESTSERVER_PORT

if [ "$(adb shell pm list packages com.toughra.ustadmobile)" != "" ]; then
  adb shell pm uninstall com.toughra.ustadmobile
fi

if [ "$(adb shell pm list packages com.toughra.ustadmobile2)" != "" ]; then
  adb shell pm uninstall com.toughra.ustadmobile2
fi

adb install $TESTAPK
INSTALL_STATUS=$?

if [ $INSTALL_STATUS -ne 0 ]; then
  echo "Error: APK installation failed. Exiting."
  exit 1
fi

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

maestro  --device=$TESTSERIAL  test -e LEARNING_SPACE_URL=$LEARNING_SPACE_URL -e USERNAME=$TESTUSER \
         -e PASSWORD=$TESTPASS -e CONTROLSERVER=$CONTROLSERVER \
         -e TESTSERIAL=$TESTSERIAL $TESTARG -e TEST=$TEST -e TESTRESULTSDIR=$TESTRESULTSDIR \
         -e TESTSERVER_URL=$TESTSERVER_URL  # $OUTPUTARGS

$SCRIPTDIR/../../testserver-controller/stop.sh

#Uninstall when finished
adb shell pm uninstall com.toughra.ustadmobile

exit $TESTSTATUS
