package com.ustadmobile.sharedse.impl.http

import android.content.res.Resources
import com.ustadmobile.core.util.ext.dpAsPx
import com.ustadmobile.core.util.ext.pxAsDp

/**
 * Calculate out the factor to use to convert vh units to pixels. On Android this will be the
 * height of the screen minus 48dp for the action bar.
 */
actual fun vhToPxFactor(): Float {
    return ((Resources.getSystem().displayMetrics.heightPixels - 48F.dpAsPx).pxAsDp / 100F)
}