package com.ustadmobile.core.controller

import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ScopedGrantListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ScopedGrantListView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    private val scopedGrantItemListener: DefaultScopedGrantListItemListener = DefaultScopedGrantListItemListener(view, ListViewMode.BROWSER, context, di)
): UstadListPresenter<ScopedGrantListView, ScopedGrant>(context, arguments, view, di, lifecycleOwner),
    ScopedGrantListItemListener by scopedGrantItemListener
{

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        scopedGrantItemListener.listViewMode = mListMode
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.list = repo.scopedGrantDao.findByTableIdAndEntityUidWithNameAsDataSource(
            arguments[ScopedGrantListView.ARG_FILTER_TABLE_ID]?.toInt() ?: 0,
            arguments[ScopedGrantListView.ARG_FILTER_ENTITY_UID]?.toLong() ?: 0L)
    }

    override fun handleClickCreateNewFab() {
        /* TODO: Add code to go to the edit view when the user clicks the new item FAB. This is only
         * called when the fab is clicked, not if the first item is create new item (e.g. picker mode).
         * That has to be handled at a platform level to use prepareCall etc.
        systemImpl.go(ScopedGrantEditView.VIEW_NAME, mapOf(), context)
         */
    }

}