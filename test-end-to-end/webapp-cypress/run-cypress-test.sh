#!/bin/bash

# Function to display an error message and exit with an error code
exit_with_error() {
  echo "Error: $1"
  exit 1
}

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e:c:r' --long 'serial1:,username:,password:,endpoint:,console-output,spec:' -n 'run-cypress-test.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

# Default values for options
TESTSERIAL=""
TESTUSER="admin"
TESTPASS="testpass"
SPEC=""
USECONSOLEOUTPUT=0

ENDPOINT=""
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
CONTROLSERVER=""



while true; do
        case "$1" in
             '-s'|'--serial1')
               TESTSERIAL=$2
               shift 2
               continue
               ;;
             '-u'|'--username')
               TESTUSER=$2
               shift 2
               continue
               ;;
             '-p'|'--password')
               TESTPASS=$2
               shift 2
               continue
               ;;
            '-e'|'--endpoint')
              ENDPOINT=$2
              shift 2
              continue
              ;;
            '-c'|'--console-output')
              echo "Use console output"
              USECONSOLEOUTPUT=1
              shift 1
              continue
              ;;
            '-r'|'--spec')
              echo "Set spec to $2"
              SPEC=$2
              shift 2
              continue
              ;;
         '--')
                        shift
                        break
                ;;

	esac
done

# Get the host IP address
IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)

# Set default values for endpoint and control server if not provided
if [ "$ENDPOINT" = "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

if [ "$CONTROLSERVER" = "" ]; then
  CONTROLSERVER="http://localhost:8075/"
fi

# Ensure the results directory exists
if [ ! -e $SCRIPTDIR/results ]; then
  mkdir $SCRIPTDIR/results
fi

# Start control server
$SCRIPTDIR/../../testserver-controller/start.sh  || exit_with_error "Failed to start control server"

# Define the spec argument
SPECARG=$SPEC
if [ "$SPEC" != "" ]; then
  SPECARG="$SCRIPTDIR/cypress/e2e/$SPEC.cy.js"
else
  SPECARG="$SCRIPTDIR/cypress/e2e/"
fi

# Define the output arguments for Cypress
if [ "$USECONSOLEOUTPUT" -eq 1 ]; then
  OUTPUTARGS=""
else
  OUTPUTARGS="--reporter junit --reporter-options mochaFile=results/my-test-output.xml,toConsole=true"
fi

cd $SCRIPTDIR

# Run npm install and Cypress tests
npm install || exit_with_error "Failed to run 'npm install'"
npm exec cypress run --spec "$SPECARG" $OUTPUTARGS || exit_with_error "Cypress test run failed"

# Capture the exit status of the Cypress test run
teststatus=$?

# Stop the control server
"$SCRIPTDIR/../../testserver-controller/stop.sh" || exit_with_error "Failed to stop control server"

# Exit with the same status as the Cypress test run
exit $teststatus

