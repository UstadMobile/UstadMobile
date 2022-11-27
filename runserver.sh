#!/bin/bash

if [ ! -e app-ktor-server/build/libs/ustad-server-all.jar ]; then
  echo "Please build the server jar: ./gradlew app-ktor-server:shadowJar"
  exit 1
fi

if [ ! -e app-ktor-server/ustad-server.conf ]; then
  cp app-ktor-server/src/main/resources/application.conf app-ktor-server/ustad-server.conf
fi

echo "Running Ustad server: config file is app-ktor-server/ustad-server.conf"
cd app-ktor-server
java -jar build/libs/ustad-server-all.jar -config=ustad-server.conf

