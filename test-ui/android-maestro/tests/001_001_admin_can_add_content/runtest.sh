#!/bin/bash

echo "Run admin can add content test"
echo "ENDPOINT=$ENDPOINT"
echo "Username=$TESTUSER"
echo "PASSWORD=$TESTPASS"
echo "SERIAL=$TESTSERIAL"
INDEX=0
FAILCOUNT=0
for FILENAME in $(ls ../Content/*); do
  FILEBASENAME="$(basename $FILENAME)"

  adb push $FILENAME /sdcard/Download/$FILEBASENAME
  CONTENTNAME="ContentUploadDownloadTest"$INDEX
 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 ../../start-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4
  maestro $MAESTRO_BASE_OPTS \
    -e TESTFILENAME=$FILEBASENAME -e TESTCONTENTNAME=$CONTENTNAME admin_can_add_content.yaml

  TESTRESULT=$?
  if [ "$TESTRESULT" == "0" ]; then
    echo "pass" > results/result-$CONTENTNAME
  else
    echo "fail" > results/result-$CONTENTNAME
  fi

 ../../stop-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4 results/$CONTENTNAME.mp4
 ../../../../runserver.sh --stop
  INDEX=$((INDEX+1))
 done
if [ "$FAILCOUNT" == "0" ] ; then
  echo "pass" > results/result
else
  echo "fail" > results/result
fi

