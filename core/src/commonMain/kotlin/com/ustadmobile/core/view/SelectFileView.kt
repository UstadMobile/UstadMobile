package com.ustadmobile.core.view

interface SelectFileView : UstadView {

    companion object {

        const val VIEW_NAME = "SelectFileView"

        const val ARG_SELECT_FILE = "selectFile"

        const val SELECT_GALLERY = "video/*;audio/*"

        const val SELECT_FILE = "*/*"

    }
}
