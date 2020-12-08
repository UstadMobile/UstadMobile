package com.ustadmobile.core.util.ext

import android.content.res.Resources
import kotlin.math.roundToInt

/**
 * Convert the given float that is in dp (density pixels) to actual pixels
 */
inline val Float.dpAsPx
    get() = (Resources.getSystem().displayMetrics.density * this).roundToInt()
