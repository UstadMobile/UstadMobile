package com.ustadmobile.core.view

interface ContentEntryListView : ContentWithOptionsView {

    fun showCreateContentOption(showOption: Boolean)

    fun navigateBack()

    fun showMessage(message: String)

    companion object {

        const val VIEW_NAME = "ContentEntryList"

        const val CONTENT_CREATE_FOLDER = 1

        const val CONTENT_IMPORT_FILE = 2

        const val CONTENT_CREATE_CONTENT = 3

        const val CONTENT_IMPORT_LINK = 4
    }
}