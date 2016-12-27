#!/bin/bash
#
# Preprocess and run tests
#

export JAVA_HOME=/opt/jdk1.8.0_65/

export PATH=$JAVA_HOME/bin:$PATH

WORKINGDIR=$(pwd)
cd ../../testres

CTRLPORT=$1
if [ -z "${CTRLPORT}" ]; then
    CTRLPORT=8065
fi
echo "PORT is: ${CTRLPORT}"

IPADDR=$(/sbin/ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)


./runserver.sh ${CTRLPORT}
SERVERPID=$(cat DodgyHTTPD/dodgyhttpd.pid)

cd $WORKINGDIR

./updatecore

sed s/__TESTSERVERIP__/$IPADDR/g ../../core/test/com/ustadmobile/test/core/TestConstants.java | \
    sed s/__TESTSERVERPORT__/$CTRLPORT/g \
    > ./src/com/ustadmobile/test/core/TestConstants.java

