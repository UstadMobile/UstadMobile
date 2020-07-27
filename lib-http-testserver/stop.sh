#!/bin/bash

if [ ! -e build/server.pid ]; then
    echo "No build/server.pid file. Nothing to stop."
fi

PID=$(cat build/server.pid)
kill $PID
echo "Stopped server running as PID $PID"
rm build/server.pid

