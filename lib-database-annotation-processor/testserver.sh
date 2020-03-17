#!/bin/bash

if [ "$1" == "start" ]; then
  mkdir -p build/tmp
  java -jar build/libs/lib-database-annotation-processor-tests.jar &>testserver.log &
  echo "$!" > build/tmp/testserver.pid
  echo "Started testserver: pid $(cat testserver.pid). Stdout is being written to build/tmp/testserver.log"
elif [ "$1" == "stop" ]; then
  PID=$(cat build/tmp/testserver.pid)
  echo "Stopping testserver PID $PID"
  kill $PID && rm build/tmp/testserver.pid
else
  echo "Usage: start|stop"
fi

