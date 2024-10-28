#!/bin/bash

java -classpath build/libs/ustad-server-all.jar \
    com.ustadmobile.lib.rest.clitools.appconfig.AppConfigCLIKt "$@"
