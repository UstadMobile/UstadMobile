package com.ustadmobile.core.view

interface SelectExtractFileView : UstadEditView<Any> {

    var acceptedMimeTypes: List<String>

    var noFileSelectedError: String?

    var unSupportedFileError: String?

    companion object {

        const val VIEW_NAME = "SelectExtractFileView"

    }
}
