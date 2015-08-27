#!/bin/bash

if [ -d DodgyHTTPD ]; then
    cd DodgyHTTPD
    git pull
else
    git clone https://github.com/UstadMobile/DodgyHTTPD.git
    cd DodgyHTTPD
fi

mvn compile
mvn exec:java -Dexec.mainClass="com.ustadmobile.dodgyhttpd.DodgyHTTPDServer" -Dexec.args='-d "../assets" -p 8065' &
SERVERPID=$!
echo "Server ID is $SERVERPID"
echo $SERVERPID > dodgyhttpd.pid

