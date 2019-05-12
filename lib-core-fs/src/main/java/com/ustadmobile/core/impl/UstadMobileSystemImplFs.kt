package com.ustadmobile.core.impl

import java.io.IOException
import java.io.InputStream

/**
 * Created by mike on 12/27/17.
 */

interface UstadMobileSystemImplFs {

    fun getHttpCache(context: Any): HttpCache

    /**
     * Get an asset Synchronously
     * @param context
     * @param path
     * @return
     */
    @Throws(IOException::class)
    fun getAssetSync(context: Any, path: String): InputStream

}
