#!/bin/bash

#Password reset via command line interface
java -classpath build/libs/ustad-server-all.jar \
    com.ustadmobile.lib.rest.clitools.passwordreset.PasswordResetKt  "$@"
