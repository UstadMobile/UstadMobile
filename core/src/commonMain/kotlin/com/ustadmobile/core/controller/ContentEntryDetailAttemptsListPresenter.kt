package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithStatementDisplay
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ContentEntryDetailAttemptsListPresenter(context: Any, arguments: Map<String, String>, view: ContentEntryDetailAttemptsListView,
                                              di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ContentEntryDetailAttemptsListView, PersonWithStatementDisplay>(context, arguments, view, di, lifecycleOwner){

    private var contentEntryUid: Long = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contentEntryUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()
        mLoggedInPersonUid = accountManager.activeAccount.personUid
        updateListOnView()
    }


    private fun updateListOnView() {
        view.list = repo.personDao.findPersonsWithContentEntryAttempts(contentEntryUid, mLoggedInPersonUid)
    }

    override fun handleClickCreateNewFab() {

    }

    fun onClickPersonWithStatementDisplay(personWithStatementDisplay: PersonWithStatementDisplay) {
       /* systemImpl.go(LeavingReasonEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to leavingReason.leavingReasonUid.toString()), context)*/
    }
}