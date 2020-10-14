package com.ustadmobile.core.util.ext

import android.content.res.Resources

inline val Int.pxAsDp: Float
    get() = (this.toFloat() / Resources.getSystem().displayMetrics.density)