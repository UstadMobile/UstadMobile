package com.ustadmobile.core.catalog.contenttype


/**
 * Created by mike on 1/6/18.
 */

class ScormTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = "ScormViewTodo"

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf("zip")

    companion object {

        val MIME_TYPES = arrayOf("application/scorm+zip")
    }

}
