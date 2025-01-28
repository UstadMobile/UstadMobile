#!/bin/bash

# Run Maestro tests using Maestro Cloud

SCRIPTDIR=$(realpath $(dirname $0))
cd $SCRIPTDIR


if [ "$MAESTRO_CLOUD_PROJECTID" == "" ]; then
  echo "Must set Maestro cloud project id as MAESTRO_CLOUD_PROJECTID environment var"
  exit 1
fi

if [ "$MAESTRO_CLOUD_APIKEY" == "" ]; then
  echo "Must set Maestro cloud API key as MAESTRO_CLOUD_APIKEY environment var"
  exit 1
fi

if [ "$TESTCONTROLLER_URL" == "" ]; then
  echo "TESTCONTROLLER_URL environment var must be set"
  exit 1
fi

if [ "$TESTCONTROLLER_PORT_RANGE" == "" ]; then
  echo "TESTCONTROLLER_PORT_RANGE environment var must be set: e.g. 8000-9000 as allowed by firewall"
  exit 1
fi

function cleanup() {
    if [ "$TESTCONTROLLER_PID" != "" ]; then
        wget -qO- "${TESTCONTROLLER_URL}stop"
        kill $TESTCONTROLLER_PID
    fi
}

trap cleanup EXIT

#Delete all previous results.
if [ -e build ]; then
    rm -r build
fi

if [ -e log ]; then
    rm -r log
fi

mkdir -p build/results
mkdir -p build/reports/maestro


echo "run-maestro-cloud-ci: Starting testserver-controller on URL: $TESTCONTROLLER_URL"

java -jar ../../testserver-controller/build/libs/testserver-controller-all.jar \
  -P:url=$TESTCONTROLLER_URL -P:srcRoot=../../ -P:mode=maestro \
  -P:portRange=$TEST_LEARNINGSPACE_PORTRANGE &
TESTCONTROLLER_PID=$!

wait-port $TESTCONTROLLER_HOST:$TESTCONTROLLER_PORT
WAIT_PORT_STATUS=$?

if [ "$WAIT_PORT_STATUS" != "0" ]; then
    echo "run-maestro-cloud-ci: wait-port on $TESTCONTROLLER_HOST:$TESTCONTROLLER_PORT failed!"
    exit 2
fi

echo "run-maestro-cloud-ci: Time to run Maestro tests"


maestro cloud \
    --api-key=$MAESTRO_CLOUD_APIKEY \
    --project-id=$MAESTRO_CLOUD_PROJECTID \
    --app-file=../../app-android/build/outputs/apk/release/app-android-release.apk \
    --flows=e2e-tests \
    --include-tags=hello_world \
    --format=junit \
     --output build/results/report.xml \
    -e TESTCONTROLLER_URL=$TESTCONTROLLER_URL



