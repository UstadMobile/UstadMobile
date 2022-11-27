package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.ContentPlugin

/**
 * Interface that is used simply as a marker for DI purposes and to hold some constants. The PDF
 * plugins are children of ContentImportContentPlugin on Android and JVM
 */
interface PDFTypePlugin : ContentPlugin {

    companion object {

        var PDF_MIME_MAP = mapOf("application/pdf" to ".pdf" ,
                                "application/x-pdf" to ".pdf" ,
                                "application/octet-stream" to ".pdf")

        val PDF_EXT_LIST = listOf(".pdf")


        const val PLUGIN_ID = 18

    }


}