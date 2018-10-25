package com.ustadmobile.port.android.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mike on 9/21/15.
 */
public class UMAndroidUtil {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Set the direction of a given view if we are running on a version of Android that supports
     * this : support for directionality in views was added in Android 4.2
     *
     * @param view
     * @param direction
     */
    public static void setDirectionIfSupported(View view, int direction) {
        if(Build.VERSION.SDK_INT >= 17 ) {
            view.setLayoutDirection(direction);
        }
    }


    /**
     * Convert an Android bundle to a hashtable
     * @param bundle
     * @return
     */
    public static Hashtable bundleToHashtable(Bundle bundle) {
        if(bundle == null)
            return null;

        Hashtable retVal = new Hashtable();
        Set<String> keys = bundle.keySet();
        Iterator<String> iterator = keys.iterator();

        String key;
        Object val;
        while(iterator.hasNext()) {
            key = iterator.next();
            val = bundle.get(key);
            //TODO: could this not simply be putAll?
            if(val instanceof String) {
                retVal.put(key, val);
            }else if(val instanceof Integer) {
                retVal.put(key, val);
            }else if(val instanceof String[]) {
                retVal.put(key, val);
            }else if(val instanceof  Long){
                retVal.put(key, val);
            }
        }

        return retVal;
    }

    public static Bundle hashtableToBundle(Hashtable table) {
        if(table == null)
            return null;

        Bundle bundle = new Bundle();

        Iterator iterator = table.keySet().iterator();
        String key;
        Object val;
        while(iterator.hasNext()) {
            key = (String)iterator.next();
            val = table.get(key);
            if(val instanceof Integer) {
                bundle.putInt(key, (Integer)val);
            }else if(val instanceof String){
                bundle.putString(key, (String)val);
            }else if(val instanceof String[]) {
                bundle.putStringArray(key, (String[])val);
            }else if(val instanceof Long){
                bundle.putLong(key, (Long) val);
            }
        }
        return bundle;

    }



    @SuppressLint("NewApi")
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }

    }


}
