package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.view.VideoContentView

abstract class PDFTypePlugin : ContentPlugin {

    val viewName: String
        get() = VideoContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = PDF_MIME_MAP.keys.toList()

    override val supportedFileExtensions: List<String>
        get() = PDF_EXT_LIST.map { it.removePrefix(".") }

    override val pluginId: Int
        get() = PLUGIN_ID

    companion object {

        var PDF_MIME_MAP = mapOf("application/pdf" to ".pdf" ,
                                "application/x-pdf" to ".pdf" )

        val PDF_EXT_LIST = listOf(".pdf")


        const val PLUGIN_ID = 14

    }


}