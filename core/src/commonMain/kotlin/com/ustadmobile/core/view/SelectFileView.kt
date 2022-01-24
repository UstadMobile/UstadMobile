package com.ustadmobile.core.view

import com.ustadmobile.core.contentjob.SupportedContent

interface SelectFileView : UstadEditView<Any> {

    var acceptedMimeTypes: List<String>

    var noFileSelectedError: String?

    var unSupportedFileError: String?

    companion object {

        const val VIEW_NAME = "SelectFileView"

        const val ARG_SELECTION_MODE = "selectMimeTypeMode"

        const val SELECTION_MODE_GALLERY = "video/*;audio/*"

        val SELECTION_MODE_FILE =
                (SupportedContent.EPUB_MIME_TYPES +
                SupportedContent.XAPI_MIME_TYPES +
                SupportedContent.H5P_EXTENSIONS +
                        "application/octet-stream").joinToString(";")

    }
}
