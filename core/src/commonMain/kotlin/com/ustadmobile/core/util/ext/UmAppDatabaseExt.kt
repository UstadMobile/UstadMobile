package com.ustadmobile.core.util.ext

import androidx.paging.DataSource
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.graph.*
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import kotlinx.coroutines.withTimeoutOrNull

fun UmAppDatabase.runPreload() {
    preload()
}

/**
 * Insert a new class and
 */
suspend fun UmAppDatabase.createNewClazzAndGroups(clazz: Clazz, impl: UstadMobileSystemImpl, context: Any) {
    clazz.clazzTeachersPersonGroupUid = personGroupDao.insertAsync(
            PersonGroup("${clazz.clazzName} - " +
                    impl.getString(MessageID.teachers_literal, context)))

    clazz.clazzStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MessageID.students, context)))

    clazz.clazzPendingStudentsPersonGroupUid = personGroupDao.insertAsync(PersonGroup("${clazz.clazzName} - " +
            impl.getString(MessageID.pending_requests, context)))

    clazz.takeIf { it.clazzCode == null }?.clazzCode = randomString(Clazz.CLAZZ_CODE_DEFAULT_LENGTH)

    clazz.clazzUid = clazzDao.insertAsync(clazz)

    entityRoleDao.insertAsync(EntityRole(Clazz.TABLE_ID, clazz.clazzUid,
        clazz.clazzTeachersPersonGroupUid, Role.ROLE_CLAZZ_TEACHER_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(Clazz.TABLE_ID, clazz.clazzUid,
        clazz.clazzStudentsPersonGroupUid, Role.ROLE_CLAZZ_STUDENT_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(Clazz.TABLE_ID, clazz.clazzUid,
        clazz.clazzPendingStudentsPersonGroupUid, Role.ROLE_CLAZZ_STUDENT_PENDING_UID.toLong()))
}

/**
 * Enrol the given person into the given class. The effective date of joining is midnight as per
 * the timezone of the class (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 */
suspend fun UmAppDatabase.enrolPersonIntoClazzAtLocalTimezone(personToEnrol: Person, clazzUid: Long,
                                                              role: Int,
                                                              clazzWithSchool: ClazzWithSchool? = null): ClazzMemberWithPerson {
    val clazzWithSchoolVal = clazzWithSchool ?: clazzDao.getClazzWithSchool(clazzUid)
        ?: throw IllegalArgumentException("Class does not exist")

    val clazzTimeZone = clazzWithSchoolVal.effectiveTimeZone()
    val joinTime = DateTime.now().toOffsetByTimezone(clazzTimeZone).localMidnight.utc.unixMillisLong
    val clazzMember = ClazzMemberWithPerson().apply {
        clazzMemberPersonUid = personToEnrol.personUid
        clazzMemberClazzUid = clazzUid
        clazzMemberRole = role
        clazzMemberActive = true
        clazzMemberDateJoined = joinTime
        person = personToEnrol
        clazzMemberUid = clazzMemberDao.insertAsync(this)
    }

    val personGroupUid = when(role) {
        ClazzMember.ROLE_TEACHER -> clazzWithSchoolVal.clazzTeachersPersonGroupUid
        ClazzMember.ROLE_STUDENT -> clazzWithSchoolVal.clazzStudentsPersonGroupUid
        ClazzMember.ROLE_STUDENT_PENDING -> clazzWithSchoolVal.clazzPendingStudentsPersonGroupUid
        else -> null
    }

    if(personGroupUid != null) {
        val personGroupMember = PersonGroupMember().also {
            it.groupMemberPersonUid = personToEnrol.personUid
            it.groupMemberGroupUid = personGroupUid
            it.groupMemberUid = personGroupMemberDao.insertAsync(it)
        }
    }

    return clazzMember
}

/**
 * Enrol the given person into the given school. The effective date of joining is midnight as per
 * the timezone of the school (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 */
suspend fun UmAppDatabase.enrolPersonIntoSchoolAtLocalTimezone(personToEnrol: Person, schoolUid: Long,
                                                              role: Int)
        : SchoolMemberWithPerson {
    val schoolVal =  schoolDao.findByUidAsync(schoolUid)
    ?: throw IllegalArgumentException("School does not exist")

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
        val personGroupMember = PersonGroupMember().also {
            it.groupMemberPersonUid = personToEnrol.personUid
            it.groupMemberGroupUid = personGroupUid
            it.groupMemberUid = personGroupMemberDao.insertAsync(it)
        }
    }

    return schoolMember
}

