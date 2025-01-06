#!/bin/bash

# Function to display an error message and exit with an error code
exit_with_error() {
  echo "Error: $1"
  exit 1
}

# Parse command line arguments using getopt
TEMP=$(getopt -o 'r:' --long 'spec:,controlserverurl:,learningspaceurl:' -n 'run-cypress-test.sh' -- "$@")
eval set -- "$TEMP"
unset TEMP

# Default values for options
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
SPEC=""
CONTROLSERVER_URL="http://localhost:8075/"   # Default value
LEARNING_SPACE_URL="http://localhost:8087/"  # Default value

# Parse the arguments
while true; do
    case "$1" in
        '-r'|'--spec')
            echo "Set spec to $2"
            SPEC=$2
            shift 2
            continue
            ;;
        '--controlserverurl')
            echo "Set testserver Controller URL to $2"
            CONTROLSERVER_URL=$2
            shift 2
            continue
            ;;
        '--learningspaceurl')
            echo "Set Learning Space URL to $2"
            LEARNING_SPACE_URL=$2
            shift 2
            continue
            ;;
        '--')
            shift
            break
            ;;
        *)
            break
            ;;
    esac
done

# Export the URL to Cypress environment variables
export CONTROLSERVER_URL

# Ensure the results directory exists
if [ ! -e "$SCRIPTDIR/results" ]; then
    mkdir -p "$SCRIPTDIR/results"
fi

# Clear the results directory if it contains files
if [ -d "$SCRIPTDIR/results" ] && [ "$(ls -A $SCRIPTDIR/results)" ]; then
    rm -rf "$SCRIPTDIR/results"/*
fi

# Start the control server
echo "Starting control server with site URL: $LEARNING_SPACE_URL"
$SCRIPTDIR/../../testserver-controller/start.sh --siteUrl "$LEARNING_SPACE_URL" || exit_with_error "Failed to start control server"

# Define the spec argument
if [ -n "$SPEC" ]; then
    SPECARG="$SCRIPTDIR/cypress/e2e/$SPEC.cy.js"
else
    SPECARG="$SCRIPTDIR/cypress/e2e/"
fi

# Navigate to the script directory
cd "$SCRIPTDIR" || exit_with_error "Failed to change directory to $SCRIPTDIR"

# Run npm install and Cypress tests
echo "Running 'npm install'..."
npm install || exit_with_error "Failed to run 'npm install'"

# Run Cypress tests
echo "Running Cypress tests..."
npm exec cypress run -- --env CONTROLSERVER_URL="$CONTROLSERVER_URL" --spec "$SPECARG" || exit_with_error "Cypress test run failed"

# Capture the exit status of the Cypress test run
teststatus=$?

# Stop the control server
echo "Stopping control server..."
"$SCRIPTDIR/../../testserver-controller/stop.sh" || exit_with_error "Failed to stop control server"

# Exit with the same status as the Cypress test run
exit $teststatus
