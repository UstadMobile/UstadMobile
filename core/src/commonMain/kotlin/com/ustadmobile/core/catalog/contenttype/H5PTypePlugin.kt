package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.XapiPackageContentView

open class H5PTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf(*EXTENSIONS)

    companion object {

        val MIME_TYPES = arrayOf("application/tincan+zip", "application/zip")

        val EXTENSIONS = arrayOf("h5p")

        protected val XML_FILE_NAME = "tincan.xml"
    }


}