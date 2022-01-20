package com.ustadmobile.core.view

import com.ustadmobile.core.contentjob.SupportedContent

interface SelectFileView : UstadView {

    companion object {

        const val VIEW_NAME = "SelectFileView"

        const val ARG_SELECTION_MODE = "selectMimeTypeMode"

        const val SELECTION_MODE_GALLERY = "video/*;audio/*"

        const val SELECTION_MODE_VIDEO = "video/*"

        const val SELECTION_MODE_AUDIO = "audio/*"

        const val SELECTION_MODE_IMAGE = "image/*"

        const val SELECTION_MODE_ANY = "*/*"

        val SELECTION_MODE_FILE =
                (SupportedContent.EPUB_MIME_TYPES +
                SupportedContent.XAPI_MIME_TYPES +
                SupportedContent.H5P_EXTENSIONS +
                        "application/octet-stream" +
                        "application/pdf").joinToString(";")

    }
}
