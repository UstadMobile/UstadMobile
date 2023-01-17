#!/bin/bash

BASEDIR="$(realpath $(dirname $0))"

if [ ! -e $BASEDIR/build/server.pid ]; then
    echo "No build/server.pid file. Nothing to stop."
fi

wget -qO- http://localhost:8075/stop

PID=$(cat $BASEDIR/build/server.pid)
kill $PID
echo "Stopped server running as PID $PID"
rm $BASEDIR/build/server.pid

#Generate report
java -classpath $BASEDIR/build/libs/testserver-controller-all.jar \
  com.ustadmobile.test.http.AdbVideoReportMakerKt $BASEDIR/../
