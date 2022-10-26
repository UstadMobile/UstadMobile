#!/bin/bash

echo "Run admin can add content test"

FILENAME=$(ls ../Content/Epub_Content.epub)
FILEBASENAME=$(basename $FILENAME)
CONTENTNAME="ContentHideTest"
adb push $FILENAME /sdcard/Download/$FILEBASENAME

../../../../runserver.sh --password $TESTPASS  --clear --background --nobuild
../../start-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4
maestro --platform android test \
-e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER -e PASSWORD=$TESTPASS \
-e TESTFILENAME=Epub_Content.epub -e TESTCONTENTNAME=$CONTENTNAME admin_can_hide_content.yaml
../../stop-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4 results/$CONTENTNAME.mp4
../../../../runserver.sh --stop
TESTRESULT=$?
if [ "$TESTRESULT" != "0" ]; then
   echo "fail" > results/result
elif [ ! -f results/result ]; then
   echo "pass" > results/result
fi


