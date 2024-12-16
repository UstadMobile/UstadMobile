#!/bin/bash


WORKDIR=$(pwd)
BASEDIR="$(realpath $(dirname $0))"


cd $BASEDIR

java -classpath app-ktor-server/build/libs/ustad-server-all.jar \
    com.ustadmobile.lib.rest.clitools.appconfig.AppConfigCLIKt "$@"

cd $WORKDIR