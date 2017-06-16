#!/bin/bash

for i in {1..30}; do
    ./gradlew shared:test
    RESULT=$?
    if [ "$?" != "0" ]; then
       echo "Test $i FAILED"
       exit 1
    fi
done

echo "All tests succeeded"
