#!/bin/bash

# Function to display an error message and exit with an error code
exit_with_error() {
  echo "Error: $1"
  exit 1
}

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'r' --long 'spec:' -n 'run-cypress-test.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

# Default values for options
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
SPEC=""
CONTROLSERVER=""




while true; do
        case "$1" in
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

if [ "$CONTROLSERVER" = "" ]; then
  CONTROLSERVER="http://localhost:8075/"
fi

# Ensure the results directory exists
if [ ! -e $SCRIPTDIR/results ]; then
  mkdir $SCRIPTDIR/results
fi

if [ -d "$SCRIPTDIR/results" ]; then
  # Check if there are files to delete
  if [ "$(ls -A $SCRIPTDIR/results)" ]; then
    rm -rf "$SCRIPTDIR/results"/*
fi

fi


# Start control server
$SCRIPTDIR/../../testserver-controller/start.sh --siteUrl http://localhost:8087/ || exit_with_error "Failed to start control server"

# Define the spec argument
SPECARG=$SPEC
if [ "$SPEC" != "" ]; then
  SPECARG="$SCRIPTDIR/cypress/e2e/$SPEC.cy.js"
else
  SPECARG="$SCRIPTDIR/cypress/e2e/"
fi

cd $SCRIPTDIR

# Run npm install and Cypress tests
npm install || exit_with_error "Failed to run 'npm install'"
#Note additional -- required to split up arguments between npm exec and cypress
# as per https://stackoverflow.com/questions/63854769/cypress-runs-all-test-files-even-when-spec-parameter-is-used
npm exec cypress run -- --spec "$SPECARG"  || exit_with_error "Cypress test run failed"

# Capture the exit status of the Cypress test run
teststatus=$?

# Stop the control server
"$SCRIPTDIR/../../testserver-controller/stop.sh" || exit_with_error "Failed to stop control server"

# Exit with the same status as the Cypress test run
exit $teststatus



