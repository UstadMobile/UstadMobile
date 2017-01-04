#/bin/bash

J2OBJC_DIR=~/local/j2objc
JAVA_SRC_DIR=../../core/src/main/java
IMPL_SHAREDSE_DIR=../sharedse/src/main/java
QR_SRC_DIR=lib/checkout/qrcode/src
NANO_HTTPD_SRC_DIR=lib/checkout/nanohttpd/core/src/main/java
NANO_HTTPD_NANOLETS_DIR=lib/checkout/nanohttpd/nanolets/src/main/java
NANOLRS_CORE_DIR=lib/checkout/NanoLRS/nanolrs-core/src/main/java

JAVA_SRC_FILES=$(find $JAVA_SRC_DIR -iname "*.java")
IMPL_SHAREDSE_FILES=$(find $IMPL_SHAREDSE_DIR -name "*.java") 
QR_SRC_FILES=$(find $QR_SRC_DIR -iname "*.java")
NANO_HTTPD_FILES=$(find $NANO_HTTPD_SRC_DIR -name "*.java")
NANO_HTTPD_NANOLETS_FILES=$(find $NANO_HTTPD_NANOLETS_DIR -name "*.java")
NANOLRS_CORE_FILES=$(find $NANOLRS_CORE_DIR -name "*.java")

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

#checkout NanoHTTPD
if [ ! -e nanohttpd ]; then
    git clone https://github.com/NanoHttpd/nanohttpd.git
    cd nanohttpd
    git checkout tags/nanohttpd-project-2.3.1
    cd ..
fi



#Checkout NanoLRS
if [ -e NanoLRS ]; then
	cd NanoLRS
	git pull
	cd ..
else
	git clone https://github.com/UstadMobile/NanoLRS.git NanoLRS
fi

cd ../..


pwd

$J2OBJC_DIR/j2objc -d UstadMobileIOS/Generated/ \
   -sourcepath $JAVA_SRC_DIR:$QR_SRC_DIR:$IMPL_SHAREDSE_DIR:$NANO_HTTPD_SRC_DIR:$NANO_HTTPD_NANOLETS_DIR \
   --no-package-directories $JAVA_SRC_FILES \
   $IMPL_SHAREDSE_FILES $QR_SRC_FILES $NANO_HTTPD_FILES \
   $NANO_HTTPD_NANOLETS_FILES $NANOLRS_CORE_FILES

#Copy resources (e.g. locale)
if [ -e UstadMobileIOS/res ]; then
    rm -r UstadMobileIOS/res/*
else 
	mkdir UstadMobileIOS/res
fi

cp -rv ../../core/src/main/assets/* UstadMobileIOS/res


