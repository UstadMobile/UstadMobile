#!/bin/bash

echo "Run admin can add content test"
echo "ENDPOINT=$ENDPOINT"
echo "Username=$USERNAME"
echo "PASSWORD=$PASSWORD"
INDEX=0

 for FILENAME in $(ls ../Content/*); do
  FILEBASENAME=$(basename $FILENAME)
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
  CONTENTNAME="ContentUploadDownloadTest"$INDEX
 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 ../../start-screenrecord.sh emulator-5554 $CONTENTNAME.mp4
  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=admin -e PASSWORD=testpass -e TESTFILENAME=$FILEBASENAME -e TESTCONTENTNAME=$CONTENTNAME admin_can_add_content.yaml
 ../../stop-screenrecord.sh emulator-5554 $CONTENTNAME.mp4 results/$CONTENTNAME.mp4
 ../../../../runserver.sh --stop
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi

  INDEX=$((INDEX+1))
 done

