package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class LeavingReasonListPresenter(context: Any, arguments: Map<String, String>, view: LeavingReasonListView,
                                 di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<LeavingReasonListView, LeavingReason>(context, arguments, view, di, lifecycleOwner) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        GlobalScope.launch(doorMainDispatcher()){
            view.list = repo.leavingReasonDao.findAllReasons()
        }
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                LeavingReasonEditView.VIEW_NAME,
                LeavingReason::class,
                LeavingReason.serializer())
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }

    fun onClickLeavingReason(leavingReason: LeavingReason) {
        when(mListMode) {
            ListViewMode.PICKER -> finishWithResult(safeStringify(di,
                ListSerializer(LeavingReason.serializer()), listOf(leavingReason)))
            ListViewMode.BROWSER -> navigateForResult(
                NavigateForResultOptions(this,
                    leavingReason, LeavingReasonEditView.VIEW_NAME,
                    LeavingReason::class,
                    LeavingReason.serializer(),
                    arguments = mapOf(
                        UstadView.ARG_ENTITY_UID to leavingReason.leavingReasonUid.toString())
                        .toMutableMap()))
        }
    }

}