package com.ustadmobile.port.android.util;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mike on 9/21/15.
 */
public class UMAndroidUtil {

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
        Hashtable retVal = new Hashtable();
        Set<String> keys = bundle.keySet();
        Iterator<String> iterator = keys.iterator();

        String key;
        Object val;
        while(iterator.hasNext()) {
            key = iterator.next();
            val = bundle.get(key);
            if(val instanceof String) {
                retVal.put(key, val);
            }
        }

        return retVal;
    }

    public static Bundle hashtableToBundle(Hashtable table) {
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
            }
        }
        return bundle;

    }

}