suspend fun UmAppDatabase.approvePendingClazzMember(member: ClazzMember, clazz: Clazz? = null) {
    val effectiveClazz = clazz ?: clazzDao.findByUidAsync(member.clazzMemberClazzUid)
        ?: throw IllegalStateException("Class does not exist")

    //find the group member and update that
    val numGroupUpdates = personGroupMemberDao.moveGroupAsync(member.clazzMemberPersonUid,
            effectiveClazz.clazzStudentsPersonGroupUid,
            effectiveClazz.clazzPendingStudentsPersonGroupUid)

    if(numGroupUpdates != 1) {
        throw IllegalStateException("Approve pending clazz member - no membership of clazz's pending group!")
    }

    //change the role
    member.clazzMemberRole = ClazzMember.ROLE_STUDENT
    clazzMemberDao.updateAsync(member)

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
            effectiveClazz.schoolPendingStudentsPersonGroupUid)
    if(numGroupUpdates != 1) {
        println("No group update?")
    }
}

/**
 * Inserts the person, sets its group and groupmember. Does not check if its an update
 */
suspend fun <T: Person> UmAppDatabase.insertPersonAndGroup(entity: T,
                loggedInPerson: Person? = null): T{

    val groupPerson = PersonGroup().apply {
        groupName = "Person individual group"
        personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
    }
    //Create person's group
    groupPerson.groupUid = personGroupDao.insertAsync(groupPerson)

    //Assign to person
    entity.personGroupUid = groupPerson.groupUid
    entity.personUid = personDao.insertAsync(entity)

    //Assign person to PersonGroup ie: Create PersonGroupMember
    personGroupMemberDao.insertAsync(
            PersonGroupMember(entity.personUid, entity.personGroupUid))

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
                        mapOf(Person.GENDER_MALE.toString() to MessageID.male,
                                Person.GENDER_FEMALE.toString() to MessageID.female,
                                Person.GENDER_OTHER.toString() to MessageID.other,
                                Person.GENDER_UNSET.toString() to MessageID.unset),
                        impl, context)
            }
            Report.CONTENT_ENTRY ->{
                val listOfUids = reportList.mapNotNull { it.subgroup?.toLong() }.toSet().toList()
                val entryLabelList = contentEntryDao.getContentEntryFromUids(listOfUids)
                        .map { it.uid to it.labelName }.toMap()
                UidAndLabelFormatter(entryLabelList)
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
                    mapOf(Person.GENDER_MALE.toString() to MessageID.male,
                            Person.GENDER_FEMALE.toString() to MessageID.female,
                            Person.GENDER_OTHER.toString() to MessageID.other,
                            Person.GENDER_UNSET.toString() to MessageID.unset),
                    impl, context)
        }
        Report.CONTENT_ENTRY ->{
            val entryLabelList = contentEntryDao.getContentEntryFromUids(xAxisList
                    .map { it.toLong() }).map { it.uid to it.labelName }.toMap()
            UidAndLabelFormatter(entryLabelList)
        }
        else ->{
            null
        }
    }

    return ChartData(seriesDataList.toList(), report, yAxisValueFormatter, xAxisFormatter)
}

