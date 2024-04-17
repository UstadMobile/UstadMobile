package com.ustadmobile.core.domain.compress

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.core.util.stringvalues.asIStringValues

/**
 * Used to add a header (Content-Length-Original) to an item that was compressed when it is being
 * added to the cache. This will allow the downloaders / uploaders to track how much bandwidth
 * is saved.
 */
fun CompressResult?.originalSizeHeaders(): IStringValues {
    return if(this != null) {
        mapOf(
            "Content-Length-Original" to listOf(originalSize.toString())
        ).asIStringValues()
    }else {
        IStringValues.empty()
    }
}
