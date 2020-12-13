package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ReportEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ReportEditView,
                          di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportEditView, ReportWithFilters>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class VisualTypeOptions(val optionVal: Int, val messageId: Int) {
        BARCHART(Report.BAR_CHART,
                MessageID.bar_chart),
        LINEGRAPH(Report.LINE_GRAPH,
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
                MessageID.xapi_week),
        NUMBER_SESSIONS(ReportSeries.NUMBER_SESSIONS,
                MessageID.xapi_month),
        ACTIVITIES_RECORDED(ReportSeries.ACTIVITIES_RECORDED,
                MessageID.xapi_content_entry),
        AVERAGE_SESSION_PER_CONTENT(ReportSeries.AVERAGE_SESSION_PER_CONTENT,
                MessageID.gender_literal),
        PERCENT_STUDENTS_COMPLETED(ReportSeries.PERCENT_STUDENTS_COMPLETED,
                MessageID.clazz),
        NUMBER_STUDENTS_COMPLETED(ReportSeries.NUMBER_STUDENTS_COMPLETED,
                MessageID.clazz)
    }

    class DataSetMessageIdOption(data: DataSetOptions, context: Any)
        : MessageIdOption(data.messageId, context, data.optionVal)


    val filterOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper(ReportFilterWithDisplayDetails::reportFilterUid,
            "state_Person_list", ReportFilterWithDisplayDetails.serializer().list,
            ReportFilterWithDisplayDetails.serializer().list, this,
            ReportFilterWithDisplayDetails::class) { reportFilterUid = it }

    fun handleAddOrEditFilter(filter: ReportFilterWithDisplayDetails) {
        filterOneToManyJoinEditHelper.onEditResult(filter)
    }

    fun handleRemoveFilter(filter: ReportFilterWithDisplayDetails) {
        filterOneToManyJoinEditHelper.onDeactivateEntity(filter)
    }



    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.visualTypeOptions = VisualTypeOptions.values().map { VisualTypeMessageIdOption(it, context) }
        view.xAxisOptions = XAxisOptions.values().map { XAxisMessageIdOption(it, context) }
        view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val report = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.reportDao?.findByUid(entityUid)
        } ?: Report()

        val reportFilterList = withTimeout(2000) {
            db.reportFilterDao.findByReportUid(report.reportUid)
        }

        val groupMap = reportFilterList.groupBy { it.entityType }
        val personList = groupMap[ReportFilter.PERSON_FILTER] ?: listOf()
        val verbList = groupMap[ReportFilter.VERB_FILTER] ?: listOf()
        val contentUidList = groupMap[ReportFilter.CONTENT_FILTER] ?: listOf()

        report.fromDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong - 7.days.millisecondsLong
        report.toDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong

        handleXAxisSelected(XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }.find { it.code == report.xAxis } as MessageIdOption)

        return ReportWithFilters(report, reportFilterList)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ReportWithFilters
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ReportWithFilters.serializer(), entityJsonStr)
        } else {
            editEntity = ReportWithFilters()
        }

        handleXAxisSelected(XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }.find { it.code == editEntity.xAxis } as MessageIdOption)

        return editEntity
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    fun handleRemoveSeries(){

    }

    fun handleRemoveFilter(){

    }

    fun handleClickAddSeries(){
        // add series object to report
    }

    override fun handleClickSave(entity: ReportWithFilters) {
        if (entity.reportTitle.isNullOrEmpty()) {
            view.titleErrorText = systemImpl.getString(MessageID.field_required_prompt, context)
            return
        } else {
            view.titleErrorText = null
        }


        GlobalScope.launch(doorMainDispatcher()) {

            if (entity.reportUid != 0L) {
                repo.reportDao.updateAsync(entity)

              /*  listOf(personOneToManyJoinEditHelper, verbDisplaynOneToManyJoinEditHelper, contentOneToManyJoinEditHelper).forEach {
                    repo.reportFilterDao.insertListAsync(it.entitiesToInsert)
                    repo.reportFilterDao.updateAsyncList(it.entitiesToUpdate)
                    repo.reportFilterDao.deactivateByUids(it.primaryKeysToDeactivate)
                }*/

                repo.reportDao.updateAsync(entity)

                withContext(doorMainDispatcher()) {
                    view.finishWithResult(listOf(entity))
                }

            } else {

              /*  val personUidList = personOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()
                val verbUidList = verbDisplaynOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()
                val entryUidList = contentOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()*/

                val reportFilterList = mutableListOf<ReportFilter>()

              /*  personUidList.onEach { personUid ->
                    reportFilterList.add(ReportFilter().apply {
                        entityUid = personUid
                        entityType = ReportFilter.PERSON_FILTER
                    })
                }

                verbUidList.onEach { verbUid ->
                    reportFilterList.add(ReportFilter().apply {
                        entityUid = verbUid
                        entityType = ReportFilter.VERB_FILTER
                    })
                }

                entryUidList.onEach { entryUid ->
                    reportFilterList.add(ReportFilter().apply {
                        entityUid = entryUid
                        entityType = ReportFilter.CONTENT_FILTER
                    })
                }*/

                entity.reportFilterList = reportFilterList.toList()

                systemImpl.go(ReportDetailView.VIEW_NAME,
                        mapOf(ARG_ENTITY_JSON to
                                Json.stringify(ReportWithFilters.serializer(), entity)),
                        context)

            }


        }
    }

    fun handleXAxisSelected(selectedOption: MessageIdOption) {
        if (selectedOption.code == Report.DAY || selectedOption.code == Report.MONTH || selectedOption.code == Report.WEEK) {
            view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }.filter { it.code == Report.GENDER || it.code == Report.CONTENT_ENTRY }
        } else {
            view.subGroupOptions = XAxisOptions.values().map { SubGroupByMessageIdOption(it, context) }
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}