#!/bin/bash

#
# Utility script that can be used to fail a build if the apiUrl in appconfig.properties is not as
# expected. Set the environment variable APIURL first. If the apiUrl in appconfig.properties does
# not match the APIURL environment variable
#

URLINPROP=$(cat core/src/main/assets/com/ustadmobile/core/appconfig.properties | grep "apiUrl=")

if [ "$URLINPROP" != "apiUrl=$APIURL" ]; then
    echo "appconfig.properties does not specify expected api Url: $APIURL (apiUrl in appconfig.properties is: $URLINPROP)"
    exit 1
fi


