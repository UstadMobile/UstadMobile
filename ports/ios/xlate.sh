#!/bin/bash

if [ -e src-core ]; then
    rm -rf src-core
fi

cp -r ../../core/src ./src-core

JAVA_SRC=$(find ./src-core -iname "*.java")

for file in $JAVA_SRC; do
    j2objc -sourcepath ./src-core $file
done

