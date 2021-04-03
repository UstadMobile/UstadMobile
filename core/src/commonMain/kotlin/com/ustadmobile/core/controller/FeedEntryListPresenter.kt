package com.ustadmobile.core.controller

import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class FeedEntryListPresenter(context: Any, arguments: Map<String, String>, view: FeedEntryListView,
                             di: DI, lifecycleOwner: DoorLifecycleOwner,
                             private val feedEntryItemListener: DefaultFeedEntryListItemListener = DefaultFeedEntryListItemListener(view, ListViewMode.BROWSER, context, di))
    : UstadListPresenter<FeedEntryListView, FeedEntry>(context, arguments, view, di, lifecycleOwner), FeedEntryListItemListener by feedEntryItemListener {


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        feedEntryItemListener.listViewMode = mListMode
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false //Feed entries are generated, not added
    }

    private fun updateListOnView() {
        view.list = db.feedEntryDao.findByPersonUidAsDataSource(
            accountManager.activeAccount.personUid)
    }

    override fun handleClickCreateNewFab() {
        /* TODO: Add code to go to the edit view when the user clicks the new item FAB. This is only
         * called when the fab is clicked, not if the first item is create new item (e.g. picker mode).
         * That has to be handled at a platform level to use prepareCall etc.
        systemImpl.go(FeedEntryEditView.VIEW_NAME, mapOf(), context)
         */
    }


}