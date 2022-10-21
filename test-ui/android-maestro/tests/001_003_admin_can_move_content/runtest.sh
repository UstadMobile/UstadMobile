#!/bin/bash

echo "Run admin can add content test"
echo "admin username=$USERNAME"
INDEX=0
for FILENAME in $(ls ../Content/MoveTest_Content.h5p); do
  FILEBASENAME=$(basename $FILENAME)
  CONTENTNAME="ContentMoveTest"$INDEX
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
   ../../../../runserver.sh --password testpass  --clear --background --nobuild
  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=admin -e PASSWORD=testpass -e TESTFILENAME=MoveTest_Content.h5p -e TESTCONTENTNAME=$CONTENTNAME admin_can_move_content.yaml
 ../../../../runserver.sh --stop
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi
INDEX=$((INDEX+1))
done