#!/bin/bash
#
# Preprocess and run tests
#

WORKINGDIR=$(pwd)
cd ../../testres

CTRLPORT=5061
ASSETPORT=5062
IPADDR=$(/sbin/ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)


nodejs node-qunit-server/node-qunit-server.js $CTRLPORT $ASSETPORT assets/ &
NODEPID=$!

cd $WORKINGDIR

ant -lib ../../core/lib/com.objfac.ant.preprocess_0.9.1/preprocessor.jar \
     -f build-preprocess-tests.xml

sed s/__TESTSERVERIP__/$IPADDR/g ../../core/test/com/ustadmobile/test/core/TestConstants.java | \
    sed s/__TESTSERVERPORT__/$ASSETPORT/g \
    > ./src/androidTest/java/com/ustadmobile/test/core/TestConstants.java

cat ./src/androidTest/java/com/ustadmobile/test/core/TestConstants.java
read

./gradlew connectedAndroidTest

echo "Kill $NODEPID node server" 
kill $NODEPID
