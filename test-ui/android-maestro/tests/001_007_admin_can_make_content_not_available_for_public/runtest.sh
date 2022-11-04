#!/bin/bash

echo "Run admin can add make content not accessible for public test"
echo "admin username=$TESTUSER"

FILENAME="../Content/Epub_Content.epub"
FILEBASENAME=$(basename $FILENAME)
CONTENTNAME="ContentNotAvailableForPublicTest"
adb push $FILENAME /sdcard/Download/$FILEBASENAME

   ../../../../runserver.sh --password testpass  --clear --background --nobuild
    ../../start-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4

  maestro $MAESTRO_BASE_OPTS \
    -e TESTFILENAME=Epub_Content.epub -e TESTCONTENTNAME=$CONTENTNAME \
    admin_can_make_content_not_available_for_public.yaml
 TESTRESULT=$?
 if [ "$TESTRESULT" == "0" ]; then
    echo "pass" > results/result
 else
    echo "fail" > results/result
 fi
  ../../stop-screenrecord.sh $TESTSERIAL $CONTENTNAME.mp4 results/$CONTENTNAME.mp4
 ../../../../runserver.sh --stop

