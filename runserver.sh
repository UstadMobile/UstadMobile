#!/bin/bash

# Run the server from source

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'u:p:hdcbsnj' --long 'siteUrl:,password:,help,debug,clear,background,stop,nobuild,bundlejs,' -n 'runserver.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

WORKDIR=$(pwd)
BASEDIR="$(realpath $(dirname $0))"
BACKGROUND="false"
STOP="false"
NOBUILD="false"
CONFIGARG=""

#The root path of the project (e.g. the directory into which it was checked out from git)
cd $BASEDIR


SERVERARGS=" -DLOG_DIR=$BASEDIR/build "
while true; do
  case "$1" in
    '-h'|'--help')
      echo "runserver.sh [OPTIONS]"
      echo "Run UstadMobile HTTP server"
      echo " -d --debug Enable JVM debugging"
      echo " -n --nobuild skip gradle build"
      echo " -u --siteUrl set the site url e.g. http://my.ip.address:8087/"
      exit 0
      ;;
    '-d'|'--debug')
      DEBUGARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
      shift 1
      continue
      ;;
    '-n'|'--nobuild')
      NOBUILD="true"
      shift 1
      continue
      ;;
    '-u'|'--siteUrl')
      SERVERARGS="$SERVERARGS -P:ktor.ustad.siteUrl=$2"
      shift 2
      continue
      ;;
    '--')
      shift
      break
      ;;
  esac
done


if [ "$NOBUILD" != "true" ] && [ "$STOP" != "true" ]; then
  echo "Building server from source"
  if [ "$?" != "0" ]; then
    echo "Error preparing locale"
    exit 2
  fi

  ./gradlew $SERVER_BUILD_ARGS app-ktor-server:buildFatJar
  if [ "$?" != "0" ]; then
    echo "Error compiling server"
    exit 2
  fi
fi

if [ ! -e $BASEDIR/app-ktor-server/build/libs/ustad-server-all.jar ]; then
  echo "Please build the server jar: ./gradlew app-ktor-server:buildFatJar"
  exit 1
fi

cd $BASEDIR/app-ktor-server

if [ -e "ustad-server.conf" ]; then
  SERVERARGS="$SERVERARGS -config=ustad-server.conf "
fi

#check the server is not already running
nc -z 127.0.0.1 8087
NCRESULT=$?
if [ "$NCRESULT" == "0" ]; then
  echo "Something is already running on port 8087! Please stop it before trying this again!"
  exit 1
fi

# As per the unixStartScript.txt template to accumalate args that will then be passed on
eval "set -- \$(
        printf '%s\\n' "\$DEFAULT_JVM_OPTS \$JAVA_OPTS \$${optsEnvironmentVar}" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\\\&~g; ' |
        tr '\\n' ' '
    )" '"\$@"'


java $DEBUGARGS -jar build/libs/ustad-server-all.jar -P:ktor.ustad.jsDevServer=http://localhost:8080/ "\$@" $SERVERARGS

# Go back to wherever we started from
cd $WORKDIR
