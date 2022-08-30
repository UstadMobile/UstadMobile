package com.ustadmobile.core.controller

import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.StatementListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class SessionListPresenter(context: Any, arguments: Map<String, String>,
                           view: SessionListView,
                           di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<SessionListView, PersonWithSessionsDisplay>(
        context, arguments, view, di, lifecycleOwner) {

    private var contentEntryUid: Long = 0L
    private var selectedPersonUid: Long = 0L

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()
        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0
        selectedPersonUid = arguments[UstadView.ARG_PERSON_UID]?.toLong() ?: 0
        mLoggedInPersonUid = accountManager.activeAccount.personUid
        GlobalScope.launch(doorMainDispatcher()) {

            val person = db.personDao.findByUidAsync(selectedPersonUid)
            val entry = db.contentEntryDao.findByUidAsync(contentEntryUid)

            view.personWithContentTitle = "${person?.fullName()} - ${entry?.title}"

        }
        updateListOnView()

    }

    private fun updateListOnView() {
        view.list = repo.statementDao.findSessionsForPerson(contentEntryUid, mLoggedInPersonUid,
                selectedPersonUid)
    }

    override fun handleClickCreateNewFab() {

    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    fun onClickPersonWithSessionDisplay(personWithStatementDisplay: PersonWithSessionsDisplay) {
        systemImpl.go(StatementListView.VIEW_NAME,
                mapOf(UstadView.ARG_CONTENT_ENTRY_UID to contentEntryUid.toString(),
                        UstadView.ARG_PERSON_UID to selectedPersonUid.toString(),
                        SessionListView.ARG_CONTEXT_REGISTRATION to
                        personWithStatementDisplay.contextRegistration), context)
    }

}
