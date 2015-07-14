#!/bin/bash

IP="192.168.1.1"
sed s/__TESTSERVERIP__/$IP/ ../../core/test/com/ustadmobile/test/core/TestConstants.java
