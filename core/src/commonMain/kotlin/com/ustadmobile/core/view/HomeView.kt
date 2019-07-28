package com.ustadmobile.core.view

interface HomeView : UstadView {

    fun showDownloadAllButton(show:Boolean)

    companion object {

        const val VIEW_NAME = "Home"
    }
}
