package com.ustadmobile.core.io.ext
import java.io.File
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl

/**
 * Gets a standard subdirectory to use for data that is specific to a given endpoint
 */
fun File.siteDataSubDir(siteEndpoint: Endpoint): File {
    return File(File(this, UstadMobileSystemCommon.SUBDIR_SITEDATA_NAME),
            sanitizeDbNameFromUrl(siteEndpoint.url))
}
