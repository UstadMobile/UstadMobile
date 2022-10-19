#!/bin/bash

echo "Run admin can add content test"
echo "ENDPOINT=$ENDPOINT"
echo "Username=$USERNAME"
echo "PASSWORD=$PASSWORD"
INDEX=0
 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 for FILENAME in $(ls ../Content/*); do
  FILEBASENAME=$(basename $FILENAME)
  adb push $FILENAME /sdcard/Download/$FILEBASENAME
  CONTENTNAME="TESTCONTENT"$INDEX

  maestro --platform android test -e ENDPOINT=$ENDPOINT -e USERNAME=$USERNAME -e PASSWORD=$PASSWORD -e TESTFILENAME=$FILEBASENAME -e TESTCONTENTNAME=$CONTENTNAME admin_can_add_content.yaml

  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi

  INDEX=$((INDEX+1))
 done
  ../../../../runserver.sh --stop
