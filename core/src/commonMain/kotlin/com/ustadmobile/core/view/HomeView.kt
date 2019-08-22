package com.ustadmobile.core.view

interface HomeView : UstadView {

    fun showDownloadAllButton(show:Boolean)

    fun loadProfileIcon(profileUrl: String);

    companion object {

        const val VIEW_NAME = "Home"
    }
}
