package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.XapiPackageContentView

import java.util.Arrays

/**
 * Created by mike on 9/13/17.
 *
 *
 */

class XapiPackageTypePlugin : ContentTypePlugin() {

    override val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val mimeTypes: List<String>
        get() = Arrays.asList(*MIME_TYPES)

    override val fileExtensions: List<String>
        get() = Arrays.asList(*FILE_EXTENSIONS)

    companion object {

        private val MIME_TYPES = arrayOf("application/zip")

        private val FILE_EXTENSIONS = arrayOf("zip")

        //As per spec - there should be one and only one tincan.xml file
        protected val XML_FILE_NAME = "tincan.xml"
    }

}
