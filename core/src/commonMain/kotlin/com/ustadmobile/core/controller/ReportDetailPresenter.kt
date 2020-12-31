package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.generateChartData
import com.ustadmobile.core.util.ext.generateStatementList
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.list
import org.kodein.di.DI


class ReportDetailPresenter(context: Any,
                            arguments: Map<String, String>, view: ReportDetailView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ReportDetailView, ReportWithSeriesWithFilters>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val loggedInPersonUid = accountManager.activeAccount.personUid

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithSeriesWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        val report = withTimeoutOrNull(2000) {
            db.reportDao.findByUid(entityUid)
        } ?: Report()


        val series = if(!report.reportSeries.isNullOrEmpty()){
            safeParseList(di, ReportSeries.serializer().list,
                    ReportSeries::class, report.reportSeries ?: "")
        }else{
            listOf()
        }

        val reportWithFilter = ReportWithSeriesWithFilters(report, series)

        setReportData(reportWithFilter)

        return reportWithFilter
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithSeriesWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        var editEntity: ReportWithSeriesWithFilters? = null
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ReportWithSeriesWithFilters.serializer(), entityJsonStr)
            setReportData(editEntity)
        }

        return editEntity
    }

    private fun setReportData(reportWithFilters: ReportWithSeriesWithFilters) {
        view.loading = true
        GlobalScope.launch {
            val chartData = db.generateChartData(reportWithFilters, context, systemImpl, loggedInPersonUid)
            val statementList = db.generateStatementList(reportWithFilters, loggedInPersonUid)
            view.runOnUiThread(Runnable {
                view.chartData = chartData
                view.statementList = statementList
                view.loading = false
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
    fun handleOnClickAddFromDashboard(report: ReportWithSeriesWithFilters) {
        GlobalScope.launch(){
            repo.reportDao.insert(report)
        }
    }

    companion object {


    }


}