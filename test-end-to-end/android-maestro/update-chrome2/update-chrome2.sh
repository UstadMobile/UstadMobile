#!/bin/bash

cd ../build/chrome-apks/
./install.sh
cd ../../update-chrome2

RETCODE=1

while [ "$RETCODE" != "0" ]; do
  maestro test update-chrome2.yaml
  RETCODE=$?

  if [ "$RETCODE" != "0" ]; then
    sleep 15
  fi
done

echo "Done"
