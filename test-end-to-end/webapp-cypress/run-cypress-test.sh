#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'u:p:e:r:' --long 'username:,password:,endpoint:,spec:' -n 'run-cypress-test.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

TESTUSER="admin"
TESTPASS="testpass"
WORKDIR=$(pwd)
SCRIPTDIR=$(realpath $(dirname $0))
SPEC=""

while true; do
        case "$1" in
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


cd $SCRIPTDIR
 cypress run cypress run
 --reporter-options "mochaFile=results/my-test-output.xml,toConsole=true" \


