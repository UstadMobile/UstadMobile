#!/bin/bash

APKPATHS=$(adb shell pm path com.android.chrome | sed 's/package\://g')
OUTDIR=../build/chrome-apks/


if [ ! -e $OUTDIR ]; then
  mkdir -p $OUTDIR
fi


APKLIST=""
for APKPATH in $APKPATHS; do
   adb pull $APKPATH $OUTDIR/$(basename $APKPATH)
   APKLIST="$APKLIST $(basename $APKPATH)"
done

echo "#!/bin/bash" > $OUTDIR/install.sh
echo "adb install-multiple $APKLIST" >> $OUTDIR/install.sh

echo Saved all chrome APKS and installer script to $OUTDIR

