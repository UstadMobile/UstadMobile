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
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.list
import org.kodein.di.DI


class ReportDetailPresenter(context: Any,
                            arguments: Map<String, String>, view: ReportDetailView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ReportDetailView, ReportWithSeriesWithFilters>(context, arguments, view, di, lifecycleOwner) {

    private var loggedInPerson: Person? = null

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val loggedInPersonUid = accountManager.activeAccount.personUid

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        GlobalScope.launch(doorMainDispatcher()) {

        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithSeriesWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        val report = withTimeoutOrNull(2000) {
            db.reportDao.findByUid(entityUid)
        } ?: Report()


        val series = if (!report.reportSeries.isNullOrEmpty()) {
            safeParseList(di, ReportSeries.serializer().list,
                    ReportSeries::class, report.reportSeries ?: "")
        } else {
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
        GlobalScope.launch(doorMainDispatcher()) {
            val chartData = db.generateChartData(reportWithFilters, context, systemImpl, loggedInPersonUid)
            val statementList = db.generateStatementList(reportWithFilters, loggedInPersonUid)
            view.chartData = chartData
            view.statementList = statementList
            view.loading = false

            loggedInPerson = withTimeoutOrNull(2000){
                db.personDao.findByUidAsync(loggedInPersonUid)
            }
            view.isAdmin = loggedInPerson?.admin ?: false
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
        GlobalScope.launch() {
            report.reportOwnerUid = loggedInPersonUid
            repo.reportDao.insert(report)
        }
    }

    fun handleOnClickAddAsTemplate(report: ReportWithSeriesWithFilters) {
        GlobalScope.launch(doorMainDispatcher()) {
            report.isTemplate = true
            report.reportUid = 0
            report.reportOwnerUid = 0
            repo.reportDao.insert(report)
        }
    }

}