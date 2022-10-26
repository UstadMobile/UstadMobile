#!/bin/bash

echo "Run admin can add content test"
echo "admin username=$USERNAME"

FILENAME="../Content/Epub_Content.epub"
FILEBASENAME=$(basename $FILENAME)
CONTENTNAME="ContentMoveTest"
adb push $FILENAME /sdcard/Download/$FILEBASENAME
 ../../../../runserver.sh --password testpass  --clear --background --nobuild
  ../../start-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4
maestro $MAESTRO_BASE_OPTS \
  -e TESTFILENAME=Epub_Content.epub -e TESTCONTENTNAME=$CONTENTNAME admin_can_move_content.yaml
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi
../../stop-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4 results/$CONTENTNAME.mp4
../../../../runserver.sh --stop

