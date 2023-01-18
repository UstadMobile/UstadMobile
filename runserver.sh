#!/bin/bash

#Parse command line arguments as per
# /usr/share/doc/util-linux/examples/getopt-example.bash
TEMP=$(getopt -o 'p:hdcbsnj' --long 'password:,help,debug,clear,background,stop,nobuild,bundlejs' -n 'runserver.sh' -- "$@")

eval set -- "$TEMP"
unset TEMP

WORKDIR=$(pwd)
BASEDIR="$(realpath $(dirname $0))"
BACKGROUND="false"
STOP="false"
NOBUILD="false"
BUNDLEJS="false"

#The root path of the project (e.g. the directory into which it was checked out from git)
cd $BASEDIR


SERVERARGS=" -DLOG_DIR=$BASEDIR/build "
while true; do
  case "$1" in
    '-h'|'--help')
      echo "runserver.sh [OPTIONS]"
      echo "Run UstadMobile HTTP server"
      echo " -d --debug Enable JVM debugging"
      echo " -c --clear clear ALL server data (database and data directory)"
      echo " -b --background run server in background"
      echo " -s --stop stop server that was started in the background"
      echo " -n --nobuild skip gradle build"
      echo " -p --password set the admin password to be generated (if not already set or db is cleared)"
      echo " -j --bundlejs if building the server, build the production javascript bundle. Required to show the webui using the http server. Not required for Javascript development or Android"
      exit 0
      ;;
    '-d'|'--debug')
      DEBUGARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
      shift 1
      continue
      ;;
    '-c'|'--clear')
      echo "Clearing ALL DATA as requested: Delete $BASEDIR/app-ktor-server/data"
      SERVERARGS="$SERVERARGS -P:ktor.database.cleardb=true"
      rm -rf $BASEDIR/app-ktor-server/data
      shift 1
      continue
      ;;
    '-b'|'--background')
      BACKGROUND="true"
      shift 1
      continue
      ;;
    '-s'|'--stop')
      STOP="true"
      shift 1
      continue
      ;;
    '-n'|'--nobuild')
      NOBUILD="true"
      shift 1
      continue
      ;;
    '-p'|'--password')
      SERVERARGS="$SERVERARGS -P:ktor.ustad.adminpass=$2"
      shift 2
      continue
      ;;
    '-j'|'--bundlejs')
      BUNDLEJS="true"
      shift 1
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
  ./gradlew core:prepareLocale
  if [ "$?" != "0" ]; then
    echo "Error preparing locale"
    exit 2
  fi
  SERVER_BUILD_ARGS=""
  if [ "$BUNDLEJS" == "true" ]; then
    SERVER_BUILD_ARGS=" -Pktorbundleproductionjs=true "
  fi

  ./gradlew $SERVER_BUILD_ARGS app-ktor-server:shadowJar
  if [ "$?" != "0" ]; then
    echo "Error compiling server"
    exit 2
  fi
fi

if [ ! -e $BASEDIR/app-ktor-server/build/libs/ustad-server-all.jar ]; then
  echo "Please build the server jar: ./gradlew app-ktor-server:shadowJar"
  exit 1
fi

if [ ! -e $BASEDIR/app-ktor-server/ustad-server.conf ]; then
  cp $BASEDIR/app-ktor-server/src/main/resources/application.conf $BASEDIR/app-ktor-server/ustad-server.conf
fi

cd $BASEDIR/app-ktor-server

if [ "$STOP" == "true" ];then
  if [ -e build/server.pid ]; then
    PID=$(cat build/server.pid)
    rm build/server.pid
    kill $PID
    echo "Stopped server process $PID"
    exit 0
  else
    echo "Cannot stop server: pid file build/server.pid does not exist!"
    exit 1
  fi
fi


#check the server is not already running
nc -z 127.0.0.1 8087
NCRESULT=$?
if [ "$NCRESULT" == "0" ]; then
  echo "Something is already running on port 8087! Please stop it before trying this again!"
  exit 1
fi

if [ "$BACKGROUND" == "true" ]; then
  if [ -e build/server.pid ]; then
    echo "Server already running as process id #$(cat build/server.pid). If this is incorrect, delete app-ktor-server/build/server.pid"
    exit 1
  fi

  echo "SERVERARGS =$SERVERARGS"
  java $DEBUGARGS -jar build/libs/ustad-server-all.jar -config=ustad-server.conf $SERVERARGS &
  PID=$!
  echo $PID > build/server.pid
  echo "Server started as PID $PID Logging to app-ktor-server/log/ustad-server.log"
else
  echo "Server starting: logging to app-ktor-server/log/ustad-server.log"
  java $DEBUGARGS -jar build/libs/ustad-server-all.jar -config=ustad-server.conf $SERVERARGS
fi

# Go back to wherever we started from
cd $WORKDIR
