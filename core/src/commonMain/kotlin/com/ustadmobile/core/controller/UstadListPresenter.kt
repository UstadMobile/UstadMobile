package com.ustadmobile.core.controller

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.core.view.UstadView

abstract class UstadListPresenter<V: UstadView, RT>(context: Any, arguments: Map<String, String>, view: V):
        UstadBaseController<V>(context, arguments, view) {

    protected var mListMode = ListViewMode.BROWSER

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        mListMode = ListViewMode.valueOf(
                arguments[UstadView.ARG_LISTMODE] ?: ListViewMode.BROWSER.toString())
    }

    abstract fun handleClickEntry(entry: RT)

    open fun handleClickSortOrder(sortOption: MessageIdOption) {

    }

    abstract fun handleClickCreateNew()

}