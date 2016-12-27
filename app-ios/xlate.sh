#/bin/bash

JAVA_SRC_DIR=../../core/src/main/java
IMPL_SHAREDSE_DIR=../sharedse/src/main/java
QR_SRC_DIR=lib/checkout/qrcode/src

JAVA_SRC_FILES=$(find $JAVA_SRC_DIR -iname "*.java")
IMPL_SHAREDSE_FILES=$(find $IMPL_SHAREDSE_DIR -name "*.java") 
QR_SRC_FILES=$(find $QR_SRC_DIR -iname "*.java")

mkdir -p lib/checkout
cd lib/checkout

#checkout the QR Code lib from Git.
if [ -e qrcode ]; then
    cd qrcode
    git pull
    cd ..
else
    git clone https://github.com/UstadMobile/Open-Source-QR-Code-Library-OMR-Fork.git qrcode
fi
#go back to base directory
cd ../..
pwd

~/j2objc/current/j2objc -d UstadMobileIOS/Generated/ \
   -sourcepath $JAVA_SRC_DIR:$QR_SRC_DIR:$IMPL_SHAREDSE_DIR \
   --no-package-directories $JAVA_SRC_FILES \
   $IMPL_SHAREDSE_FILES $QR_SRC_FILES
