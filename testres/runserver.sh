#!/bin/bash

if [ -d DodgyHTTPD ]; then
    cd DodgyHTTPD
    git pull
else
    git clone https://github.com/UstadMobile/DodgyHTTPD.git
    cd DodgyHTTPD
fi

RESULTDIR="."

if [ "$1" == '-r' ]; then
    RESULTDIR=$2
    PORT=$3
else
    PORT=$1
fi

if [ -z "$PORT" ]; then
    PORT=8065
fi

echo "Starting server at port: ${PORT}"

SERVER_ARGS="$SERVER_ARGS '"

mvn compile
echo mvn exec:java -Dexec.mainClass="com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" -Dexec.args="-d ../assets -p ${PORT} -r $RESULTDIR --codelookup ../../errorcodes.properties"

mvn exec:java -Dexec.mainClass="com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" -Dexec.args="-d ../assets -p ${PORT} -r $RESULTDIR --codelookup ../../errorcodes.properties" &
SERVERPID=$!
echo "Server ID is $SERVERPID"
echo $SERVERPID > dodgyhttpd.pid

