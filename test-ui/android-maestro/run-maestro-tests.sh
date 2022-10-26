#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e:t:' --long 'serial1:,username:,password:,endpoint:,tests:' -n 'run-maestro-tests.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"

# Alias for maestro with common variables

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
               '-t'|'--tests')
                     echo "Set tests to $2"
                     TESTS=$2
                     shift 2
                     continue
               ;;
               '--')
                        shift
                        break
                ;;
        
	esac
done

BASEDIR=$(pwd)

if [ "$TESTS" == "" ]; then
  TESTS=$(ls ./tests/*/runtest.sh)
fi

if [ "$TESTSERIAL" == "" ]; then
  echo "ERROR: ADB device serial required: please specify with --serial1 "
  exit 1
fi

echo "Serial1=$TESTSERIAL"
echo "Username=$TESTUSER"

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" == "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

echo "ENDPOINT=$ENDPOINT"
MAESTRO_BASE_OPTS="--platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER -e PASSWORD=$TESTPASS "

for TESTFILE in $TESTS; do
  TESTABSPATH=$(realpath $TESTFILE)
  cd $(dirname $TESTFILE)
  source $TESTABSPATH
  cd $BASEDIR
done

