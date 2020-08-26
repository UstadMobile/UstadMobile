package com.ustadmobile.core.util.ext

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UmAppDatabaseExtTest {


    private lateinit var db: UmAppDatabase

    private val context = Any()

    private lateinit var mockSystemImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        db = UmAppDatabase.getInstance(context).also {
            it.clearAllTables()
        }

        mockSystemImpl = mock {
            on { getString(any(), any())}.thenAnswer {
                "${it.arguments[0]}"
            }
        }
    }

    @Test
    fun givenClazzDoesNotExist_whenCreateClazzAndGroupsCalled_thenClazzGroupsAndEntityRolesCreated() = runBlocking {
        val testClazz = Clazz("Test name")

        db.createNewClazzAndGroups(testClazz, mockSystemImpl, context)

        val clazzInDb = db.clazzDao.findByUid(testClazz.clazzUid)
        Assert.assertEquals("Stored class has same name", testClazz.clazzName,
                clazzInDb?.clazzName)

        val teacherGroup = db.personGroupDao.findByUid(clazzInDb!!.clazzTeachersPersonGroupUid)
        Assert.assertNotNull("Teacher PersonGroup created", teacherGroup)

        val studentGroup = db.personGroupDao.findByUid(clazzInDb!!.clazzStudentsPersonGroupUid)
        Assert.assertNotNull("Student person group created", studentGroup)

        Assert.assertEquals("Teacher group has entity role", 1,
                db.entityRoleDao.findByEntitiyAndPersonGroupAndRole(
                        Clazz.TABLE_ID, testClazz.clazzUid, teacherGroup!!.groupUid,
                        Role.ROLE_TEACHER_UID.toLong()).size)

        Assert.assertEquals("Student group has entity role", 1,
                db.entityRoleDao.findByEntitiyAndPersonGroupAndRole(
                        Clazz.TABLE_ID, testClazz.clazzUid, studentGroup!!.groupUid,
                        Role.ROLE_STUDENT_UID.toLong()).size)
    }

    @Test
    fun givenExistingClazz_whenEnrolMemberCalled_thenClazzMemberIsCreatedAndPersonGroupMemberCreated() = runBlocking {
        val testClazz = Clazz("Test name")
        val testPerson = Person("teacher", "Teacher", "Test")

        db.createNewClazzAndGroups(testClazz, mockSystemImpl, context)
        db.personDao.insert(testPerson)

        db.enrolPersonIntoClazzAtLocalTimezone(testPerson, testClazz.clazzUid, ClazzMember.ROLE_TEACHER)

        val personClazzes = db.clazzMemberDao.findAllClazzesByPersonWithClazzAsList(
                testPerson.personUid, systemTimeInMillis())

        Assert.assertTrue("PersonMember was created", personClazzes.any { it.clazzMemberClazzUid == testClazz.clazzUid })

        val personGroups = db.personGroupMemberDao.findAllGroupWherePersonIsIn(testPerson.personUid)
        Assert.assertEquals("Person is now teacher group",
                testClazz.clazzTeachersPersonGroupUid,
                personGroups.first().groupMemberGroupUid)
    }


    @Test
    fun givenExistingSchool_whenEnrolMemberCalled_thenSchoolMemberIsCreatedAndPersonGroupMemberCreated() = runBlocking {
        val testSchool = School("School A")
        testSchool.schoolActive =true
        val testPerson = Person("teacher", "Teacher", "Test")

        testSchool.schoolUid = db.createNewSchoolAndGroups(testSchool, mockSystemImpl, context)
        testPerson.personUid = db.personDao.insert(testPerson)

        db.enrollPersonToSchool(testSchool.schoolUid, testPerson.personUid,
                SchoolMember.SCHOOL_ROLE_TEACHER)

        val schoolMembers = db.schoolMemberDao.findBySchoolAndPersonAndRole(
                testSchool.schoolUid,
                testPerson.personUid, SchoolMember.SCHOOL_ROLE_TEACHER)

        Assert.assertTrue("PersonMember was created", schoolMembers.any {
            it.schoolMemberSchoolUid == testSchool.schoolUid })

        val personGroups = db.personGroupMemberDao.findAllGroupWherePersonIsIn(testPerson.personUid)
        Assert.assertEquals("Person is now teacher group",
                testSchool.schoolTeachersPersonGroupUid,
                personGroups.first().groupMemberGroupUid)
    }
}