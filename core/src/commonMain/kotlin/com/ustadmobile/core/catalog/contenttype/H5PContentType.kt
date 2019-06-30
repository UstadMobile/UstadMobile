package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.view.H5PContentView

/**
 * Created by mike on 2/15/18.
 */

open class H5PContentType : ContentTypePlugin {
    override val viewName: String
        get() = H5PContentView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = arrayOf("application/h5p+zip")

    override val fileExtensions: Array<String>
        get() = arrayOf("h5p")

    companion object{
        const val TITLE_TAG = "title"

        const val DESCRIPTION_TAG = "metaDescription"

        const val AUTHOR_TAG = "author"

        const val LICENSE_TAG = "license"
    }
}
