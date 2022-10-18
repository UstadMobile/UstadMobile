#!/bin/bash

echo "Run admin can add content test"
echo "admin username=$USERNAME"
INDEX=0
for FILENAME in $(ls ../Content/*); do
  FILEBASENAME=$(basename $FILENAME)
  CONTENTNAME="CONTENTMOVETEST"$INDEX
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
  maestro --platform android test -e USERNAME=$USERNAME -e PASSWORD=$PASSWORD -e TESTFILENAME=$FILEBASENAME -e TESTCONTENTNAME=$CONTENTNAME admin_can_move_content.yaml
INDEX=$((INDEX+1))
done