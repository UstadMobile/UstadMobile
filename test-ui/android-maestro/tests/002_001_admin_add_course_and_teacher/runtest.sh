#!/bin/bash

echo "Run admin can create course test"
COURSENAME=TestCourse
../../../../runserver.sh --password testpass  --clear --background --nobuild
../../start-screenrecord.sh $TESTSERIAL $COURSENAME.mp4
maestro $MAESTRO_BASE_OPTS \
    -e COURSENAME=TestCourse admin_add_new_course_and_teacher.yaml
TESTRESULT=$?
if [ "$TESTRESULT" == "0" ]; then
   echo "pass" > results/result
else
   echo "fail" > results/result
fi
../../stop-screenrecord.sh $TESTSERIAL $COURSENAME.mp4 results/$COURSENAME.mp4
../../../../runserver.sh --stop




