package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.EpubContentView

/**
 * Created by mike on 9/9/17.
 */

open class EpubTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf(*EXTENSIONS)

    companion object {

        val MIME_TYPES = arrayOf("application/epub+zip")

        val EXTENSIONS = arrayOf("epub")

        val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }

}
