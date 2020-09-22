package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString

fun UmAppDatabase.runPreload() {
    preload()
    timeZoneEntityDao.insertSystemTimezones()
    personDetailPresenterFieldDao.preloadCoreFields()
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
        clazz.clazzTeachersPersonGroupUid, Role.ROLE_TEACHER_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(Clazz.TABLE_ID, clazz.clazzUid,
        clazz.clazzStudentsPersonGroupUid, Role.ROLE_STUDENT_UID.toLong()))
    entityRoleDao.insertAsync(EntityRole(Clazz.TABLE_ID, clazz.clazzUid,
        clazz.clazzPendingStudentsPersonGroupUid, Role.ROLE_STUDENT_PENDING_UID.toLong()))
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
        Role.SCHOOL_ROLE_TEACHER -> schoolVal.schoolTeachersPersonGroupUid
        Role.SCHOOL_ROLE_STUDENT -> schoolVal.schoolStudentsPersonGroupUid
        Role.SCHOOL_ROLE_STUDENT_PENDING -> schoolVal.schoolPendingStudentsPersonGroupUid
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

    //change the role
    member.clazzMemberRole = ClazzMember.ROLE_STUDENT
    clazzMemberDao.updateAsync(member)

    //find the group member and update that
    val numGroupUpdates = personGroupMemberDao.moveGroupAsync(member.clazzMemberPersonUid,
            effectiveClazz.clazzStudentsPersonGroupUid,
            effectiveClazz.clazzPendingStudentsPersonGroupUid)
    if(numGroupUpdates != 1) {
        println("No group update?")
    }
}

/**
 * Inserts the person, sets its group and groupmember. Does not check if its an update
 */
suspend fun UmAppDatabase.insertPersonAndGroup(entity: PersonWithAccount,
                loggedInPerson: Person? = null): PersonWithAccount{

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

    //Create a roleentity of select person role with this.

    if(loggedInPerson != null) {
        val viewPersonRoles = roleDao.findByPermissionAndNameAsync(Role.PERMISSION_PERSON_SELECT,
                Role.ROLE_VIEW_STUDENTS_NAME)
        val viewPersonRole = if (viewPersonRoles.isEmpty()) {
            Role().apply {
                roleName = Role.ROLE_VIEW_STUDENTS_NAME
                roleActive = true
                rolePermissions = Role.PERMISSION_PERSON_SELECT
                roleUid = roleDao.insertAsync(this)
            }
        } else {
            viewPersonRoles[0]
        }

        //TODO: Check if person has its own group?
        EntityRole().apply {
            erTableId = Person.TABLE_ID
            erActive = true
            erRoleUid = viewPersonRole.roleUid
            erGroupUid = loggedInPerson?.personGroupUid ?: 0L
            erEntityUid = entity.personUid
            erUid = entityRoleDao.insertAsync(this)
        }
    }


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
            school.schoolTeachersPersonGroupUid, Role.SCHOOL_ROLE_TEACHER.toLong()))
    entityRoleDao.insertAsync(EntityRole(School.TABLE_ID, school.schoolUid,
            school.schoolStudentsPersonGroupUid, Role.SCHOOL_ROLE_TEACHER.toLong()))
    entityRoleDao.insertAsync(EntityRole(School.TABLE_ID, school.schoolUid,
            school.schoolPendingStudentsPersonGroupUid, Role.SCHOOL_ROLE_STUDENT_PENDING.toLong()))

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
            Role.SCHOOL_ROLE_TEACHER -> school.schoolTeachersPersonGroupUid
            Role.SCHOOL_ROLE_STUDENT -> school.schoolStudentsPersonGroupUid
            Role.SCHOOL_ROLE_STUDENT_PENDING -> school.schoolPendingStudentsPersonGroupUid
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