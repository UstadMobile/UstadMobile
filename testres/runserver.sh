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
fi

SERVER_ARGS="$SERVER_ARGS '"

mvn compile
echo mvn exec:java -Dexec.mainClass="com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" -Dexec.args="-d ../assets -p 8065 -r $RESULTDIR"

mvn exec:java -Dexec.mainClass="com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" -Dexec.args="-d ../assets -p 8065 -r $RESULTDIR" &
SERVERPID=$!
echo "Server ID is $SERVERPID"
echo $SERVERPID > dodgyhttpd.pid

