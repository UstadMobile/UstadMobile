#!/bin/bash

# Install https://www.npmjs.com/package/generate-license-file
npx generate-license-file --input ../build/js/package.json --output src/jsMain/resources/open_source_licenses.txt.1
cat src/jsMain/resources/ustad_open_licenses.txt > src/jsMain/resources/open_source_licenses.txt
cat src/jsMain/resources/open_source_licenses.txt.1 >> src/jsMain/resources/open_source_licenses.txt
rm src/jsMain/resources/open_source_licenses.txt.1
