package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
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
            ListViewMode.PICKER -> finishWithResult(
                safeStringify(di, ListSerializer(Report.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(ReportDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.reportUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ReportTemplateListView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickAddNewItem(args: Map<String, String> ?, destinationResultKey: String?) {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                ReportEditView.VIEW_NAME,
                Report::class,
                Report.serializer(),
                destinationResultKey,
                arguments = args?.toMutableMap() ?: mutableMapOf()
            )
        )
    }


    override suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        return listOf(SelectionOption.HIDE)
    }

    override fun handleClickSelectionOption(selectedItem: List<Report>, option: SelectionOption) {
        GlobalScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.HIDE -> {
                    repo.reportDao.toggleVisibilityReportItems(true,
                            selectedItem.map { it.reportUid }, systemTimeInMillis())
                    view.showSnackBar(systemImpl.getString(MessageID.action_hidden, context), {

                        GlobalScope.launch(doorMainDispatcher()){
                            repo.reportDao.toggleVisibilityReportItems(false,
                                    selectedItem.map { it.reportUid }, systemTimeInMillis())
                        }

                    }, MessageID.undo)
                }
            }
        }
    }

    companion object {

        const val REPORT_RESULT_KEY = "Report"

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.title, ReportDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ReportDao.SORT_TITLE_DESC, false)
        )

    }

}