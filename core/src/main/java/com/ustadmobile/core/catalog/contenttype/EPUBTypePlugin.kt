package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.EpubContentView

import java.util.Arrays

/**
 * Created by mike on 9/9/17.
 */

class EPUBTypePlugin : ContentTypePlugin() {

    override val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val mimeTypes: List<String>
        get() = Arrays.asList(*MIME_TYPES)

    override val fileExtensions: List<String>
        get() = Arrays.asList(*EXTENSIONS)

    companion object {

        val MIME_TYPES = arrayOf("application/epub+zip")

        val EXTENSIONS = arrayOf("epub")

        val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }

}
