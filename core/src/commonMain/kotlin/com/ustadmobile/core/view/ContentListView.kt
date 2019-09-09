package com.ustadmobile.core.view

/**
 * FeedList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ContentListView : UstadView {

    fun showDownloadAllButton(show:Boolean)

    companion object {
        //View name
        val VIEW_NAME = "ContentList"
    }

}
