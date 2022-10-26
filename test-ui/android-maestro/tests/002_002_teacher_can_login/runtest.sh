#!/bin/bash

echo "Run admin can create course test"

 ../../../../runserver.sh --password testpass  --clear --background --nobuild
 ../../start-screenrecord.sh $SERIAL $COURSENAME.mp4
  maestro --platform android test \
 -e SERIAL=$SERIAL -e ENDPOINT=$ENDPOINT -e USERNAME=$USERNAME -e PASSWORD=$PASSWORD \
  -e COURSENAME=$COURSENAME admin_can_add_new_student.yaml
 ../../stop-screenrecord.sh $SERIAL $COURSENAME.mp4 results/$COURSENAME.mp4
 ../../../../runserver.sh --stop
  TESTRESULT=$?
  if [ "$TESTRESULT" != "0" ]; then
     echo "fail" > results/result
  elif [ ! -f results/result ]; then
     echo "pass" > results/result
  fi