fun UmAppDatabase.generateStatementList(report: ReportWithSeriesWithFilters, loggedInPersonUid: Long):
        List<DataSource.Factory<Int, StatementEntityWithDisplay>> {

    val queries = report.generateSql(loggedInPersonUid, dbType())
    val statementDataSourceList = mutableListOf<DataSource.Factory<Int, StatementEntityWithDisplay>>()
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
suspend fun UmAppDatabase.createNewSchoolAndGroups(school: School,
                                                   impl: UstadMobileSystemImpl, context: Any)
                                                    :Long {
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

    entityRoleDao.insertAsync(EntityRole(School.TABLE_ID, school.schoolUid,
            school.schoolTeachersPersonGroupUid, Role.ROLE_SCHOOL_STAFF_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(School.TABLE_ID, school.schoolUid,
            school.schoolStudentsPersonGroupUid, Role.ROLE_SCHOOL_STUDENT_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(School.TABLE_ID, school.schoolUid,
            school.schoolPendingStudentsPersonGroupUid, Role.ROLE_SCHOOL_STUDENT_PENDING_UID.toLong()))

    return school.schoolUid
}

suspend fun UmAppDatabase.enrollPersonToSchool(schoolUid: Long,
                                 personUid:Long, role: Int): SchoolMember{

    val school = schoolDao.findByUidAsync(schoolUid)?:
    throw IllegalArgumentException("School does not exist")

    //Check if relationship already exists
    val matches = schoolMemberDao.findBySchoolAndPersonAndRole(schoolUid, personUid,  role)
    if(matches.isEmpty()) {

        val schoolMember = SchoolMember()
        schoolMember.schoolMemberActive = true
        schoolMember.schoolMemberPersonUid = personUid
        schoolMember.schoolMemberSchoolUid = schoolUid
        schoolMember.schoolMemberRole = role
        schoolMember.schoolMemberJoinDate = systemTimeInMillis()

        schoolMember.schoolMemberUid = schoolMemberDao.insert(schoolMember)

        val personGroupUid = when(role) {
            Role.ROLE_SCHOOL_STAFF_UID -> school.schoolTeachersPersonGroupUid
            Role.ROLE_SCHOOL_STUDENT_UID -> school.schoolStudentsPersonGroupUid
            Role.ROLE_SCHOOL_STUDENT_PENDING_UID -> school.schoolPendingStudentsPersonGroupUid
            else -> null
        }

        if(personGroupUid != null) {
            val personGroupMember = PersonGroupMember().also {
                it.groupMemberPersonUid = schoolMember.schoolMemberPersonUid
                it.groupMemberGroupUid = personGroupUid
                it.groupMemberUid = personGroupMemberDao.insertAsync(it)
            }
        }

        return schoolMember
    }else{
        return matches[0]
    }
}


suspend fun UmAppDatabase.getQuestionListForView(clazzWorkWithSubmission: ClazzWorkWithSubmission,
                                                 clazzMemberUid: Long, responsePersonUid : Long)
        : List<ClazzWorkQuestionAndOptionWithResponse>{

    val questionsAndOptionsWithResponses :List<ClazzWorkQuestionAndOptionWithResponseRow> = withTimeoutOrNull(2000){
        clazzWorkQuestionDao.findAllQuestionsAndOptionsWithResponse(clazzWorkWithSubmission.clazzWorkUid?:0L,
                clazzMemberUid)
    } ?: listOf()

    val questionsAndOptionsWithResponseList: List<ClazzWorkQuestionAndOptionWithResponse> =
            questionsAndOptionsWithResponses.groupBy { it.clazzWorkQuestion }.entries
                    .map {
                        val questionUid = it.key?.clazzWorkQuestionUid ?: 0L

                        ClazzWorkQuestionAndOptionWithResponse(
                                clazzWorkWithSubmission ,
                                it.key ?: ClazzWorkQuestion(),
                                it.value.map {
                                    it.clazzWorkQuestionOption ?: ClazzWorkQuestionOption()
                                },
                                it.value.map {
                                    it.clazzWorkQuestionOptionResponse
                                }.first()?: ClazzWorkQuestionResponse().apply {
                                    clazzWorkQuestionResponseQuestionUid = questionUid?:0L
                                    clazzWorkQuestionResponsePersonUid = responsePersonUid
                                    clazzWorkQuestionResponseClazzMemberUid = clazzMemberUid
                                            ?: 0L
                                    clazzWorkQuestionResponseClazzWorkUid = clazzWorkWithSubmission.clazzWorkUid
                                            ?: 0L
                                })
                    }


    return questionsAndOptionsWithResponseList
}