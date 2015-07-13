#!/bin/bash
#
# Preprocess and run tests
#

ant -lib ../../core/lib/com.objfac.ant.preprocess_0.9.1/preprocessor.jar \
     -f build-preprocess-tests.xml

./gradlew connectedAndroidTest

