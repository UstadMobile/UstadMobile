#!/bin/bash
# ARG 1 PORT
# ARG 2 HOST

if [ "$1" == "" ]; then
    echo "Please specify port as first argument"
    exit 1
fi

if [ "$2" == "" ]; then
    echo "Please specify host e.g. IP Address"
    exit 1
fi

if [ -e build/server.pid ]; then
    echo "Server already running according to build/server.pid as $(cat build/server.pid). Please stop it first. If this is incorrect, remove the file."
    exit 1
fi

java -jar build/libs/lib-http-testserver-all.jar -port=$1 -host=$2 > build/out.log 2>&1 &
echo $! > build/server.pid
echo "Started lib-http-testserver and saved PID"

