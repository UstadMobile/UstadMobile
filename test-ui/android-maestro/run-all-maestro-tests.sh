#!/bin/bash


#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 's:u:p::' --long 'serial1:,username:,password::' -n 'run-all-maestro-tests.sh' -- "$@")

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
             '--')
                        shift
                        break
                ;;
        
	esac
done

TESTS=$(ls ./tests/*/runtest.sh)

echo "Serial1=$SERIAL"
echo "Username=$USERNAME"

for TESTFILE in $TESTS; do
    source $TESTFILE
done

