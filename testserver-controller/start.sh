#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'r:s:h' --long 'resultsDir:,siteUrl:,help' -n 'start.sh' -- "$@")
eval set -- "$TEMP"
unset TEMP

BASEDIR="$(realpath $(dirname $0))"

SITEURL=""

while true; do
  echo $1 $2
  case "$1" in
    '-h'|'--help')
      echo "start.sh [OPTIONS]"
      echo "Run testserver-controller"
      echo " -r --resultsDir test results directory where results will be found (required)"
      echo " -u --siteUrl the site url via which the server will be accessed as per runserver.sh (required)"
      exit 0
      ;;
    '-r'|'--resultsDir')
      TESTRESULTSDIR=$2
      shift 2
      continue
      ;;
    '-u'|'--siteUrl')
      SITEURL=$2
      shift 2
      continue
      ;;
    '--')
      shift
      break
      ;;
  esac
done

if [ "$SITEURL" == "" ]; then
  echo "MUST specify siteUrl using -u or --siteUrl"
  exit 1
fi


if [ ! -e $BASEDIR/build/libs/testserver-controller-all.jar ]; then
  echo "Please build testserver-controller e.g. ./gradlew testserver-controller:build"
  exit 1
fi

if [ -e $BASEDIR/build/server.pid ]; then
  echo "$BASEDIR/build/server.pid exists: stopping it"
  kill $(cat $BASEDIR/build/server.pid)
  rm $BASEDIR/build/server.pid
fi

nc -z 127.0.0.1 8075
NCRESULT=$?
if [ "$NCRESULT" == "0" ]; then
  echo "Something is already running on port 8075! Please stop it before trying this again!"
  exit 1
fi

# Make the project root directory the working directory
cd $BASEDIR/..
java -jar $BASEDIR/build/libs/testserver-controller-all.jar -P:resultDir=$TESTRESULTSDIR -P:siteUrl=$SITEURL &
TESTSERVERPID=$!
echo $TESTSERVERPID > $BASEDIR/build/server.pid
echo "Started testserver-controller and saved PID [ $TESTSERVERPID ]. Site URL is $SITEURL. See ../log/testserver-controller.log for output."

