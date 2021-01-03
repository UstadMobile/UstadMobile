package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.withRepoTimeout
import com.ustadmobile.lib.db.entities.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ReportEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ReportEditView,
                          di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportEditView, ReportWithSeriesWithFilters>(context, arguments, view, di, lifecycleOwner) {

    val seriesCounter = atomic(0)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class VisualTypeOptions(val optionVal: Int, val messageId: Int) {
        BAR_CHART(Report.BAR_CHART,
                MessageID.bar_chart),
        LINE_GRAPH(Report.LINE_GRAPH,
                MessageID.line_graph)
    }

    class VisualTypeMessageIdOption(day: VisualTypeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class XAxisOptions(val optionVal: Int, val messageId: Int) {
        DAY(Report.DAY,
                MessageID.day),
        WEEK(Report.WEEK,
                MessageID.xapi_week),
        MONTH(Report.MONTH,
                MessageID.xapi_month),
        CONTENT_ENTRY(Report.CONTENT_ENTRY,
                MessageID.xapi_content_entry),
        GENDER(Report.GENDER,
                MessageID.gender_literal),
        CLASS(Report.CLASS,
                MessageID.clazz)
    }

    class XAxisMessageIdOption(day: XAxisOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    class SubGroupByMessageIdOption(day: XAxisOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class DataSetOptions(val optionVal: Int, val messageId: Int) {
        TOTAL_DURATION(ReportSeries.TOTAL_DURATION,
                MessageID.total_duration),
        AVERAGE_DURATION(ReportSeries.AVERAGE_DURATION,
                MessageID.average_duration),
        NUMBER_SESSIONS(ReportSeries.NUMBER_SESSIONS,
                MessageID.count_session),
        ACTIVITIES_RECORDED(ReportSeries.ACTIVITIES_RECORDED,
                MessageID.activity_recorded),
        AVERAGE_SESSION_PER_CONTENT(ReportSeries.AVERAGE_SESSION_PER_CONTENT,
                MessageID.average_session_per_content),
        PERCENT_STUDENTS_COMPLETED(ReportSeries.PERCENT_STUDENTS_COMPLETED,
                MessageID.percent_students_completed),
        NUMBER_STUDENTS_COMPLETED(ReportSeries.NUMBER_STUDENTS_COMPLETED,
                MessageID.count_students_completed)
    }

    class DataSetMessageIdOption(data: DataSetOptions, context: Any)
        : MessageIdOption(data.messageId, context, data.optionVal)


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.visualTypeOptions = VisualTypeOptions.values().map { VisualTypeMessageIdOption(it, context) }
        view.xAxisOptions = XAxisOptions.values().map { XAxisMessageIdOption(it, context) }
        view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }
        view.dataSetOptions = DataSetOptions.values().map { DataSetMessageIdOption(it, context) }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithSeriesWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        var report = db.withRepoTimeout(2000) {
            it.takeIf { entityUid != 0L }?.reportDao?.findByUid(entityUid)
        }

        if(report == null){
            report = Report()
         /*   report.fromDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong - 7.days.millisecondsLong
            report.toDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong*/
        }

        handleXAxisSelected(XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }.find { it.code == report.xAxis } as MessageIdOption)

        val reportSeries = report.reportSeries
        var reportSeriesList = listOf<ReportSeries>()
        if(!reportSeries.isNullOrBlank()) {
            reportSeriesList = safeParseList(di, ReportSeries.serializer().list, ReportSeries::class, reportSeries)
            // set the series counter with an existing series
            val max = reportSeriesList.maxBy { it.reportSeriesUid }
            seriesCounter.value = (max?.reportSeriesUid?: 0 + 1).toInt()
        }else{
            reportSeriesList = listOf(ReportSeries().apply {
                val id = seriesCounter.getAndIncrement()
                reportSeriesName = "Series $id"
                reportSeriesUid = id.toLong()
            })
        }

        return ReportWithSeriesWithFilters(report, reportSeriesList)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithSeriesWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: ReportWithSeriesWithFilters
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ReportWithSeriesWithFilters.serializer(), entityJsonStr)
        } else {
            editEntity = ReportWithSeriesWithFilters()
           /* editEntity.fromDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong - 7.days.millisecondsLong
            editEntity.toDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong*/
        }

        handleXAxisSelected(XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }.find { it.code == editEntity.xAxis } as MessageIdOption)

        val reportSeries = editEntity.reportSeries
        val reportSeriesList: List<ReportSeries>
        if(!reportSeries.isNullOrBlank()) {
            reportSeriesList = safeParseList(di, ReportSeries.serializer().list, ReportSeries::class, reportSeries)
            // set the series counter with an existing series
            val max = reportSeriesList.maxBy { it.reportSeriesUid }
            seriesCounter.value = (max?.reportSeriesUid?: 0 + 1).toInt()
        }else{
            reportSeriesList = listOf(ReportSeries().apply {
                val id = seriesCounter.getAndIncrement()
                reportSeriesName = "Series $id"
                reportSeriesUid = id.toLong()
            })
        }
        editEntity.reportSeriesWithFiltersList = reportSeriesList

        return editEntity
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    fun handleRemoveSeries(series: ReportSeries){
        val entityVal = entity
        val newList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        newList.remove(series)
        entityVal?.reportSeriesWithFiltersList = newList.toList()
        view.entity = entityVal
    }

    fun handleClickAddSeries(){
        val series = ReportSeries().apply {
            val id = seriesCounter.getAndIncrement()
            reportSeriesName = "Series $id"
            reportSeriesUid = id.toLong()
        }
        val entityVal = entity
        val newList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        newList.add(series)
        entityVal?.reportSeriesWithFiltersList = newList.toList()
        view.entity = entityVal
    }

    fun handleAddFilter(newFilter: ReportFilter){
        val entityVal = entity
        val newSeriesList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        val seriesToAddFilter = newSeriesList.find { it.reportSeriesUid == newFilter.reportFilterSeriesUid } ?: return

        newSeriesList.remove(seriesToAddFilter)
        val newFilterList = seriesToAddFilter.reportSeriesFilters.toMutableList()

        // make sure reportfilterUid is not repeated in the same series list
        newFilter.reportFilterUid = seriesToAddFilter.reportSeriesFilters.maxBy { it.reportFilterUid }?.reportFilterUid?: 0 + 1
        newFilterList.add(newFilter)

        newSeriesList.add(ReportSeries().apply {
            reportSeriesUid = seriesToAddFilter.reportSeriesUid
            reportSeriesVisualType = seriesToAddFilter.reportSeriesVisualType
            reportSeriesDataSet = seriesToAddFilter.reportSeriesDataSet
            reportSeriesSubGroup = seriesToAddFilter.reportSeriesSubGroup
            reportSeriesName = seriesToAddFilter.reportSeriesName
            reportSeriesFilters = newFilterList.toList()
        })

        entityVal?.reportSeriesWithFiltersList = newSeriesList.toList()
        view.entity = entityVal
    }

    fun handleRemoveFilter(filter: ReportFilter) {
        val entityVal = entity
        val newSeriesList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        val seriesToRemoveFilter = newSeriesList.find { it.reportSeriesUid == filter.reportFilterSeriesUid } ?: return

        newSeriesList.remove(seriesToRemoveFilter)
        val newFilterList = seriesToRemoveFilter.reportSeriesFilters.toMutableList()
        newFilterList.remove(filter)

        newSeriesList.add(ReportSeries().apply {
            reportSeriesUid = seriesToRemoveFilter.reportSeriesUid
            reportSeriesVisualType = seriesToRemoveFilter.reportSeriesVisualType
            reportSeriesDataSet = seriesToRemoveFilter.reportSeriesDataSet
            reportSeriesSubGroup = seriesToRemoveFilter.reportSeriesSubGroup
            reportSeriesName = seriesToRemoveFilter.reportSeriesName
            reportSeriesFilters = newFilterList.toList()
        })

        entityVal?.reportSeriesWithFiltersList = newSeriesList.toList()
        view.entity = entityVal
    }


    override fun handleClickSave(entity: ReportWithSeriesWithFilters) {
        if (entity.reportTitle.isNullOrEmpty()) {
            view.titleErrorText = systemImpl.getString(MessageID.field_required_prompt, context)
            return
        } else {
            view.titleErrorText = null
        }

        entity.reportSeries = safeStringify(di, ReportSeries.serializer().list, entity.reportSeriesWithFiltersList)

        GlobalScope.launch(doorMainDispatcher()) {

            if (entity.reportUid != 0L) {

                repo.reportDao.updateAsync(entity)

                withContext(doorMainDispatcher()) {
                    view.finishWithResult(listOf(entity))
                }

            } else {
                systemImpl.go(ReportDetailView.VIEW_NAME,
                        mapOf(ARG_ENTITY_JSON to
                                safeStringify(di, ReportWithSeriesWithFilters.serializer(), entity)),
                        context)
            }

        }
    }

    fun handleXAxisSelected(selectedOption: MessageIdOption) {
        if (selectedOption.code == Report.DAY || selectedOption.code == Report.MONTH || selectedOption.code == Report.WEEK) {
            view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }
                    .filter { it.code == Report.GENDER ||
                            it.code == Report.CONTENT_ENTRY ||
                            it.code == Report.CLASS }
        } else {
            view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }
        }
    }

}