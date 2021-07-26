package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.ChartData
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
import kotlinx.serialization.builtins.ListSerializer
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
            safeParseList(di,
                ListSerializer(ReportSeries.serializer()),
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
            view.statementListDetails = statementList
            view.loading = false

            loggedInPerson = withTimeoutOrNull(2000){
                db.personDao.findByUidAsync(loggedInPersonUid)
            }
            view.saveAsTemplateVisible = loggedInPerson?.admin ?: false
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
        GlobalScope.launch(doorMainDispatcher()) {
            report.reportOwnerUid = loggedInPersonUid
            repo.reportDao.insertAsync(report)
        }
    }

    fun handleOnClickAddAsTemplate(report: ReportWithSeriesWithFilters) {
        GlobalScope.launch(doorMainDispatcher()) {
            report.isTemplate = true
            report.reportUid = 0
            report.reportOwnerUid = 0
            repo.reportDao.insertAsync(report)
        }
    }

    fun generateCSVFile() {
        GlobalScope.launch {
            val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
            val report = withTimeoutOrNull(2000) {
                db.reportDao.findByUid(entityUid)
            } ?: Report()


            val series = if (!report.reportSeries.isNullOrEmpty()) {
                safeParseList(
                    di,
                    ListSerializer(ReportSeries.serializer()),
                    ReportSeries::class, report.reportSeries ?: ""
                )
            } else {
                listOf()
            }
            val reportWithFilter = ReportWithSeriesWithFilters(report, series)
            var chartData =
                db.generateChartData(reportWithFilter, context, systemImpl, loggedInPersonUid)
            if(chartData.seriesData.isEmpty() && view.chartData != null &&
                view.chartData?.seriesData?.isNotEmpty() == true){
                chartData = view.chartData as ChartData
            }

            val csvString = StringBuilder()
            //Add Report title
            csvString.append(chartData?.reportWithFilters?.reportTitle)
            val reportDesc = chartData?.reportWithFilters?.reportDescription ?: ""
            //Add Report description (if it exists)
            if (reportDesc.isNotEmpty()) {
                csvString.append(" (")
                csvString.append(reportDesc)
                csvString.append(") ")
            }
            csvString.append("\n")
            //Add Column names
            val yAxisId =
                chartData?.reportWithFilters?.reportSeriesWithFiltersList?.get(0)?.reportSeriesYAxis
                    ?: 0
            val xAxisId = chartData?.reportWithFilters?.xAxis ?: 0
            val yLabel = UstadMobileSystemImpl.instance.getString(
                ReportEditPresenter.YAxisOptions.values()
                    .filter { it.optionVal == yAxisId }[0].messageId, context
            )
            val xLabel = UstadMobileSystemImpl.instance.getString(
                ReportEditPresenter.XAxisOptions.values()
                    .filter { it.optionVal == xAxisId }[0].messageId,
                context
            )
            csvString.append("\n")
            csvString.append(xLabel)
            csvString.append(",")
            csvString.append(yLabel)
            csvString.append("\n")

            for (everyData in chartData?.seriesData ?: emptyList()) {
                //For every series, add Series name
                csvString.append(everyData.series.reportSeriesName)
                csvString.append("\n")

                //Get chartData for series
                for (everySeriesData in everyData.dataList) {
                    var formattedX =
                        chartData?.xAxisValueFormatter?.format(everySeriesData.xAxis?:"0")
                    if(formattedX == null){
                        formattedX = everySeriesData.xAxis
                    }
                    csvString.append(formattedX)

                    csvString.append(",")
                    csvString.append(everySeriesData.yAxis.toString())
                    csvString.append("\n")
                }
            }

            csvString.append("\n")

            withContext(doorMainDispatcher()) {
                view.shareCSVData(csvString)
            }

        }

    }

}