package com.ustadmobile.core.util.ext

import com.ustadmobile.core.model.BitmaskFlag

/**
 * Given a list of BitmaskFlag objects, get the combined flag value (e.g. perform a binary or
 * using the flagvalue of each BitmaskFlag in the list if its enabled value is true)
 */
val List<BitmaskFlag>.combinedFlagValue: Long
    get() = fold(0) { accVal, flag ->
        if(flag.enabled) {
            (accVal or flag.flagVal)
        }else {
            accVal
        }
    }