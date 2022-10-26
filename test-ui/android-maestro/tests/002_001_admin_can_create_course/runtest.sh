#!/bin/bash

echo "Run admin can add content test"
echo "ENDPOINT=$ENDPOINT"
echo "Username=$USERNAME"
echo "PASSWORD=$PASSWORD"
#INDEX=0

 #for FILENAME in $(ls ../Content/*); do
  #FILEBASENAME=$(basename $FILENAME)
  #adb push $FILENAME /sdcard/Download/$FILEBASENAME
  #CONTENTNAME="ContentUploadDownloadTest"$INDEX
 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 ../../start-screenrecord.sh emulator-5554 $COURSENAME.mp4
  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=admin -e PASSWORD=testpass -e COURSENAME=$COURSENAME admin_can_create_course.yaml
 ../../stop-screenrecord.sh emulator-5554 $COURSENAME.mp4 results/$COURSENAME.mp4
 ../../../../runserver.sh --stop
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi

  #INDEX=$((INDEX+1))
 #done

