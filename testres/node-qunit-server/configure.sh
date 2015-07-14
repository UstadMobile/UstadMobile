#!/bin/bash
#
# Install node modules needed for Node Qunit test server to run
#

npm install git+https://github.com/UstadMobile/node-slow-stream.git \
    buffered-stream http-server stream-buffers MD5
