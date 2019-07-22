package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.ScormPackageView

/**
 * Created by mike on 1/6/18.
 */

class ScormTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = ScormPackageView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf("zip")

    companion object {

        val MIME_TYPES = arrayOf("application/scorm+zip")
    }

}
