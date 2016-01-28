#!/bin/bash

###Assumes you have python for android installed###

echo "Hey there"


#Steps to do this thing.
#1. Make temp LRS working directory
#2. Pull the LRS code in LRSTEMP/LRSCode
#3. Check if you have python for android, buildozer, etc
#4. Build the LRS code with buildozer in LRSTEMP/LRSAndroid
#5. Make another direcory LRSTEMP/LRSGradle 
#6. Make an empty gradle project in there.
#7. Copy assets from LRSTEMP/LRSAndroid to LRSTEMP/LRSGradle
#8. Congrats you have the gradle file. Copy the required folder in ANDROID_PROJECT_DIR
#9. Update Android's build.gradle and AndroidManifest.xml files 
#10. Hope


ANDROID_PROJECT_DIR=`pwd`
ANDROID_PROJECT_DIR=`echo ${ANDROID_PROJECT_DIR}`

cd ../../core/
echo "Getting core libs.."
ant getlibs
ant -f build.xml getlibs
ant -f antenna-build.xml getlibs
cd ${ANDROID_PROJECT_DIR}
echo "Updating core libs.."
./updatecore

echo "Working from Android Project Dir: ${ANDROID_PROJECT_DIR}"

echo "Making LRS temp directory"
mkdir "LRSTEMP"
#mkdir "LRSTEMP/LRSCode"
#mkdir "LRSTEMP/LRSAndroid"

#May not need this
#mkdir "LRSTEMP/LRSGradle"

cd "LRSTEMP"
#Working directory is this.

git clone https://github.com/UstadMobile/ADL_LRS LRSCode
git clone https://github.com/varunasingh/LRS_P4A_Frame.git LRSAndroid

cp -r LRSCode/adl_lrs LRSAndroid/service/
cp -r LRSCode/oauth2_provider LRSAndroid/service/
cp -r LRSCode/lrs LRSAndroid/service/
cp -r LRSCode/oauth_provider LRSAndroid/service/
cp -r LRSCode/manage.py LRSAndroid/service/

cp -r LRSCode/umapi LRSAndroid/service/

#cp -r LRSCode/* LRSAndroid/service/
cp LRSAndroid/service/adl_lrs/urls.py LRSAndroid/service/

LRS_ANDROIDMANIFEST="LRSAndroid/asset/AndroidManifest.xml"
SETTINGS_FILE="LRSAndroid/service/adl_lrs/settings.py"
cp ${SETTINGS_FILE} ${SETTINGS_FILE}.bak
>${SETTINGS_FILE}.tmp

cat ${SETTINGS_FILE} | sed ':again;$!N;$!b again; s/'"'"'default'"'"'[^}]*}//' | sed ':again;$!N;$!b again; s/DATABASES[^}]*}//g' >>${SETTINGS_FILE}.tmp
cp ${SETTINGS_FILE}.tmp ${SETTINGS_FILE}

echo "" >> ${SETTINGS_FILE}
echo "BASE_DIR = path.dirname(path.dirname(__file__))" >> ${SETTINGS_FILE}

echo "" >> ${SETTINGS_FILE}
echo "DATABASES = {" >> ${SETTINGS_FILE}
echo "    'default': {" >> ${SETTINGS_FILE}
echo "        'ENGINE': 'django.db.backends.sqlite3'," >> ${SETTINGS_FILE}
echo "        'NAME': path.join(BASE_DIR, 'lrs_db.sqlite3')," >> ${SETTINGS_FILE}
echo "        'ATOMIC_REQUESTS': True" >> ${SETTINGS_FILE}
echo "    }" >> ${SETTINGS_FILE}
echo "}" >> ${SETTINGS_FILE}

#Add umapi Application to adl_lrs settings.py
#Within INSTALLED_APPS : 'umapi'
sed -i.bak -e '/^INSTALLED_APPS/,/^}/{/^)/i\    '"'"'umapi'"'"'', -e '}' ${SETTINGS_FILE}

#Add umapi urls to adl_lrs urls.py
#url(r'^umapi/', include('umapi.urls.')),
URLS_FILE="LRSAndroid/service/adl_lrs/urls.py"
sed -i.bak -e '/^urlpatterns/,/^}/{/^)/i\    url(r'"'"'^umapi/'"'"', include('"'"'umapi.urls'"'"'))', -e '}' ${URLS_FILE}
sed -i.bak "" ${URLS_FILE}



cd LRSAndroid
mkdir logs
cd logs
>celery_tasks.log
>django_request.log
>lrs.log
cd ../service/
python manage.py syncdb --noinput

cd ..
buildozer -v android debug

#.....wait for it.....
#This will take some time..

cp -r httplib2 .buildozer/applibs/
cp -r httplib2 .buildozer/android/app/_applibs/

buildozer -v android debug

#To run:
#buildozer android deploy run

cd ../

