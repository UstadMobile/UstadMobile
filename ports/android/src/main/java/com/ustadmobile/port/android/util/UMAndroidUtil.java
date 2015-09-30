package com.ustadmobile.port.android.util;

import android.os.Build;
import android.view.View;

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

}
