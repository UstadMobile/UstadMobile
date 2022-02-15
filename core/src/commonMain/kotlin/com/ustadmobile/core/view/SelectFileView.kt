package com.ustadmobile.core.view

interface SelectFileView : UstadView {

    companion object {

        const val VIEW_NAME = "SelectFileView"

        const val ARG_MIMETYPE_SELECTED = "selectMimeTypeMode"

        const val SELECTION_MODE_GALLERY = "video/*;audio/*"

        const val SELECTION_MODE_VIDEO = "video/*"

        const val SELECTION_MODE_AUDIO = "audio/*"

        const val SELECTION_MODE_IMAGE = "image/*"

        const val SELECTION_MODE_ANY = "*/*"

        val SELECTION_MODE_DOC = listOf(
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.presentation",
                "application/msword",
                "application/vnd.ms-powerpoint",
                "application/pdf",
                "application/octet-stream",)
                .joinToString(";")

    }
}
