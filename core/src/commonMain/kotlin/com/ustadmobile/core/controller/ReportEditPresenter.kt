package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportFilterWithDisplayDetails
import com.ustadmobile.lib.db.entities.ReportWithFilters
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ReportEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ReportEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          di: DI)
    : UstadEditPresenter<ReportEditView, ReportWithFilters>(context, arguments, view, lifecycleOwner, di) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class ChartOptions(val optionVal: Int, val messageId: Int) {
        BARCHART(Report.BAR_CHART,
                MessageID.bar_chart),
        LINEGRAPH(Report.LINE_GRAPH,
                MessageID.line_graph)
    }

    class ChartTypeMessageIdOption(day: ChartOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class YAxisOptions(val optionVal: Int, val messageId: Int) {
        SCORE(Report.SCORE,
                MessageID.score),
        DURATION(Report.DURATION,
                MessageID.total_duration),
        AVG_DURATION(Report.AVG_DURATION,
                MessageID.average_duration),
        COUNT_ACTIVITIES(Report.COUNT_ACTIVITIES,
                MessageID.count_activity)
    }

    class YAxisMessageIdOption(day: YAxisOptions, context: Any)
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
                MessageID.gender_literal)
    }

    class XAxisMessageIdOption(day: XAxisOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    class GroupByMessageIdOption(day: XAxisOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    val personOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper(ReportFilterWithDisplayDetails::reportFilterUid,
            "state_Person_list", ReportFilterWithDisplayDetails.serializer().list,
            ReportFilterWithDisplayDetails.serializer().list, this) { reportFilterUid = it }

    fun handleAddOrEditPerson(person: ReportFilterWithDisplayDetails) {
        personOneToManyJoinEditHelper.onEditResult(person)
    }

    fun handleRemovePerson(person: ReportFilterWithDisplayDetails) {
        personOneToManyJoinEditHelper.onDeactivateEntity(person)
    }

    val verbDisplaynOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper(ReportFilterWithDisplayDetails::reportFilterUid,
            "state_VerbDisplay_list", ReportFilterWithDisplayDetails.serializer().list,
            ReportFilterWithDisplayDetails.serializer().list, this) { reportFilterUid = it }

    fun handleAddOrEditVerbDisplay(verbDisplay: ReportFilterWithDisplayDetails) {
        verbDisplaynOneToManyJoinEditHelper.onEditResult(verbDisplay)
    }

    fun handleRemoveVerb(verbDisplay: ReportFilterWithDisplayDetails) {
        verbDisplaynOneToManyJoinEditHelper.onDeactivateEntity(verbDisplay)
    }


    val contentOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper(ReportFilterWithDisplayDetails::reportFilterUid,
            "state_Content_list", ReportFilterWithDisplayDetails.serializer().list,
            ReportFilterWithDisplayDetails.serializer().list, this) { reportFilterUid = it }

    fun handleAddOrEditContent(content: ReportFilterWithDisplayDetails) {
        contentOneToManyJoinEditHelper.onEditResult(content)
    }

    fun handleRemoveContent(content: ReportFilterWithDisplayDetails) {
        contentOneToManyJoinEditHelper.onDeactivateEntity(content)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.personFilterList = personOneToManyJoinEditHelper.liveList
        view.verbFilterList = verbDisplaynOneToManyJoinEditHelper.liveList
        view.contentFilterList = contentOneToManyJoinEditHelper.liveList
        view.chartOptions = ChartOptions.values().map { ChartTypeMessageIdOption(it, context) }
        view.yAxisOptions = YAxisOptions.values().map { YAxisMessageIdOption(it, context) }
        view.xAxisOptions = XAxisOptions.values().map { XAxisMessageIdOption(it, context) }
        view.groupOptions = XAxisOptions.values().map { GroupByMessageIdOption(it, context) }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val report = withTimeoutOrNull(2000) {
            db.reportDao.findByUid(entityUid)
        } ?: Report()

        val reportFilterList = withTimeout(2000) {
            db.reportFilterDao.findByReportUid(report.reportUid)
        }

        val groupMap = reportFilterList.groupBy { it.entityType }
        val personList = groupMap[ReportFilter.PERSON_FILTER] ?: listOf()
        val verbList = groupMap[ReportFilter.VERB_FILTER] ?: listOf()
        val contentUidList = groupMap[ReportFilter.CONTENT_FILTER] ?: listOf()

        personOneToManyJoinEditHelper.liveList.sendValue(personList)
        verbDisplaynOneToManyJoinEditHelper.liveList.sendValue(verbList)
        contentOneToManyJoinEditHelper.liveList.sendValue(contentUidList)

        report.fromDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong - 7.days.millisecondsLong
        report.toDate = DateTime.nowLocal().localEndOfDay.utc.unixMillisLong

        handleXAxisSelected(XAxisOptions.values().map { GroupByMessageIdOption(it, context) }.find { it.code == report.xAxis } as MessageIdOption)

        return ReportWithFilters(report, reportFilterList)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ReportWithFilters
        if (entityJsonStr != null) {
            editEntity = Json.parse(ReportWithFilters.serializer(), entityJsonStr)
        } else {
            editEntity = ReportWithFilters()
        }

        handleXAxisSelected(XAxisOptions.values().map { GroupByMessageIdOption(it, context) }.find { it.code == editEntity.xAxis } as MessageIdOption)

        return editEntity
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
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

                listOf(personOneToManyJoinEditHelper, verbDisplaynOneToManyJoinEditHelper, contentOneToManyJoinEditHelper).forEach {
                    repo.reportFilterDao.insertListAsync(it.entitiesToInsert)
                    repo.reportFilterDao.updateAsyncList(it.entitiesToUpdate)
                    repo.reportFilterDao.deactivateByUids(it.primaryKeysToDeactivate)
                }

                repo.reportDao.updateAsync(entity)

                withContext(doorMainDispatcher()) {
                    view.finishWithResult(listOf(entity))
                }

            } else {

                val personUidList = personOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()
                val verbUidList = verbDisplaynOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()
                val entryUidList = contentOneToManyJoinEditHelper.liveList.getValue()?.map { it.entityUid }
                        ?: listOf()

                val reportFilterList = mutableListOf<ReportFilter>()
                personUidList.onEach { personUid ->
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
                }

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
            view.groupOptions = XAxisOptions.values().map { GroupByMessageIdOption(it, context) }.filter { it.code == Report.GENDER || it.code == Report.CONTENT_ENTRY }
        } else {
            view.groupOptions = XAxisOptions.values().map { GroupByMessageIdOption(it, context) }
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}