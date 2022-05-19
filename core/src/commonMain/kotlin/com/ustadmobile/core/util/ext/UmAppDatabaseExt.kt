package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.controller.ReportFilterEditPresenter.Companion.genderMap
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.graph.LabelValueFormatter
import com.ustadmobile.core.util.graph.MessageIdFormatter
import com.ustadmobile.core.util.graph.TimeFormatter
import com.ustadmobile.core.util.graph.UidAndLabelFormatter
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_NO_DELETE
import com.ustadmobile.lib.util.randomString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun UmAppDatabase.runPreload() {
    GlobalScope.launch { preload() }
}

/**
 * Insert a new class and
 * @param termMap course terminology map
 */
suspend fun UmAppDatabase.createNewClazzAndGroups(
    clazz: Clazz,
    impl: UstadMobileSystemImpl,
    termMap: Map<String, String>,
    context: Any
) {
    clazz.clazzTeachersPersonGroupUid = personGroupDao.insertAsync(
            PersonGroup("${clazz.clazzName} - " + termMap[TerminologyKeys.TEACHER_KEY]))

    clazz.clazzStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            termMap[TerminologyKeys.STUDENTS_KEY]))

    clazz.clazzPendingStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MessageID.pending_requests, context)))

    clazz.clazzParentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MessageID.parent, context)))

    clazz.takeIf { it.clazzCode == null }?.clazzCode = randomString(Clazz.CLAZZ_CODE_DEFAULT_LENGTH)


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


    clazz.clazzUid = clazzDao.insertAsync(clazz)
}


/**
 * Enrol the given person into the given class. The effective date of joining is midnight as per
 * the timezone of the class (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 *
 * @throws IllegalStateException when the person is already in the class
 */
