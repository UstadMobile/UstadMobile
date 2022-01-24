package com.ustadmobile.core.contentjob

import com.ustadmobile.core.util.UmPlatformUtil

object SupportedContent {

    val EPUB_MIME_TYPES = listOf("application/epub+zip", "application/octet-stream")

    val XAPI_MIME_TYPES = listOf("application/tincan+zip", "application/zip", "application/octet-stream")

    val H5P_MIME_TYPES = listOf("application/h5p-tincan+zip","application/tincan+zip", "application/zip", "application/octet-stream")

    val SCORM_MIME_TYPES = listOf("application/scorm+zip")

    val H5P_EXTENSIONS = if(UmPlatformUtil.isWeb) listOf(".h5p") else listOf("h5p")

    val EPUB_EXTENSIONS = if(UmPlatformUtil.isWeb) listOf(".epub") else listOf("epub")

    val ZIP_EXTENSIONS = if(UmPlatformUtil.isWeb) listOf(".zip") else listOf("zip")

}