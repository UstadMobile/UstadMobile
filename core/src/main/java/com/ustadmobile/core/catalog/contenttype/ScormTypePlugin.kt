package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.ScormPackageView

import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.io.InputStream
import java.util.Arrays

/**
 * Created by mike on 1/6/18.
 */

class ScormTypePlugin : ContentTypePlugin() {

    override val viewName: String
        get() = ScormPackageView.VIEW_NAME

    override val mimeTypes: List<String>
        get() = Arrays.asList(*MIME_TYPES)

    override val fileExtensions: List<String>
        get() = Arrays.asList(*arrayOf("zip"))

    companion object {

        val MIME_TYPES = arrayOf("application/scorm+zip")
    }

}
