#!/bin/bash

#
# There are various functions where we need to do an end-to-end test that involve users interacting
# with each other. We can install the app twice by building another copy with a different ID.
#

SCRIPTDIR=$(realpath $(dirname $0))
WORKDIR=$(pwd)

mkdir -p $SCRIPTDIR/build/apks
cp $SCRIPTDIR/../../app-android-launcher/build/outputs/apk/release/app-android-launcher-release.apk \
   $SCRIPTDIR/build/apks/app-android-launcher-release.apk
cd $SCRIPTDIR/../..
./gradlew -PapplicationIdSuffix=2 app-android-launcher:assembleRelease
cp $SCRIPTDIR/../../app-android-launcher/build/outputs/apk/release/app-android-launcher-release.apk \
      $SCRIPTDIR/build/apks/app-android-launcher-release-2.apk

cd $WORKDIR

