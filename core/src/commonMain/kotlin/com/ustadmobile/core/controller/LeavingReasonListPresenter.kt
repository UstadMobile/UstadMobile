package com.ustadmobile.core.controller

import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class LeavingReasonListPresenter(context: Any, arguments: Map<String, String>, view: LeavingReasonListView,
                                 di: DI, lifecycleOwner: DoorLifecycleOwner)
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

    }

    fun onClickLeavingReason(leavingReason: LeavingReason) {
        view?.finishWithResult(listOf(leavingReason))
    }

}