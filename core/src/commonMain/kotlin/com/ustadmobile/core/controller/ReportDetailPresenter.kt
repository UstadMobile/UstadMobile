package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*


class ReportDetailPresenter(context: Any,
                            arguments: Map<String, String>, view: ReportDetailView,
                            lifecycleOwner: DoorLifecycleOwner,
                            systemImpl: UstadMobileSystemImpl,
                            db: UmAppDatabase, repo: UmAppDatabase,
                            activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ReportDetailView, ReportWithFilters>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    val graphHelper = ReportGraphHelper(context, systemImpl, repo)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)


    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        val report = withTimeoutOrNull(2000) {
            db.reportDao.findByUid(entityUid)
        } ?: Report()

        val reportFilterList = withTimeout(2000) {
            db.reportFilterDao.findByReportUid(report.reportUid)
        }

        val reportWithFilter = ReportWithFilters(report, reportFilterList)

        setReportData(reportWithFilter)

        return reportWithFilter
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        val editEntity: ReportWithFilters?
        editEntity = if (entityJsonStr != null) {
            Json.parse(ReportWithFilters.serializer(), entityJsonStr)
        } else {
            ReportWithFilters()
        }

        setReportData(editEntity)

        return editEntity
    }

    private fun setReportData(reportWithFilters: ReportWithFilters) {
        GlobalScope.launch {

            val chartData = DoorMutableLiveData(graphHelper.getChartDataForReport(reportWithFilters))
            val statementList = graphHelper.getStatementListForReport(reportWithFilters)

            view.runOnUiThread(Runnable {
                view.chartData = chartData
                view.statementList = statementList
            })

        }


    }


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickEdit() {
        val report = arguments[ARG_ENTITY_UID]?.toLong() ?: return
        systemImpl.go(ReportEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to report.toString()),
                context)
    }


    /**
     *
     */
    fun handleOnClickAddOrRemoveFromDashboard(report: ReportWithFilters) {
        if (report.reportUid != 0L) {
            repo.reportDao.updateReportInactive(!report.reportInactive, report.reportUid)
        } else {
            repo.reportDao.insert(report)
            repo.reportFilterDao.insertList(report.reportFilterList)
        }
    }

    companion object {


    }


}