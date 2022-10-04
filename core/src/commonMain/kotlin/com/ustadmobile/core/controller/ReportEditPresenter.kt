package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.toDateRangeMoment
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Moment.Companion.MONTHS_REL_UNIT
import com.ustadmobile.lib.db.entities.Moment.Companion.TYPE_FLAG_RELATIVE
import com.ustadmobile.lib.db.entities.Moment.Companion.WEEKS_REL_UNIT
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import kotlin.math.max


class ReportEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ReportEditView,
                          di: DI,
                          lifecycleOwner: LifecycleOwner)
    : UstadEditPresenter<ReportEditView, ReportWithSeriesWithFilters>(context, arguments, view, di, lifecycleOwner) {

    private val seriesCounter = atomic(1)

    private val filterCounter = atomic(1)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class VisualTypeOptions(val optionVal: Int, val messageId: Int) {
        BAR_CHART(ReportSeries.BAR_CHART,
                MessageID.bar_chart),
        LINE_GRAPH(ReportSeries.LINE_GRAPH,
                MessageID.line_chart)
    }

    class VisualTypeMessageIdOption(day: VisualTypeOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

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
                MessageID.clazz),
        ENROLMENT_OUTCOME(Report.ENROLMENT_OUTCOME,
                MessageID.class_enrolment_outcome),
        ENROLMENT_LEAVING(Report.ENROLMENT_LEAVING_REASON,
                MessageID.class_enrolment_leaving)
    }

    class XAxisMessageIdOption(day: XAxisOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

    enum class DateRangeOptions(val code: Int, val messageId: Int,
                                var dateRange: DateRangeMoment?) {
        EVERYTHING(Report.EVERYTHING, MessageID.time_range_all,
                DateRangeMoment(Moment(), Moment())),
        LAST_WEEK(Report.LAST_WEEK_DATE,
                MessageID.last_week_date_range,
                DateRangeMoment(
                        Moment().apply {
                            typeFlag = TYPE_FLAG_RELATIVE
                            relUnit = WEEKS_REL_UNIT
                            relOffSet = -1
                        }, Moment().apply {
                    typeFlag = TYPE_FLAG_RELATIVE
                })),
        LAST_TWO_WEEKS(Report.LAST_TWO_WEEKS_DATE,
                MessageID.last_two_week_date_range,
                DateRangeMoment(
                        Moment().apply {
                            typeFlag = TYPE_FLAG_RELATIVE
                            relUnit = WEEKS_REL_UNIT
                            relOffSet = -2
                        }, Moment().apply {
                    typeFlag = TYPE_FLAG_RELATIVE
                })),
        LAST_MONTH(Report.LAST_MONTH_DATE,
                MessageID.last_month_date_range,
                DateRangeMoment(
                        Moment().apply {
                            typeFlag = TYPE_FLAG_RELATIVE
                            relUnit = MONTHS_REL_UNIT
                            relOffSet = -1
                        }, Moment().apply {
                    typeFlag = TYPE_FLAG_RELATIVE
                })),
        LAST_THREE_MONTHS(Report.LAST_THREE_MONTHS_DATE,
                MessageID.last_three_months_date_range,
                DateRangeMoment(
                        Moment().apply {
                            typeFlag = TYPE_FLAG_RELATIVE
                            relUnit = MONTHS_REL_UNIT
                            relOffSet = -3
                        }, Moment().apply {
                    typeFlag = TYPE_FLAG_RELATIVE
                })),
        CUSTOM_RANGE(Report.CUSTOM_RANGE,
                MessageID.selected_custom_range, null),
        NEW_CUSTOM_RANGE(Report.NEW_CUSTOM_RANGE_DATE,
                MessageID.new_custom_date_range,
                DateRangeMoment(
                        Moment().apply {
                            typeFlag = TYPE_FLAG_RELATIVE
                            relUnit = WEEKS_REL_UNIT
                        }, Moment().apply {
                    typeFlag = TYPE_FLAG_RELATIVE
                })),
    }

    enum class SubGroupOptions(val optionVal: Int, val messageId: Int) {
        NONE(ReportSeries.NONE,
                MessageID.None),
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
                MessageID.clazz),
        ENROLMENT_OUTCOME(Report.ENROLMENT_OUTCOME,
                MessageID.class_enrolment_outcome),
        ENROLMENT_LEAVING(Report.ENROLMENT_LEAVING_REASON,
                MessageID.class_enrolment_leaving)
    }

    class SubGroupByMessageIdOption(day: SubGroupOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

    enum class YAxisOptions(val optionVal: Int, val messageId: Int) {
        TOTAL_DURATION(ReportSeries.TOTAL_DURATION,
                MessageID.content_total_duration),
        AVERAGE_DURATION(ReportSeries.AVERAGE_DURATION,
                MessageID.content_average_duration),
        NUMBER_SESSIONS(ReportSeries.NUMBER_SESSIONS,
                MessageID.count_session),
        INTERACTIONS_RECORDED(ReportSeries.INTERACTIONS_RECORDED,
                MessageID.interaction_recorded),
        NUMBER_ACTIVE_USERS(ReportSeries.NUMBER_ACTIVE_USERS,
                MessageID.number_active_users),
        AVERAGE_USAGE_TIME_PER_USER(ReportSeries.AVERAGE_USAGE_TIME_PER_USER,
                MessageID.average_usage_time_per_user),
        NUMBER_STUDENTS_COMPLETED(ReportSeries.NUMBER_OF_STUDENTS_COMPLETED_CONTENT,
                MessageID.number_students_completed),
        PERCENT_STUDENTS_COMPLETED(ReportSeries.PERCENT_OF_STUDENTS_COMPLETED_CONTENT,
                MessageID.percent_students_completed),
        TOTAL_ATTENDANCE(ReportSeries.TOTAL_ATTENDANCE,
                MessageID.total_attendances),
        TOTAL_ABSENCES(ReportSeries.TOTAL_ABSENCES,
                MessageID.total_absences),
        TOTAL_LATES(ReportSeries.TOTAL_LATES,
                MessageID.total_lates),
        PERCENT_STUDENTS_ATTENDED(ReportSeries.PERCENTAGE_STUDENTS_ATTENDED,
                MessageID.percent_students_attended),
        PERCENT_STUDENTS_ATTENDED_OR_LATE(ReportSeries.PERCENTAGE_STUDENTS_ATTENDED_OR_LATE,
                MessageID.percent_students_attended_or_late),
        TOTAL_CLASSES(ReportSeries.TOTAL_CLASSES,
                MessageID.total_number_of_classes),
        UNIQUE_STUDENTS_ATTENDING(ReportSeries.NUMBER_UNIQUE_STUDENTS_ATTENDING,
                MessageID.number_unique_students_attending)
    }

    class YAxisMessageIdOption(data: YAxisOptions, context: Any, di: DI)
        : MessageIdOption(data.messageId, context, data.optionVal, di = di)


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.visualTypeOptions = VisualTypeOptions.values().map { VisualTypeMessageIdOption(it, context, di) }
        view.xAxisOptions = XAxisOptions.values().map { XAxisMessageIdOption(it, context, di) }
        view.yAxisOptions = YAxisOptions.values().map { YAxisMessageIdOption(it, context, di) }
        view.dateRangeOptions = DateRangeOptions.values().filter { it.dateRange != null }
                .map {  ObjectMessageIdOption(it.messageId, context, it.code, it.dateRange, di) }
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(
            RESULT_REPORT_FILTER_KEY,
            ListSerializer(ReportFilter.serializer()), ReportFilter::class) {
            val newFilter = it.firstOrNull() ?: return@observeSavedStateResult
            val entityVal = entity
            val newSeriesList = entityVal?.reportSeriesWithFiltersList?.toMutableList()
                ?: mutableListOf()
            val seriesToAddFilter = newSeriesList.find { it.reportSeriesUid == newFilter.reportFilterSeriesUid }
                ?: return@observeSavedStateResult

            newSeriesList.remove(seriesToAddFilter)
            val newFilterList = seriesToAddFilter.reportSeriesFilters?.toMutableList()
                ?: mutableListOf()

            // new filter
            if (newFilter.reportFilterUid == 0) {
                newFilter.reportFilterUid = filterCounter.incrementAndGet()
                newFilterList.add(newFilter)
            } else {
                val indexOfFilter = newFilterList.indexOfFirst { it.reportFilterUid == newFilter.reportFilterUid }
                newFilterList[indexOfFilter] = newFilter
            }

            newSeriesList.add(ReportSeries().apply {
                reportSeriesUid = seriesToAddFilter.reportSeriesUid
                reportSeriesVisualType = seriesToAddFilter.reportSeriesVisualType
                reportSeriesYAxis = seriesToAddFilter.reportSeriesYAxis
                reportSeriesSubGroup = seriesToAddFilter.reportSeriesSubGroup
                reportSeriesName = seriesToAddFilter.reportSeriesName
                reportSeriesFilters = newFilterList.toList()
            })

            entityVal?.reportSeriesWithFiltersList = newSeriesList.toList()
            view.entity = entityVal
            requireSavedStateHandle()[RESULT_REPORT_FILTER_KEY] = null

        }


        observeSavedStateResult(
            RESULT_DATE_RANGE_KEY,
            ListSerializer(DateRangeMoment.serializer()), DateRangeMoment::class) {
            val dateRangeMoment = it.firstOrNull() ?: return@observeSavedStateResult
            handleAddCustomRange(dateRangeMoment)
            requireSavedStateHandle()[RESULT_DATE_RANGE_KEY] = null
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithSeriesWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val report = db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.reportDao?.findByUid(entityUid)
        } ?: Report()

        handleXAxisSelected(XAxisOptions.values().map { XAxisMessageIdOption(it, context, di) }.find { it.code == report.xAxis } as MessageIdOption)
        if(report.reportDateRangeSelection == Report.CUSTOM_RANGE){
            handleAddCustomRange(report.toDateRangeMoment())
        }

        val reportSeries = report.reportSeries
        var reportSeriesList = listOf<ReportSeries>()
        if (!reportSeries.isNullOrBlank()) {
            reportSeriesList = safeParseList(di,
                ListSerializer(ReportSeries.serializer()), ReportSeries::class, reportSeries)
            // set the series counter with an existing series
            val maxSeries = reportSeriesList.maxByOrNull { it.reportSeriesUid }
            var currentMaxFilter = 0
            reportSeriesList.forEach {
                val maxFilter = it.reportSeriesFilters?.maxByOrNull { it.reportFilterUid }?.reportFilterUid
                        ?: 0
                currentMaxFilter = max(currentMaxFilter, maxFilter)
            }
            val nextSeries = maxSeries?.reportSeriesUid ?: 1
            seriesCounter.value = nextSeries
            filterCounter.value = currentMaxFilter + 1
        } else {
            reportSeriesList = listOf(ReportSeries().apply {
                val id = seriesCounter.getAndIncrement()
                reportSeriesName = "Series $id"
                reportSeriesUid = id
            })
        }

        return ReportWithSeriesWithFilters(report, reportSeriesList)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithSeriesWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity = if (entityJsonStr != null) {
            safeParse(di, ReportWithSeriesWithFilters.serializer(), entityJsonStr)
        } else {
            ReportWithSeriesWithFilters()
        }

        handleXAxisSelected(XAxisOptions.values().map { XAxisMessageIdOption(it, context, di) }.find { it.code == editEntity.xAxis } as MessageIdOption)
        if(editEntity.reportDateRangeSelection == Report.CUSTOM_RANGE){
            handleAddCustomRange(editEntity.toDateRangeMoment())
        }

        val reportSeries = editEntity.reportSeries
        val reportSeriesList: List<ReportSeries>
        if (!reportSeries.isNullOrBlank()) {

            reportSeriesList = safeParseList(di, ListSerializer(ReportSeries.serializer()),
                ReportSeries::class, reportSeries)
            // set the series counter with an existing series
            val max = reportSeriesList.maxByOrNull { it.reportSeriesUid }
            val nextSeries = ((max?.reportSeriesUid ?: 0) + 1)
            seriesCounter.value = nextSeries
            var currentMaxFilter = 0
            reportSeriesList.forEach {
                val maxFilter = it.reportSeriesFilters?.maxByOrNull { it.reportFilterUid }?.reportFilterUid
                        ?: 0
                currentMaxFilter = max(currentMaxFilter, maxFilter)
            }
            filterCounter.value = currentMaxFilter.toInt() + 1
            editEntity.reportSeriesWithFiltersList = reportSeriesList

        } else if (editEntity.reportSeriesWithFiltersList == null || editEntity.reportSeriesWithFiltersList?.isNullOrEmpty() == true) {
            reportSeriesList = listOf(ReportSeries().apply {
                val id = seriesCounter.getAndIncrement()
                reportSeriesName = "Series $id"
                reportSeriesUid = id
            })
            editEntity.reportSeriesWithFiltersList = reportSeriesList
        }


        return editEntity
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity ?: return
        entityVal.reportSeries = safeStringify(di, ListSerializer(ReportSeries.serializer()),
            entityVal.reportSeriesWithFiltersList ?: listOf())
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, ReportWithSeriesWithFilters.serializer(),
            entityVal)
    }

    fun handleAddCustomRange(dateRangeMoment: DateRangeMoment) {
        view.dateRangeOptions = DateRangeOptions.values().map {
            ObjectMessageIdOption(it.messageId, context, it.code, it.dateRange ?: dateRangeMoment,
                    di, if(it.dateRange != null) null else dateRangeMoment.toDisplayString())
        }
        view.selectedDateRangeMoment = dateRangeMoment
        val entityVal = entity ?: return
        entityVal.reportDateRangeSelection = Report.CUSTOM_RANGE
        view.entity = entityVal
    }

    fun handleRemoveSeries(series: ReportSeries) {
        val entityVal = entity
        val newList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        newList.remove(series)
        entityVal?.reportSeriesWithFiltersList = newList.toList()
        view.entity = entityVal
    }

    fun handleClickAddSeries() {
        val series = ReportSeries().apply {
            val id = seriesCounter.getAndIncrement()
            reportSeriesName = "Series $id"
            reportSeriesUid = id
        }
        val entityVal = entity
        val newList = entityVal?.reportSeriesWithFiltersList?.toMutableList() ?: mutableListOf()
        newList.add(series)
        entityVal?.reportSeriesWithFiltersList = newList.toList()
        view.entity = entityVal
    }


    fun handleRemoveFilter(filter: ReportFilter) {
        val entityVal = entity
        val newSeriesList = entityVal?.reportSeriesWithFiltersList?.toMutableList()
                ?: mutableListOf()
        val seriesToRemoveFilter = newSeriesList.find { it.reportSeriesUid == filter.reportFilterSeriesUid }
                ?: return

        newSeriesList.remove(seriesToRemoveFilter)
        val newFilterList = seriesToRemoveFilter.reportSeriesFilters?.toMutableList()
        newFilterList?.remove(filter)

        newSeriesList.add(ReportSeries().apply {
            reportSeriesUid = seriesToRemoveFilter.reportSeriesUid
            reportSeriesVisualType = seriesToRemoveFilter.reportSeriesVisualType
            reportSeriesYAxis = seriesToRemoveFilter.reportSeriesYAxis
            reportSeriesSubGroup = seriesToRemoveFilter.reportSeriesSubGroup
            reportSeriesName = seriesToRemoveFilter.reportSeriesName
            reportSeriesFilters = newFilterList?.toList()
        })

        entityVal?.reportSeriesWithFiltersList = newSeriesList.toList()
        view.entity = entityVal
    }


    fun handleOnFilterClicked(filter: ReportFilter) {
        navigateForResult(
            NavigateForResultOptions(
                this, filter,
                ReportFilterEditView.VIEW_NAME,
                ReportFilter::class,
                ReportFilter.serializer(),
                RESULT_REPORT_FILTER_KEY
            )
        )
    }


    fun handleDateRangeChange() {
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                XapiPackageContentView.VIEW_NAME,
                DateRangeMoment::class,
                DateRangeMoment.serializer(),
                RESULT_DATE_RANGE_KEY
            )
        )
    }


    override fun handleClickSave(entity: ReportWithSeriesWithFilters) {
        if (entity.reportTitle.isNullOrEmpty()) {
            view.titleErrorText = systemImpl.getString(MessageID.field_required_prompt, context)
            return
        } else {
            view.titleErrorText = null
        }

        entity.reportSeries = safeStringify(di, ListSerializer(ReportSeries.serializer()),
            entity.reportSeriesWithFiltersList ?: listOf())

        GlobalScope.launch(doorMainDispatcher()) {

            if (entity.reportUid != 0L) {

                repo.reportDao.updateAsync(entity)

                withContext(doorMainDispatcher()) {
                    finishWithResult(safeStringify(di,
                        ListSerializer(ReportWithSeriesWithFilters.serializer()),
                        listOf(entity)))
                }

            } else {
                systemImpl.go(ReportDetailView.VIEW_NAME,
                        mapOf(ARG_ENTITY_JSON to
                                safeStringify(di, ReportWithSeriesWithFilters.serializer(), entity)),
                        context)
            }

        }
    }

    fun handleXAxisSelected(selectedOption: IdOption) {
        if (selectedOption.optionId == Report.DAY || selectedOption.optionId == Report.MONTH || selectedOption.optionId == Report.WEEK) {
            view.subGroupOptions = SubGroupOptions.values().map { SubGroupByMessageIdOption(it, context, di) }
                    .filter {
                        it.code == Report.GENDER ||
                                it.code == Report.CONTENT_ENTRY ||
                                it.code == Report.CLASS || it.code == ReportSeries.NONE ||
                                it.code == Report.ENROLMENT_LEAVING_REASON ||
                                it.code == Report.ENROLMENT_OUTCOME
                    }
        } else if (selectedOption.optionId == Report.CLASS ||
                selectedOption.optionId == Report.CONTENT_ENTRY ||
                selectedOption.optionId == Report.GENDER ||
                selectedOption.optionId == Report.ENROLMENT_LEAVING_REASON ||
                selectedOption.optionId == Report.ENROLMENT_OUTCOME) {
            view.subGroupOptions = SubGroupOptions.values().map { SubGroupByMessageIdOption(it, context, di) }
        }
    }

    fun handleDateRangeSelected(selectedOption: IdOption) {
        if(selectedOption.optionId == DateRangeOptions.NEW_CUSTOM_RANGE.code){
            return
        }
        val dateRangeFound = view.dateRangeOptions?.find { it.code == selectedOption.optionId } ?: return
        view.selectedDateRangeMoment = dateRangeFound.obj
    }

    companion object {
        const val RESULT_REPORT_FILTER_KEY = "Filters"
        const val RESULT_DATE_RANGE_KEY = "DateRanges"
    }

}