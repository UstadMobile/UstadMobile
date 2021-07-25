package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.view.EpubContentView

/**
 * Created by mike on 9/9/17.
 */

abstract class EpubTypePlugin : ContentPlugin {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = listOf(*MIME_TYPES)

    override val supportedFileExtensions: List<String>
        get() = listOf(*EXTENSIONS)

    companion object {

        val MIME_TYPES = arrayOf("application/epub+zip")

        val EXTENSIONS = arrayOf("epub")

        val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }

}
