package com.ustadmobile.core.view


interface HtmlTextViewDetailView: UstadDetailView<String> {

    var title: String?

    companion object {

        const val VIEW_NAME = "StringDetailView"

        const val DISPLAY_TEXT = "textToDisplay"

        const val DISPLAY_TITLE = "titleToDisplay"

    }

}