package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ReportListPresenter(context: Any, arguments: Map<String, String>, view: ReportListView,
        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ReportListView, Report>(context, arguments, view, di, lifecycleOwner) {

    var loggedInPersonUid = 0L

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ReportListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ReportListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true

    }

    private fun updateListOnView() {
        view.list = when (currentSortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.reportDao.findAllActiveReportByUserAsc(loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.reportDao.findAllActiveReportByUserDesc(loggedInPersonUid)
        }
    }

    override fun handleClickEntry(entry: Report) {
        when (mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ReportDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.reportUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ReportEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ReportListSortOption)?.sortOrder ?: return
        if (sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    suspend fun getGraphData(item: Report): ReportGraphHelper.ChartData {
        val reportList = db.reportFilterDao.findByReportUid(item.reportUid)
        val graphHelper = ReportGraphHelper(context, systemImpl, repo)
        return graphHelper.getChartDataForReport(ReportWithFilters(item, reportList))
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}