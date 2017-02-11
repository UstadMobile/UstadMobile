#/bin/bash

NANOHTTPD_VERSION_TAG=nanohttpd-project-2.3.1
NANOLRS_VERSION_TAG=v0.1.43

J2OBJC_DIR=~/local/j2objc
JAVA_SRC_DIR=../core/src/main/java
IMPL_SHAREDSE_DIR=../sharedse/src/main/java
QR_SRC_DIR=lib/checkout/qrcode/qrcode-core/src/main/java

NANO_HTTPD_SRC_DIR=lib/checkout/nanohttpd/core/src/main/java
NANO_HTTPD_NANOLETS_DIR=lib/checkout/nanohttpd/nanolets/src/main/java

NANOLRS_CORE_DIR=lib/checkout/NanoLRS/nanolrs-core/src/main/java

JAVA_SRC_FILES=$(find $JAVA_SRC_DIR -iname "*.java")
IMPL_SHAREDSE_FILES=$(find $IMPL_SHAREDSE_DIR -name "*.java") 
QR_SRC_FILES=$(find $QR_SRC_DIR -iname "*.java")
NANO_HTTPD_FILES=$(find $NANO_HTTPD_SRC_DIR -name "*.java")
NANO_HTTPD_NANOLETS_FILES=$(find $NANO_HTTPD_NANOLETS_DIR -name "*.java")
NANOLRS_CORE_FILES=$(find $NANOLRS_CORE_DIR -name "*.java")

BASEDIR=$(pwd)

# Run the build config generation
cd ..
./gradlew :core:generateCoreBuildConfig
if [ "$?" != "0" ]; then
   RETVAL=$?
   echo "Error running generate core build config on $(PWD): please check Android setup and retry"
   exit $RETVAL
fi
cd app-ios


mkdir -p lib/checkout
cd lib/checkout

# Remove obsolete AppConfig if it's present (this was gitignored)
if [ -e ../core/src/main/java/com/ustadmobile/core/impl/AppConfig.java ]; then
	rm ../core/src/main/java/com/ustadmobile/core/impl/AppConfig.java
fi


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
    git checkout tags/$NANOHTTPD_VERSION_TAG
    cd ..
fi

#Checkout NanoLRS and "build" it's iOS version
if [ ! -e NanoLRS ]; then
	git clone https://github.com/UstadMobile/NanoLRS.git NanoLRS
fi
cd NanoLRS
git checkout master
git pull
git checkout tags/$NANOLRS_VERSION_TAG
cd nanolrs-ios
./generate.sh

if [ "$?" != "0" ]; then
    "Error running nanolrs build : please check and try again"
    exit 1
fi

#back to base dir
cd $BASEDIR

echo "Back to base directory " pwd

if [ -e UstadMobileIOS/Generated/ ]; then
    rm UstadMobileIOS/Generated/*.h  UstadMobileIOS/Generated/*.m
fi

SOURCEPATH_MAIN=$JAVA_SRC_DIR:$QR_SRC_DIR:$IMPL_SHAREDSE_DIR:$NANO_HTTPD_SRC_DIR:$NANO_HTTPD_NANOLETS_DIR:$NANOLRS_CORE_DIR
SOURCEPATH_TEST=$NANOLRS_CORE_TEST_DIR

$J2OBJC_DIR/j2objc -d UstadMobileIOS/Generated/ \
   -sourcepath $SOURCEPATH_MAIN \
   --no-package-directories $JAVA_SRC_FILES \
   $IMPL_SHAREDSE_FILES $QR_SRC_FILES $NANO_HTTPD_FILES \
   $NANO_HTTPD_NANOLETS_FILES   

#Copy resources (e.g. locale)
if [ -e UstadMobileIOS/res ]; then
    rm -r UstadMobileIOS/res/*
else 
	mkdir UstadMobileIOS/res
fi

cp -rv ../core/src/main/assets/* UstadMobileIOS/res


