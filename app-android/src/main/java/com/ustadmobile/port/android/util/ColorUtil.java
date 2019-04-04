package com.ustadmobile.port.android.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

public class ColorUtil {

    /**
     * Get color from ContextCompat
     *
     * @param color The color code
     * @return  the color
     */
    public static int getContextCompatColorFromColor(int color, Context context){
        return ContextCompat.getColor(context, color);
    }
}