echo "Time to create a blank Gradle project."

#May not need this
#cd LRSGradle

ANDROID_PATH=`which android`

if [ "$ANDROID_PATH" == "" ];then
   echo "Android path not found. set it. Exiting.."
   exit 1
fi


#Obviously make sure you have android-19 or substitute with whatever
android create project --path ./LRSGradle --activity LRSGradleActivity --package com.ustadmobile --target "android-14" --gradle --gradle-version '1.3.0'

cd LRSGradle/

#Chage to: distributionUrl=http\://services.gradle.org/distributions/gradle-2.2.1-all.zip
sed -e '/distributionUrl/ s/^#*/#/' -i.bak gradle/wrapper/gradle-wrapper.properties
echo "distributionUrl=http\://services.gradle.org/distributions/gradle-2.2.1-all.zip" >> gradle/wrapper/gradle-wrapper.properties


#change to this:
#Replace mavenCentral() with jcenter()
#release {
#            minifyEnabled false
#	     //proguardFile getDefaultProguardFile('proguard-android.txt')
#	}
#Change apply plugin from android to com.android.library
#apply plugin 'com.android.library'

#Add minSDK and targetSdk values
#defaultConfig {
#        minSdkVersion 14
#        targetSdkVersion 14
#    }

echo "apply plugin: 'com.android.library'" > build.gradle

echo "android {" >> build.gradle
echo "    compileSdkVersion 14" >> build.gradle
echo "    buildToolsVersion '23.0.2'" >> build.gradle

echo "    defaultConfig {" >> build.gradle
echo "        minSdkVersion 9" >> build.gradle
echo "        targetSdkVersion 19" >> build.gradle
echo "    }" >> build.gradle

echo "    buildTypes {" >> build.gradle
echo "        release {" >> build.gradle
echo "            minifyEnabled false" >> build.gradle
echo "        }" >> build.gradle
echo "    }" >> build.gradle
echo "}" >> build.gradle

#sed -i.bak "" build.gradle #Replace
#sed -i.bak "" build.gradle #Change release
#sed -i.bak "" build.gradle #Change apply plugin
#sed -i.bak "" build.gradle #defaultConfig

cd ../
echo "Copying sources and assets.."

DEST_SRC="LRSGradle/src/main"
DEST_JAVA_SRC="LRSGradle/src/main/java"
SOURCE_SRC="LRSAndroid/.buildozer/android/platform/python-for-android/dist/djangolrs/src"

ASSET_SOURCE="LRSAndroid/.buildozer/android/platform/python-for-android/dist/djangolrs/assets/"
LIBS_SOURCE="LRSAndroid/.buildozer/android/platform/python-for-android/dist/djangolrs/libs"
RES_SOURCE="LRSAndroid/.buildozer/android/platform/python-for-android/dist/djangolrs/res"

cp -r ${SOURCE_SRC}/org ${DEST_JAVA_SRC}
rm -rf ${DEST_JAVA_SRC}/com #Dont need it - Created from cli

cp -r ${ASSET_SOURCE} ${DEST_SRC}/
cp -r ${LIBS_SOURCE} ${DEST_SRC}/jniLibs
cp -r ${DEST_SRC}/jniLibs/armeabi ${DEST_SRC}/jniLibs/armeabi-v7a
rm -rf ${DEST_SRC}/res/
cp -r ${RES_SOURCE} ${DEST_SRC}/
mkdir ${DEST_SRC}/aidl
cp -r ${SOURCE_SRC}/com ${DEST_SRC}/aidl/
rm -rf ${DEST_SRC}../androidTest/ #Dont need it



#If set up as application, run as: 
#./gradlew assembleDebug

#Set up UstadMobile

cd ${ANDROID_PROJECT_DIR}/

#Add settings.gradle and include

echo "include ':lrs'" > settings.gradle

#Change build.gradle and add module lrs
#compile project(':lrs')
#sed -i.bak -e '/^dependencies/,/^}/{/^}/i\    compile project('"'"'\:lrs'"'"')' -e '}' build.gradle

cd ${ANDROID_PROJECT_DIR}
cp -r "LRSTEMP/LRSGradle" lrs/

#comment out the intent at lrs's AndroidManifest.xml and many more things
#sed -i.bak "" lrs/src/main/AndroidManifest.xml #Comment intent
#Actually just copy it from somewhere.
cp LRSTEMP/${LRS_ANDROIDMANIFEST} lrs/src/main/AndroidManifest.xml

#build itt
./gradlew assembleDebug

#This is what lrs's build.gradle should look like: 
#apply plugin: 'com.android.library'

#android {
#    compileSdkVersion 14
#    buildToolsVersion '23.0.2'

#    defaultConfig {
#        minSdkVersion 9
#        targetSdkVersion 19
#    }

#    buildTypes {
#        release {
#            minifyEnabled false
#        }
#    }
#}





