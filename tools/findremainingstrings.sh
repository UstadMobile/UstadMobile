#!/bin/bash

#
# Script to run the find remaining strings tool. This will output a strings xml file for a given
# langauge with all the strings that need translated. It will include the English text and all
# comments for those strings
#

java -classpath ../app-ktor-server/build/libs/ustad-server-all.jar \
	com.ustadmobile.lib.rest.remainingstrings.RemainingStringsToolKt $1 $2
