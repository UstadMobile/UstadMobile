#!/bin/bash

BASEDIR="$(realpath $(dirname $0))"

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
java -jar $BASEDIR/build/libs/testserver-controller-all.jar &
TESTSERVERPID=$!
echo $TESTSERVERPID > $BASEDIR/build/server.pid
echo "Started testserver-controller and saved PID [ $TESTSERVERPID ]"