@Throws(AlreadyEnroledInClassException::class)
suspend fun UmAppDatabase.enrolPersonIntoClazzAtLocalTimezone(personToEnrol: Person, clazzUid: Long,
                                                              role: Int,
                                                              clazzWithSchool: ClazzWithSchool? = null): ClazzEnrolment {
    val clazzWithSchoolVal = clazzWithSchool ?: clazzDao.getClazzWithSchool(clazzUid)
        ?: throw IllegalArgumentException("Class does not exist")

    val existingEnrolments = clazzEnrolmentDao.getAllClazzEnrolledAtTimeAsync(clazzUid,
        systemTimeInMillis(), 0, personToEnrol.personUid)

    if(existingEnrolments.isNotEmpty()) {
        throw AlreadyEnroledInClassException(existingEnrolments.first())
    }

    val clazzTimeZone = clazzWithSchoolVal.effectiveTimeZone()
    val joinTime = DateTime.now().toOffsetByTimezone(clazzTimeZone).localMidnight.utc.unixMillisLong
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

    enrolment.clazzEnrolmentDateJoined = DateTime(enrolment.clazzEnrolmentDateJoined)
        .toOffsetByTimezone(clazzTimeZone).localMidnight.utc.unixMillisLong
    if(enrolment.clazzEnrolmentDateLeft != Long.MAX_VALUE){
        enrolment.clazzEnrolmentDateLeft = DateTime(enrolment.clazzEnrolmentDateLeft)
            .toOffsetByTimezone(clazzTimeZone).localEndOfDay.utc.unixMillisLong
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
 * Enrol the given person into the given school. The effective date of joining is midnight as per
 * the timezone of the school (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 */
@Throws(AlreadyEnroledInSchoolException::class)
suspend fun UmAppDatabase.enrolPersonIntoSchoolAtLocalTimezone(personToEnrol: Person, schoolUid: Long,
                                                              role: Int)
        : SchoolMemberWithPerson {
    val schoolVal =  schoolDao.findByUidAsync(schoolUid)
        ?: throw IllegalArgumentException("School does not exist")

    val existingEnrolment = schoolMemberDao.findBySchoolAndPersonAndRole(schoolUid,
        personToEnrol.personUid, 0, systemTimeInMillis())

    if(existingEnrolment.isNotEmpty())
        throw AlreadyEnroledInSchoolException(existingEnrolment.first())

    val schoolTimeZone = schoolVal.schoolTimeZone?: "UTC"
    val joinTime = DateTime.now().toOffsetByTimezone(schoolTimeZone).localMidnight.utc.unixMillisLong
    val schoolMember = SchoolMemberWithPerson().apply {
        schoolMemberPersonUid = personToEnrol.personUid
        schoolMemberSchoolUid = schoolUid
        schoolMemberRole= role
        schoolMemberActive = true
        schoolMemberJoinDate = joinTime
        person = personToEnrol
        schoolMemberUid = schoolMemberDao.insertAsync(this)
    }

    val personGroupUid = when(role) {
        Role.ROLE_SCHOOL_STAFF_UID -> schoolVal.schoolTeachersPersonGroupUid
        Role.ROLE_SCHOOL_STUDENT_UID -> schoolVal.schoolStudentsPersonGroupUid
        Role.ROLE_SCHOOL_STUDENT_PENDING_UID -> schoolVal.schoolPendingStudentsPersonGroupUid
        else -> null
    }

    if(personGroupUid != null) {
        personGroupMemberDao.insertAsync(PersonGroupMember().also {
            it.groupMemberPersonUid = personToEnrol.personUid
            it.groupMemberGroupUid = personGroupUid
        })
    }

    return schoolMember
}

suspend fun UmAppDatabase.approvePendingClazzEnrolment(enrolment: PersonWithClazzEnrolmentDetails, clazzUid: Long) {
    val effectiveClazz = clazzDao.findByUidAsync(clazzUid)
        ?: throw IllegalStateException("Class does not exist")

    //find the group member and update that
    val numGroupUpdates = personGroupMemberDao.moveGroupAsync(enrolment.personUid,
        effectiveClazz.clazzStudentsPersonGroupUid,
        effectiveClazz.clazzPendingStudentsPersonGroupUid, systemTimeInMillis())

    if(numGroupUpdates != 1) {
        throw IllegalStateException("Approve pending clazz member - no membership of clazz's pending group!")
    }

    //change the role
    val enrolmentUpdateCount = clazzEnrolmentDao.updateClazzEnrolmentRole(enrolment.personUid, clazzUid,
        newRole = ClazzEnrolment.ROLE_STUDENT, oldRole = ClazzEnrolment.ROLE_STUDENT_PENDING,
        systemTimeInMillis())
    if(enrolmentUpdateCount != 1) {
        throw IllegalStateException("Approve pending clazz member - no update of enrolment!")
    }
}

suspend fun UmAppDatabase.declinePendingClazzEnrolment(enrolment: PersonWithClazzEnrolmentDetails, clazzUid: Long){
    val effectiveClazz = clazzDao.findByUidAsync(clazzUid)
        ?: throw IllegalStateException("Class does not exist")

        clazzEnrolmentDao.updateClazzEnrolmentActiveForPersonAndClazz(enrolment.personUid,
            clazzUid, ClazzEnrolment.ROLE_STUDENT_PENDING, false, systemTimeInMillis())

        personGroupMemberDao.updateGroupMemberActive(false, enrolment.personUid,
            effectiveClazz.clazzPendingStudentsPersonGroupUid, systemTimeInMillis())

}


suspend fun UmAppDatabase.approvePendingSchoolMember(member: SchoolMember, school: School? = null) {
    val effectiveClazz = school ?: schoolDao.findByUidAsync(member.schoolMemberSchoolUid)
        ?: throw IllegalStateException("Class does not exist")

    //change the role
    member.schoolMemberRole = Role.ROLE_SCHOOL_STUDENT_UID
    schoolMemberDao.updateAsync(member)

    //find the group member and update that
    val numGroupUpdates = personGroupMemberDao.moveGroupAsync(member.schoolMemberPersonUid,
            effectiveClazz.schoolStudentsPersonGroupUid,
            effectiveClazz.schoolPendingStudentsPersonGroupUid, systemTimeInMillis())
    if(numGroupUpdates != 1) {
        println("No group update?")
    }
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

suspend fun UmAppDatabase.generateChartData(report: ReportWithSeriesWithFilters,
                                            context: Any, impl: UstadMobileSystemImpl, loggedInPersonUid: Long): ChartData{

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
            Report.GENDER -> {
                MessageIdFormatter(
                        genderMap.mapKeys { it.key.toString() },
                        impl, context)
            }
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
        Report.GENDER -> {
            MessageIdFormatter(
                    genderMap.mapKeys { it.key.toString() },
                    impl, context)
        }
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
                    .plus(0L to impl.getString(MessageID.unset, context))
            UidAndLabelFormatter(reasonLabelList)
        }
        else ->{
            null
        }
    }

    return ChartData(seriesDataList.toList(), report, yAxisValueFormatter, xAxisFormatter)
}

fun UmAppDatabase.generateStatementList(report: ReportWithSeriesWithFilters, loggedInPersonUid: Long):
        List<DoorDataSourceFactory<Int, StatementEntityWithDisplayDetails>> {

    val queries = report.generateSql(loggedInPersonUid, dbType())
    val statementDataSourceList = mutableListOf<DoorDataSourceFactory<Int, StatementEntityWithDisplayDetails>>()
    queries.forEach {
        statementDataSourceList.add(statementDao.getListResults(SimpleDoorQuery(it.value.sqlListStr, it.value.queryParams)))
    }
    return statementDataSourceList.toList()
}


data class ChartData(val seriesData: List<SeriesData>,
                     val reportWithFilters: ReportWithSeriesWithFilters,
                     val yAxisValueFormatter: LabelValueFormatter?,
                     val xAxisValueFormatter: LabelValueFormatter?)

data class SeriesData(val dataList: List<StatementDao.ReportData>,
                      val subGroupFormatter: LabelValueFormatter?,
                      val series: ReportSeries)


/**
 * Insert a new school
 */
suspend fun UmAppDatabase.createNewSchoolAndGroups(
    school: School,
    impl: UstadMobileSystemImpl,
    context: Any
) :Long {
    school.schoolTeachersPersonGroupUid = personGroupDao.insertAsync(
            PersonGroup("${school.schoolName} - " +
                    impl.getString(MessageID.teachers_literal, context)))

    school.schoolStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup(
            "${school.schoolName} - " +
            impl.getString(MessageID.students, context)))

    school.schoolPendingStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup(
            "${school.schoolName} - " +
            impl.getString(MessageID.pending_requests, context)))


    school.takeIf { it.schoolCode == null }?.schoolCode = randomString(Clazz.CLAZZ_CODE_DEFAULT_LENGTH)

    school.schoolUid = schoolDao.insertAsync(school)

    return school.schoolUid
}

suspend fun UmAppDatabase.enrollPersonToSchool(schoolUid: Long,
                                 personUid:Long, role: Int): SchoolMember{

    val school = schoolDao.findByUidAsync(schoolUid)?:
    throw IllegalArgumentException("School does not exist")

    //Check if relationship already exists
    val matches = schoolMemberDao.findBySchoolAndPersonAndRole(schoolUid, personUid,  role)
    if(matches.isEmpty()) {

        val schoolMember = SchoolMember().apply {
            schoolMemberActive = true
            schoolMemberPersonUid = personUid
            schoolMemberSchoolUid = schoolUid
            schoolMemberRole = role
            schoolMemberJoinDate = systemTimeInMillis()
            schoolMemberUid = schoolMemberDao.insertAsync(this)
        }


        val personGroupUid = when(role) {
            Role.ROLE_SCHOOL_STAFF_UID -> school.schoolTeachersPersonGroupUid
            Role.ROLE_SCHOOL_STUDENT_UID -> school.schoolStudentsPersonGroupUid
            Role.ROLE_SCHOOL_STUDENT_PENDING_UID -> school.schoolPendingStudentsPersonGroupUid
            else -> null
        }

        if(personGroupUid != null) {
            personGroupMemberDao.insertAsync(PersonGroupMember().also {
                it.groupMemberPersonUid = schoolMember.schoolMemberPersonUid
                it.groupMemberGroupUid = personGroupUid
            })
        }

        return schoolMember
    }else{
        return matches[0]
    }
}

/**
 * Gets the maximum number of items that can be in a query parameter of type list. This is 100 on
 * SQLite and unlimited (-1) on Postgres
 */
internal val UmAppDatabase.maxQueryParamListSize: Int
    get() = if(this.dbType() == DoorDbType.SQLITE) 99 else -1


data class ContainerEntryWithMd5Partition(val entriesWithMatchingFile: List<ContainerEntryWithContainerEntryFile>,
                                          val entriesWithoutMatchingFile: List<ContainerEntryWithMd5>)

data class ContainerEntryPartition(
    val entriesWithMatchingFile: List<ContainerEntryWithMd5>,
    val entriesWithoutMatchingFile: List<ContainerEntryWithMd5>,
    val existingFiles: List<ContainerEntryFile>
)

/**
 * Partition a list of containerentrywithmd5 into a list of those md5s that we already have
 * and those that we don't have yet.
 */
suspend fun UmAppDatabase.partitionContainerEntriesWithMd5(
    containerEntryFiles: List<ContainerEntryWithMd5>
): ContainerEntryPartition {
    val existingFiles = containerEntryFileDao.findEntriesByMd5SumsSafeAsync(containerEntryFiles
        .mapNotNull { it.cefMd5 }, maxQueryParamListSize)
    val existingMd5s = existingFiles.mapNotNull { it.cefMd5 }.toSet()

    val (entriesWithFile, entriesNeedDownloaded) = containerEntryFiles
        .partition { it.cefMd5 in existingMd5s }

    return ContainerEntryPartition(entriesWithFile, entriesNeedDownloaded, existingFiles)
}

/**
 * Given a list of ContainerEntryWithMd5 (e.g. the container entries that are required for a
 * container and the md5 sum of the contents that each should be linked with), link each
 * ContainerEntry in the list with the ContainerEntryFile where the ContainerEntryFile is already
 * present locally. Returns a list of those not present locally (e.g. those that need downloaded).
 *
 * @param containerUid The container uid for which we are linking entries.
 * @param containerEntryFiles The ContainerEntryFile list to check.
 * throwing an exception)
 *
 * @return a pair of
 */
suspend fun UmAppDatabase.linkExistingContainerEntries(
    containerUid: Long,
    containerEntryFiles: List<ContainerEntryWithMd5>
): ContainerEntryWithMd5Partition {

    val (entriesWithFile, entriesNeedDownloaded, existingFiles) = partitionContainerEntriesWithMd5(
            containerEntryFiles)

    val alreadyLinkedEntries = containerEntryDao.findByContainerAsync(containerUid)
    val entriesToLink = entriesWithFile
            .filter { entryWithFile ->! alreadyLinkedEntries.any { it.cePath ==  entryWithFile.cePath } }
            .onEach { entryWithFile ->
                entryWithFile.ceUid = 0L
                entryWithFile.ceCefUid = existingFiles.first { it.cefMd5 == entryWithFile.cefMd5 }.cefUid
            }

    containerEntryDao.insertListAsync(entriesToLink)

    val entriesWithValRetList = entriesWithFile.map { entryWithFile ->
        ContainerEntryWithContainerEntryFile().apply {
            this.containerEntryFile = existingFiles.firstOrNull { it.cefMd5 ==  entryWithFile.cefMd5 }
            ceUid = entryWithFile.ceUid
            ceContainerUid = containerUid
            ceCefUid = containerEntryFile?.cefUid ?: 0L
            cePath = entryWithFile.cePath
        }
    }

    return ContainerEntryWithMd5Partition(entriesWithValRetList,
            entriesNeedDownloaded)
}

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

/**
 * Insert authentication credentials for the given person uid with the given password. This is fine
 * to use in tests etc, but for performance it is better to use AuthManager.setAuth
 */
suspend fun UmAppDatabase.insertPersonAuthCredentials2(
    personUid: Long,
    password: String,
    pbkdf2Params: Pbkdf2Params,
    site: Site? = null
) {
    val db = (this as DoorDatabaseRepository).db as UmAppDatabase
    val effectiveSite = site ?: db.siteDao.getSite()
    val authSalt = effectiveSite?.authSalt ?: throw IllegalStateException("insertAuthCredentials: No auth salt!")
    val lastChangedBy = db.syncNodeDao.getLocalNodeClientId()

    personAuth2Dao.insertAsync(PersonAuth2().apply {
        pauthUid = personUid
        pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
        pauthAuth = password.doublePbkdf2Hash(authSalt, pbkdf2Params).encodeBase64()
        pauthLcb = lastChangedBy
    })
}

/**
 * 25/Feb/2022
 *
 * This should NOT be needed, but content imports (maybe 4% of the time) have been observed that end
 * with the container size not being updated in spite of the fact that the process completed. This
 * happens for no apparent reason. All container entries were present. The fileSize on the container
 * should be 0 until the container is ready (to avoid any possibility of a client downloading a
 * container that is not ready).
 */
suspend fun UmAppDatabase.validateAndUpdateContainerSize(
    containerUid: Long,
    attempts: Int = 3,
    waitInterval: Long = 200
) : Long{
    var containerSize: Long = 0
    for(i in 0 until attempts) {
        containerSize = withDoorTransactionAsync(UmAppDatabase::class) {
            val currentSize = containerDao.getContainerSizeByUid(containerUid)
            if(currentSize != 0L)
                return@withDoorTransactionAsync currentSize

            containerDao.updateContainerSizeAndNumEntriesAsync(containerUid, systemTimeInMillis())
            containerDao.getContainerSizeByUid(containerUid)
        }
        if(containerSize != 0L)
            return containerSize

        delay(waitInterval)
    }

    return containerSize
}
