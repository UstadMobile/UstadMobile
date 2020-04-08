package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.lib.db.entities.UmAccount

class SelQuestionSetListPresenter(context: Any, arguments: Map<String, String>,
                          view: SelQuestionSetListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<SelQuestionSetListView, SelQuestionSet>(context, arguments, view,
        lifecycleOwner, systemImpl, db, repo, activeAccount) {


    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc)
    }

    class SelQuestionSetListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map {
            SelQuestionSetListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO("check on add permission for this account: e.g. " +
        //        "repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)")
        return true
    }

    private fun updateListOnView() {
        view.list = repo.selQuestionSetDao.findAllQuestionSetsWithNumQuestions()
    }

    override fun handleClickEntry(entry: SelQuestionSet) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(SelQuestionSetEditView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.selQuestionSetUid.toString()), context)
        }

    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SelQuestionSetEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
    }
}