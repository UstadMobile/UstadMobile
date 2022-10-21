#!/bin/bash

echo "Run admin can add content test"

INDEX=0

  for FILENAME in $(ls ../Content/HideTest_Content.epub); do
  FILEBASENAME=$(basename $FILENAME)
  CONTENTNAME="ContentHideTest"$INDEX
  adb push $FILENAME /sdcard/Download/$FILEBASENAME

   ../../../../runserver.sh --password testpass  --clear --background --nobuild
  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=admin -e PASSWORD=testpass -e TESTFILENAME=HideTest_Content.epub -e TESTCONTENTNAME=$CONTENTNAME admin_can_hide_content.yaml
   ../../../../runserver.sh --stop
    TESTRESULT=$?
    if [ "$TESTRESULT" != "0" ]; then
       echo "fail" > results/result
    elif [ ! -f results/result ]; then
       echo "pass" > results/result
    fi

 INDEX=$((INDEX+1))
done