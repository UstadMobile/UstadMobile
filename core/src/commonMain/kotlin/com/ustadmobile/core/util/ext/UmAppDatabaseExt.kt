package com.ustadmobile.core.util.ext

import app.cash.paging.PagingSource
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.graph.LabelValueFormatter
import com.ustadmobile.core.util.graph.MessageIdFormatter
import com.ustadmobile.core.util.graph.TimeFormatter
import com.ustadmobile.core.util.graph.UidAndLabelFormatter
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_NO_DELETE
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.core.db.dao.getResults
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.AlreadyEnroledInClassException
import com.ustadmobile.door.DoorDatabaseRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Insert a new class and
 * @param termMap course terminology map
 */
suspend fun UmAppDatabase.createNewClazzAndGroups(
    clazz: Clazz,
    impl: UstadMobileSystemImpl,
    termMap: Map<String, String>,
) {
    clazz.clazzTeachersPersonGroupUid = personGroupDao.insertAsync(
            PersonGroup("${clazz.clazzName} - " + termMap[TerminologyKeys.TEACHER_KEY]))

    clazz.clazzStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            termMap[TerminologyKeys.STUDENTS_KEY]))

    clazz.clazzPendingStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MR.strings.pending_requests)))

    clazz.clazzParentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MR.strings.parent)))

    clazz.takeIf { it.clazzCode == null }?.clazzCode = randomString(Clazz.CLAZZ_CODE_DEFAULT_LENGTH)

    val generatedUid = clazzDao.insertAsync(clazz)
    if(clazz.clazzUid != 0L)
        clazz.clazzUid = generatedUid

    //Make the default ScopedGrants
    scopedGrantDao.insertListAsync(listOf(ScopedGrant().apply {
        sgFlags = ScopedGrant.FLAG_TEACHER_GROUP.or(FLAG_NO_DELETE)
        sgPermissions = Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT
        sgGroupUid = clazz.clazzTeachersPersonGroupUid
        sgEntityUid = clazz.clazzUid
        sgTableId = Clazz.TABLE_ID
    }, ScopedGrant().apply {
        sgFlags = ScopedGrant.FLAG_STUDENT_GROUP.or(FLAG_NO_DELETE)
        sgPermissions = Role.ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT
        sgGroupUid = clazz.clazzStudentsPersonGroupUid
        sgEntityUid = clazz.clazzUid
        sgTableId = Clazz.TABLE_ID
    }, ScopedGrant().apply {
        sgFlags = (ScopedGrant.FLAG_PARENT_GROUP or FLAG_NO_DELETE)
        sgPermissions = Role.ROLE_CLAZZ_PARENT_PERMISSION_DEFAULT
        sgGroupUid = clazz.clazzParentsPersonGroupUid
        sgEntityUid = clazz.clazzUid
        sgTableId = Clazz.TABLE_ID
    }))


}


/**
 * Enrol the given person into the given class. The effective date of joining is midnight as per
 * the timezone of the class (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 *
 * @throws IllegalStateException when the person is already in the class
 */
@Throws(AlreadyEnroledInClassException::class)
suspend fun UmAppDatabase.enrolPersonIntoClazzAtLocalTimezone(
    personToEnrol: Person,
    clazzUid: Long,
    role: Int,
    clazzWithSchool: ClazzWithSchool? = null
): ClazzEnrolment {
    val clazzWithSchoolVal = clazzWithSchool ?: clazzDao.getClazzWithSchool(clazzUid)
        ?: throw IllegalArgumentException("Class does not exist")

    val existingEnrolments = clazzEnrolmentDao.getAllClazzEnrolledAtTimeAsync(clazzUid,
        systemTimeInMillis(), 0, personToEnrol.personUid)

    if(existingEnrolments.isNotEmpty()) {
        throw AlreadyEnroledInClassException()
    }

    val clazzTimeZone = clazzWithSchoolVal.effectiveTimeZone()
    val joinTime = Clock.System.now().toLocalMidnight(clazzTimeZone).toEpochMilliseconds()

    val clazzEnrolment = ClazzEnrolment().apply {
        clazzEnrolmentPersonUid = personToEnrol.personUid
        clazzEnrolmentClazzUid = clazzUid
        clazzEnrolmentRole = role
        clazzEnrolmentActive = true
        clazzEnrolmentDateJoined = joinTime
    }

    return processEnrolmentIntoClass(clazzEnrolment, clazzWithSchoolVal)
}

/**
 * Process the given enrolment. This will insert the ClazzEnrolment itself and will add the person
 * being enroled into the PersonGroup according to their role.
 */
suspend fun UmAppDatabase.processEnrolmentIntoClass(
    enrolment: ClazzEnrolment,
    clazzWithSchool: ClazzWithSchool? = null
) : ClazzEnrolment {

    val clazzWithSchoolVal = clazzWithSchool ?: clazzDao.getClazzWithSchool(
        enrolment.clazzEnrolmentClazzUid)
        ?: throw IllegalArgumentException("processEnrolmentIntoClass: Class does not exist")
    val clazzTimeZone = clazzWithSchoolVal.effectiveTimeZone()

    enrolment.clazzEnrolmentDateJoined = Instant.fromEpochMilliseconds(enrolment.clazzEnrolmentDateJoined)
        .toLocalMidnight(clazzTimeZone).toEpochMilliseconds()

    if(enrolment.clazzEnrolmentDateLeft != Long.MAX_VALUE){
        enrolment.clazzEnrolmentDateLeft = Instant.fromEpochMilliseconds(enrolment.clazzEnrolmentDateLeft)
            .toLocalEndOfDay(clazzTimeZone).toEpochMilliseconds()
    }

    enrolment.clazzEnrolmentUid = clazzEnrolmentDao.insertAsync(enrolment)


    val personGroupUid = when(enrolment.clazzEnrolmentRole) {
        ClazzEnrolment.ROLE_TEACHER -> clazzWithSchoolVal.clazzTeachersPersonGroupUid
        ClazzEnrolment.ROLE_STUDENT -> clazzWithSchoolVal.clazzStudentsPersonGroupUid
        ClazzEnrolment.ROLE_PARENT -> clazzWithSchoolVal.clazzParentsPersonGroupUid
        ClazzEnrolment.ROLE_STUDENT_PENDING -> clazzWithSchoolVal.clazzPendingStudentsPersonGroupUid
        else -> -1
    }

    if(personGroupUid != -1L) {
        val existingGroupMemberships = personGroupMemberDao.checkPersonBelongsToGroup(
            personGroupUid, enrolment.clazzEnrolmentPersonUid)
        personGroupMemberDao.takeIf { existingGroupMemberships.isEmpty() }?.insertAsync(
            PersonGroupMember().also {
                it.groupMemberPersonUid = enrolment.clazzEnrolmentPersonUid
                it.groupMemberGroupUid = personGroupUid
            })
    }

    val parentsToEnrol = if(enrolment.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT) {
        onRepoWithFallbackToDb(2500) {
            it.personParentJoinDao.findByMinorPersonUidWhereParentNotEnrolledInClazz(
                enrolment.clazzEnrolmentPersonUid, enrolment.clazzEnrolmentClazzUid)
        }
    }else {
        listOf()
    }

    parentsToEnrol.forEach { parentJoin ->
        onRepoWithFallbackToDb(2500) {
            it.personDao.findByUidAsync(parentJoin.parentPersonUid)
        }?.also { parentPerson ->
            enrolPersonIntoClazzAtLocalTimezone(parentPerson, enrolment.clazzEnrolmentClazzUid,
                ClazzEnrolment.ROLE_PARENT, clazzWithSchoolVal)
        }

    }

    return enrolment
}


/**
 * Inserts the person, sets its group and groupmember. Does not check if its an update
 */
suspend fun <T: Person> UmAppDatabase.insertPersonAndGroup(
    entity: T,
    groupFlag: Int = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
): T{

    val groupPerson = PersonGroup().apply {
        groupName = "Person individual group"
        personGroupFlag = groupFlag
    }
    //Create person's group
    groupPerson.groupUid = personGroupDao.insertAsync(groupPerson)

    //Assign to person
    entity.personGroupUid = groupPerson.groupUid
    entity.personUid = personDao.insertAsync(entity)

    //Assign person to PersonGroup ie: Create PersonGroupMember
    personGroupMemberDao.insertAsync(
            PersonGroupMember(entity.personUid, entity.personGroupUid))

    //Grant the person all permissions on their own data
    grantScopedPermission(entity, Role.ALL_PERMISSIONS, Person.TABLE_ID, entity.personUid)

    return entity
}

/**
 * Inserts the person, sets its group and groupmember. Does not check if its an update
 */
fun UmAppDatabase.insertPersonOnlyAndGroup(entity: Person): Person{

    val groupPerson = PersonGroup().apply {
        groupName = "Person individual group"
        personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
    }
    //Create person's group
    groupPerson.groupUid = personGroupDao.insert(groupPerson)

    //Assign to person
    entity.personGroupUid = groupPerson.groupUid
    entity.personUid = personDao.insert(entity)

    //Assign person to PersonGroup ie: Create PersonGroupMember
    personGroupMemberDao.insert(
            PersonGroupMember(entity.personUid, entity.personGroupUid))

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

        val reportList = statementDao.getResults(it.value.sqlStr, it.value.queryParams)
        val series = it.key

        xAxisList.addAll(reportList.mapNotNull { it.xAxis }.toSet())
        if(series.reportSeriesYAxis == ReportSeries.AVERAGE_DURATION
                || series.reportSeriesYAxis == ReportSeries.TOTAL_DURATION){
            yAxisValueFormatter = TimeFormatter()
        }

        val subGroupFormatter = when(series.reportSeriesSubGroup){
            Report.CLASS -> {
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val clazzLabelList = clazzDao.getClassNamesFromListOfIds(listOfUids)
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
                val entryLabelList = contentEntryDao.getContentEntryFromUids(listOfUids)
                        .map { it.uid to it.labelName }.toMap()
                UidAndLabelFormatter(entryLabelList)
            }
            Report.ENROLMENT_LEAVING_REASON -> {
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val reasonLabelList = leavingReasonDao.getReasonsFromUids(listOfUids)
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
            val clazzLabelList = clazzDao.getClassNamesFromListOfIds(xAxisList
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
            val entryLabelList = contentEntryDao.getContentEntryFromUids(xAxisList
                    .map { it.toLong() }).map { it.uid to it.labelName }.toMap()
            UidAndLabelFormatter(entryLabelList)
        }
        Report.ENROLMENT_OUTCOME -> {
            MessageIdFormatter(
                    OUTCOME_TO_MESSAGE_ID_MAP.mapKeys { it.key.toString() }, impl, context)
        }
        Report.ENROLMENT_LEAVING_REASON -> {
            val reasonLabelList = leavingReasonDao.getReasonsFromUids(xAxisList
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
        List<PagingSource<Int, StatementEntityWithDisplayDetails>> {

    val queries = report.generateSql(loggedInPersonUid, dbType())
    val statementDataSourceList = mutableListOf<PagingSource<Int, StatementEntityWithDisplayDetails>>()
    queries.forEach {
        statementDataSourceList.add(statementDao.getListResults(SimpleDoorQuery(it.value.sqlListStr, it.value.queryParams)))
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

suspend fun UmAppDatabase.grantScopedPermission(toGroupUid: Long, permissions: Long,
                                                scopeTableId: Int, scopeEntityUid: Long) : ScopedGrantResult{
    val sgUid = scopedGrantDao.insertAsync(ScopedGrant().apply {
        sgGroupUid = toGroupUid
        sgPermissions = permissions
        sgTableId = scopeTableId
        sgEntityUid = scopeEntityUid
    })

    return ScopedGrantResult(sgUid)
}

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
