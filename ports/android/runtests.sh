#!/bin/bash
#
# Preprocess and run tests
#

WORKINGDIR=$(pwd)
cd ../../testres

CTRLPORT=8065

IPADDR=$(/sbin/ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)

./runserver.sh -r $WORKINGDIR
SERVERPID=$(cat DodgyHTTPD/dodgyhttpd.pid)

cd $WORKINGDIR

ant -lib ../../core/lib/com.objfac.ant.preprocess_0.9.1/preprocessor.jar \
     -f preproc.xml

sed s/__TESTSERVERIP__/$IPADDR/g ../../core/test/com/ustadmobile/test/core/TestConstants.java | \
    sed s/__TESTSERVERPORT__/$CTRLPORT/g \
    > ./src/androidTest/java/com/ustadmobile/test/core/TestConstants.java


./gradlew connectedAndroidTest

echo "Kill $NODEPID node server" 
kill $SERVERPID

