package com.ustadmobile.core.controller

import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class LeavingReasonListPresenter(context: Any, arguments: Map<String, String>, view: LeavingReasonListView,
                                 di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<LeavingReasonListView, LeavingReason>(context, arguments, view, di, lifecycleOwner) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickCreateNewFab() {
        /* TODO: Add code to go to the edit view when the user clicks the new item FAB. This is only
         * called when the fab is clicked, not if the first item is create new item (e.g. picker mode).
         * That has to be handled at a platform level to use prepareCall etc.
        systemImpl.go(LeavingReasonEditView.VIEW_NAME, mapOf(), context)
         */
    }

    fun onClickLeavingReason(leavingReason: LeavingReason) {
        view?.finishWithResult(listOf(leavingReason))
    }

}