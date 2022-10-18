#!/bin/bash

echo "Run admin can add content test"

INDEX=0

 for FILENAME in $(ls ../Content/*); do
  FILEBASENAME=$(basename $FILENAME)
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
  CONTENTNAME="TESTCONTENT"$INDEX
  maestro --platform android test -e USERNAME=$USERNAME -e PASSWORD=$PASSWORD -e TESTFILENAME=$FILEBASENAME -e TESTCONTENTNAME=$CONTENTNAME admin_can_add_content.yaml
  INDEX=$((INDEX+1))
 done

