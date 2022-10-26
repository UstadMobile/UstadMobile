#!/bin/bash

echo "Run admin can create course test"

 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 ../../start-screenrecord.sh $TESTSERIAL $COURSENAME.mp4
  maestro --platform android test \
 -e SERIAL=$TESTSERIAL -e ENDPOINT=$ENDPOINT -e USERNAME=$TESTUSER -e PASSWORD=$TESTPASS \
  -e COURSENAME=$COURSENAME admin_can_create_course.yaml
 ../../stop-screenrecord.sh $TESTSERIAL $COURSENAME.mp4 results/$COURSENAME.mp4
 ../../../../runserver.sh --stop
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi



