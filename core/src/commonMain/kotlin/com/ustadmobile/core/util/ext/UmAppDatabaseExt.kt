package com.ustadmobile.core.util.ext

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.xapi.getResults
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.graph.LabelValueFormatter
import com.ustadmobile.core.util.graph.MessageIdFormatter
import com.ustadmobile.core.util.graph.TimeFormatter
import com.ustadmobile.core.util.graph.UidAndLabelFormatter
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.StatementEntityAndDisplayDetails
import com.ustadmobile.lib.db.entities.StatementReportData


/**
 * Inserts the person, sets its group and groupmember. Does not check if its an update
 */
@Deprecated("Should use AddNewPersonUseCase instead")
suspend fun <T: Person> UmAppDatabase.insertPersonAndGroup(
    entity: T,
    groupFlag: Int = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
): T{
    val groupPerson = PersonGroup().apply {
        groupName = "Person individual group"
        personGroupFlag = groupFlag
    }
    //Create person's group
    groupPerson.groupUid = personGroupDao().insertAsync(groupPerson)

    //Assign to person
    entity.personGroupUid = groupPerson.groupUid
    entity.personUid = personDao().insertAsync(entity)

    //Assign person to PersonGroup ie: Create PersonGroupMember
    personGroupMemberDao().insertAsync(
            PersonGroupMember(entity.personUid, entity.personGroupUid))

    //Grant the person all permissions on their own data

    grantScopedPermission(entity, Long.MAX_VALUE, Person.TABLE_ID, entity.personUid)

    return entity
}


suspend fun UmAppDatabase.generateChartData(
    report: ReportWithSeriesWithFilters,
    context: Any,
    impl: UstadMobileSystemImpl,
    loggedInPersonUid: Long
): ChartData{

    val queries = report.generateSql(loggedInPersonUid, dbType())
    val seriesDataList = mutableListOf<SeriesData>()

    var yAxisValueFormatter: LabelValueFormatter? = null

    val xAxisList = mutableSetOf<String>()
    queries.forEach {

        val reportList = statementDao().getResults(it.value.sqlStr, it.value.queryParams)
        val series = it.key

        xAxisList.addAll(reportList.mapNotNull { it.xAxis }.toSet())
        if(series.reportSeriesYAxis == ReportSeries.AVERAGE_DURATION
                || series.reportSeriesYAxis == ReportSeries.TOTAL_DURATION){
            yAxisValueFormatter = TimeFormatter()
        }

        val subGroupFormatter = when(series.reportSeriesSubGroup){
            Report.CLASS -> {
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val clazzLabelList = clazzDao().getClassNamesFromListOfIds(listOfUids)
                        .map { it.uid to it.labelName }.toMap()
                UidAndLabelFormatter(clazzLabelList)
            }
//must be handled when reports are brought back
//            Report.GENDER -> {
//                MessageIdFormatter(
//                        genderMap.mapKeys { it.key.toString() },
//                        impl, context)
//            }
            Report.CONTENT_ENTRY ->{
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val entryLabelList = contentEntryDao().getContentEntryFromUids(listOfUids)
                        .map { it.uid to it.labelName }.toMap()
                UidAndLabelFormatter(entryLabelList)
            }
            Report.ENROLMENT_LEAVING_REASON -> {
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val reasonLabelList = leavingReasonDao().getReasonsFromUids(listOfUids)
                        .map { it.uid to it.labelName }.toMap()
                UidAndLabelFormatter(reasonLabelList)
            }
            Report.ENROLMENT_OUTCOME -> {
                MessageIdFormatter(
                        OUTCOME_TO_MESSAGE_ID_MAP.mapKeys { it.key.toString() }, impl, context)
            }
            else ->{
                null
            }
        }

        seriesDataList.add(SeriesData(reportList, subGroupFormatter, series))
    }

    val xAxisFormatter = when(report.xAxis){
        Report.CLASS -> {
            val clazzLabelList = clazzDao().getClassNamesFromListOfIds(xAxisList
                    .map { it.toLong() }).map { it.uid to it.labelName }.toMap()
            UidAndLabelFormatter(clazzLabelList)
        }
//Must be handled when reporting is brought back
//        Report.GENDER -> {
//            MessageIdFormatter(
//                    genderMap.mapKeys { it.key.toString() },
//                    impl, context)
//        }
        Report.CONTENT_ENTRY ->{
            val entryLabelList = contentEntryDao().getContentEntryFromUids(xAxisList
                    .map { it.toLong() }).map { it.uid to it.labelName }.toMap()
            UidAndLabelFormatter(entryLabelList)
        }
        Report.ENROLMENT_OUTCOME -> {
            MessageIdFormatter(
                    OUTCOME_TO_MESSAGE_ID_MAP.mapKeys { it.key.toString() }, impl, context)
        }
        Report.ENROLMENT_LEAVING_REASON -> {
            val reasonLabelList = leavingReasonDao().getReasonsFromUids(xAxisList
                    .map { it.toLong() }).map { it.uid to it.labelName }.toMap()
                    .plus(0L to impl.getString(MR.strings.unset))
            UidAndLabelFormatter(reasonLabelList)
        }
        else ->{
            null
        }
    }

    return ChartData(seriesDataList.toList(), report, yAxisValueFormatter, xAxisFormatter)
}

