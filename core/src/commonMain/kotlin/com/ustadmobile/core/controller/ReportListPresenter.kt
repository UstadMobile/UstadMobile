package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ReportListPresenter(context: Any, arguments: Map<String, String>, view: ReportListView,
        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ReportListView, Report>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted{

    var loggedInPersonUid = 0L

    var searchText: String? = null
    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedSortOption = SORT_OPTIONS[0]
        loggedInPersonUid = accountManager.activeAccount.personUid
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        view.list = repo.reportDao.findAllActiveReport(searchText.toQueryLikeParam(),
        loggedInPersonUid, selectedSortOption?.flag ?: ReportDao.SORT_TITLE_ASC, false)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }


    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    override fun handleClickEntry(entry: Report) {
        when (mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ReportDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.reportUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ReportTemplateView.VIEW_NAME, mapOf(), context)
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.title, ReportDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ReportDao.SORT_TITLE_DESC, false)
        )

    }

}