#!/bin/bash
#
# Preprocess and run tests
#
# To update only the config (e.g. IP address of test server)
# run ./runtests updateonly

WORKINGDIR=$(pwd)
cd ../../testres

CTRLPORT=8065
UPDATEONLY=$1
SERVERPID=0

IPADDR=$(/sbin/ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)

if [ "$UPDATEONLY" != "updateonly" ]; then
    ./runserver.sh -r $WORKINGDIR
    SERVERPID=$(cat DodgyHTTPD/dodgyhttpd.pid)
fi

cd $WORKINGDIR

ant -lib ../../core/lib/com.objfac.ant.preprocess_0.9.1/preprocessor.jar \
     -f preproc.xml

sed s/__TESTSERVERIP__/$IPADDR/g ../../core/test/com/ustadmobile/test/core/TestConstants.java | \
    sed s/__TESTSERVERPORT__/$CTRLPORT/g \
    > ./src/androidTest/java/com/ustadmobile/test/core/TestConstants.java


if [ "$UPDATEONLY" != "updateonly" ]; then
    ./gradlew connectedAndroidTest
    echo "Kill $NODEPID node server" 
    kill $SERVERPID
fi
