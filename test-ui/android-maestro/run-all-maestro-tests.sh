#!/bin/bash


#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p:e::' --long 'serial1:,username:,password:,endpoint::' -n 'run-all-maestro-tests.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP


while true; do
        case "$1" in
             '-s'|'--serial1')
                	SERIAL=$2
                        shift 2
                        continue
                ;;
             '-u'|'--username')
                	USERNAME=$2
                        shift 2
                        continue
                ;;   
             '-p'|'--password')
                   	PASSWORD=$2
                        shift 2
                       continue
                ;;
              '-e'|'--endpoint')
                    ENDPOINT=$2
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
TESTS=$(ls ./tests/*/runtest.sh)

if [ "$SERIAL" == "" ]; then
  echo "ERROR: ADB device serial required: please specify with --serial1 "
  exit 1
fi

echo "Serial1=$SERIAL"
echo "Username=$USERNAME"

IPADDR=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1)
if [ "$ENDPOINT" == "" ]; then
    ENDPOINT="http://$IPADDR:8087/"
fi

echo "ENDPOINT=$ENDPOINT"
for TESTFILE in $TESTS; do
  TESTABSPATH=$(realpath $TESTFILE)
  cd $(dirname $TESTFILE)
  source $TESTABSPATH
  cd $BASEDIR
done


