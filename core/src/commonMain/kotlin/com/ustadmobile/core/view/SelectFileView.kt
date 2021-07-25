package com.ustadmobile.core.view

import com.ustadmobile.core.catalog.contenttype.EpubTypePlugin
import com.ustadmobile.core.catalog.contenttype.H5PTypePlugin
import com.ustadmobile.core.catalog.contenttype.XapiPackageTypePlugin

interface SelectFileView : UstadView {

    companion object {

        const val VIEW_NAME = "SelectFileView"

        const val ARG_SELECTION_MODE = "selectMode"

        const val SELECTION_MODE_GALLERY = "video/*;audio/*"

        val SELECTION_MODE_FILE =
                (EpubTypePlugin.MIME_TYPES +
                XapiPackageTypePlugin.MIME_TYPES +
                H5PTypePlugin.MIME_TYPES +
                        "application/octet-stream").joinToString(";")

    }
}
