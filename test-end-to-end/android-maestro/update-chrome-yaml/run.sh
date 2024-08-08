#!/bin/bash

RETCODE=1

while [ "$RETCODE" != "0" ]; do
  maestro test chrome.yaml
  RETCODE=$?

  if [ "$RETCODE" != "0" ]; then
    sleep 15
  fi
done