fun UmAppDatabase.generateStatementList(report: ReportWithSeriesWithFilters, loggedInPersonUid: Long):
        List<PagingSource<Int, StatementEntityAndDisplayDetails>> {

    val queries = report.generateSql(loggedInPersonUid, dbType())
    val statementDataSourceList = mutableListOf<PagingSource<Int, StatementEntityAndDisplayDetails>>()
    queries.forEach {
        statementDataSourceList.add(statementDao().getListResults(SimpleDoorQuery(it.value.sqlListStr, it.value.queryParams)))
    }
    return statementDataSourceList.toList()
}


data class ChartData(val seriesData: List<SeriesData>,
                     val reportWithFilters: ReportWithSeriesWithFilters,
                     val yAxisValueFormatter: LabelValueFormatter?,
                     val xAxisValueFormatter: LabelValueFormatter?)

data class SeriesData(val dataList: List<StatementReportData>,
                      val subGroupFormatter: LabelValueFormatter?,
                      val series: ReportSeries)


/**
 * Gets the maximum number of items that can be in a query parameter of type list. This is 100 on
 * SQLite and unlimited (-1) on Postgres
 */
internal val UmAppDatabase.maxQueryParamListSize: Int
    get() = if(this.dbType() == DoorDbType.SQLITE) 99 else -1


data class ScopedGrantResult(val sgUid: Long)

@Deprecated("This has been replaced with SystemPermission and CoursePermission")
suspend fun UmAppDatabase.grantScopedPermission(toGroupUid: Long, permissions: Long,
                                                scopeTableId: Int, scopeEntityUid: Long) : ScopedGrantResult{
    val sgUid = scopedGrantDao().insertAsync(ScopedGrant().apply {
        sgGroupUid = toGroupUid
        sgPermissions = permissions
        sgTableId = scopeTableId
        sgEntityUid = scopeEntityUid
    })

    return ScopedGrantResult(sgUid)
}

@Deprecated("This has been replaced with SystemPermission and CoursePermission")
suspend fun UmAppDatabase.grantScopedPermission(toPerson: Person, permissions: Long,
                                                scopeTableId: Int, scopeEntityUid: Long): ScopedGrantResult {
    return grantScopedPermission(toPerson.personGroupUid, permissions, scopeTableId, scopeEntityUid)
}

suspend fun <R> UmAppDatabase.localFirstThenRepoIfNull(
    block: suspend (UmAppDatabase) -> R
): R {
    val localDb = (this as? DoorDatabaseRepository)?.db as? UmAppDatabase
    val localResult: R? = localDb?.let { block(it) }
    if(localResult != null)
        return localResult

    return block(this)
}

suspend fun UmAppDatabase.localFirstThenRepoIfFalse(
    block: suspend (UmAppDatabase) -> Boolean
): Boolean {
    val localDb = (this as? DoorDatabaseRepository)?.db as? UmAppDatabase
    val localResult = localDb?.let { block(it) }
    if(localResult == true)
        return localResult

    return block(this)
}
