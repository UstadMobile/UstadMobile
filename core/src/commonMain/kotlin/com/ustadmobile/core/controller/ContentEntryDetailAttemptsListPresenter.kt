package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.db.dao.StatementDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ContentEntryDetailAttemptsListPresenter(context: Any, arguments: Map<String, String>,
                                              view: ContentEntryDetailAttemptsListView,
                                              di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<ContentEntryDetailAttemptsListView, PersonWithAttemptsSummary>(
        context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted,
        AttemptListListener {

    private var contentEntryUid: Long = 0L

    var searchText: String? = null

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedSortOption = SORT_OPTIONS[0]
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

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    private fun updateListOnView() {
        view.list = repo.statementDao.findPersonsWithContentEntryAttempts(contentEntryUid,
                mLoggedInPersonUid, searchText.toQueryLikeParam(),
                selectedSortOption?.flag ?: StatementDaoCommon.SORT_FIRST_NAME_ASC)
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }


    override fun handleClickCreateNewFab() {

    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    override fun onClickPersonWithStatementDisplay(personWithAttemptsSummary: PersonWithAttemptsSummary) {
            systemImpl.go(SessionListView.VIEW_NAME,
                    mapOf(UstadView.ARG_CONTENT_ENTRY_UID to contentEntryUid.toString(),
                            UstadView.ARG_PERSON_UID to
                                    personWithAttemptsSummary.personUid.toString()), context)
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, StatementDaoCommon.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, StatementDaoCommon.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, StatementDaoCommon.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, StatementDaoCommon.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.last_active, StatementDaoCommon.SORT_LAST_ACTIVE_ASC, true),
                SortOrderOption(MessageID.last_active, StatementDaoCommon.SORT_LAST_ACTIVE_DESC, false)
        )

    }

}
