#!/bin/bash

#
# Script to run the prototype localizer. This will replace English text in a Pencil prototype
# with foreign language text using the translations in the Strings XML files. It will also 
# output a CSV file containing a list of all text found for which there is no tranlsation.
#

java -classpath ../app-ktor-server/build/libs/ustad-server-all.jar \
	com.ustadmobile.lib.rest.prototypestrings.PrototypeLocalizerKt $1 $2 $3 $4 $5

