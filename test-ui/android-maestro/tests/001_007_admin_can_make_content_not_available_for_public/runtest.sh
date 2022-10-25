#!/bin/bash

echo "Run admin can add content test"
echo "admin username=$USERNAME"
INDEX=0
for FILENAME in $(ls ../Content/Epub_Content.epub); do
  FILEBASENAME=$(basename $FILENAME)
  CONTENTNAME="ContentNotAvailableForPublicTest"$INDEX
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
   ../../../../runserver.sh --password testpass  --clear --background --nobuild
    ../../start-screenrecord.sh emulator-5554 $CONTENTNAME.mp4
  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=admin -e PASSWORD=testpass -e TESTFILENAME=Epub_Content.epub -e TESTCONTENTNAME=$CONTENTNAME admin_can_make_content_not_available_for_public.yaml
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