package com.ustadmobile.core.io.ext
import java.io.File
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import java.io.IOException

/**
 * Gets a standard subdirectory to use for data that is specific to a given endpoint
 */
fun File.siteDataSubDir(siteEndpoint: Endpoint): File {
    return File(File(this, UstadMobileSystemCommon.SUBDIR_SITEDATA_NAME),
            sanitizeDbNameFromUrl(siteEndpoint.url))
}

fun File.isParentOf(parent: File): Boolean {
    val parentNormalized = parent.normalize()
    var parentToCheck = this.parentFile
    do{
        if(parentToCheck?.normalize() == parentNormalized)
            return true

        parentToCheck = parentToCheck?.parentFile

    }while (parentToCheck != null)

    return false
}

fun makeTempDir(prefix: String, postfix: String = ""): File {
    val tmpDir = File.createTempFile(prefix, postfix)
    return if (tmpDir.delete() && tmpDir.mkdirs())
        tmpDir
    else
        throw IOException("Could not delete / create tmp dir")
}