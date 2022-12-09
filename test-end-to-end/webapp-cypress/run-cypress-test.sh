#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e:c:r' --long 'serial1:,username:,password:,endpoint:,console-output,spec:' -n 'run-cypress-test.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
CONTROLSERVER=""
USECONSOLEOUTPUT=0
SPEC=""

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

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" = "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

if [ "$CONTROLSERVER" = "" ]; then
  CONTROLSERVER="http://localhost:8075/"
fi

if [ ! -e $SCRIPTDIR/results ]; then
  mkdir $SCRIPTDIR/results
fi

# Start control server
$SCRIPTDIR/../../testserver-controller/start.sh

SPECARG=$SPEC
if [ "$SPEC" != "" ]; then
  SPECARG="$SCRIPTDIR/cypress/e2e/$SPEC.cy.js"
else
  SPECARG="$SCRIPTDIR/cypress/e2e/"
fi

OUTPUTARGS=" --format junit --output $SCRIPTDIR/results/report.xml "
if [ "$USECONSOLEOUTPUT" == "1" ]; then
  OUTPUTARGS=""
fi

cd $SCRIPTDIR
echo $SPECARG
npx cypress run cypress run --reporter junit \
--reporter-options "mochaFile=results/my-test-output.xml,toConsole=false" \
--spec $SPECARG

$SCRIPTDIR/../../testserver-controller/stop.sh


