package com.ustadmobile.core.util.ext

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.json.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class UmAppDatabaseExtTest {


    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private val context = Any()

    private lateinit var mockSystemImpl: UstadMobileSystemImpl

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .addSyncCallback(nodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)

        okHttpClient = OkHttpClient()

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
            engine {
                preconfigured = okHttpClient
            }
        }

        repo = db.asRepository(repositoryConfig(context, "http://localhost/dummy/",
            nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient))

        mockSystemImpl = mock {
            on { getString(any(), any())}.thenAnswer {
                "${it.arguments[0]}"
            }
        }
    }

    @After
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun givenClazzDoesNotExist_whenCreateClazzAndGroupsCalled_thenClazzGroupsAndEntityRolesCreated() = runBlocking {
        val testClazz = Clazz("Test name")

        repo.createNewClazzAndGroups(testClazz, mockSystemImpl, mapOf(), context)

        val clazzInDb = db.clazzDao.findByUid(testClazz.clazzUid)!!
        Assert.assertEquals("Stored class has same name", testClazz.clazzName,
                clazzInDb?.clazzName)

        val teacherGroup = db.personGroupDao.findByUid(clazzInDb.clazzTeachersPersonGroupUid)
        Assert.assertNotNull("Teacher PersonGroup created", teacherGroup)

        val studentGroup = db.personGroupDao.findByUid(clazzInDb.clazzStudentsPersonGroupUid)
        Assert.assertNotNull("Student person group created", studentGroup)
    }

    @Test
    fun givenExistingClazz_whenEnrolMemberCalled_thenClazzMemberIsCreatedAndPersonGroupMemberCreated() = runBlocking {
        val testClazz = Clazz("Test name")
        val testPerson = Person("teacher", "Teacher", "Test")

        repo.createNewClazzAndGroups(testClazz, mockSystemImpl, mapOf(), context)
        repo.personDao.insert(testPerson)

        repo.enrolPersonIntoClazzAtLocalTimezone(testPerson, testClazz.clazzUid, ClazzEnrolment.ROLE_TEACHER)

        val personClazzes = db.clazzEnrolmentDao.findAllClazzesByPersonWithClazzAsListAsync(
                testPerson.personUid)

        Assert.assertTrue("PersonMember was created", personClazzes.any { it.clazzEnrolmentClazzUid == testClazz.clazzUid })

        val personGroups = db.personGroupMemberDao.findAllGroupWherePersonIsIn(testPerson.personUid)
        Assert.assertEquals("Person is now teacher group",
                testClazz.clazzTeachersPersonGroupUid,
                personGroups.first().groupMemberGroupUid)
    }

    @Test
    fun givenExistingClazz_whenEnrolMemberCalledWithParent_thenStudentAndParentAreEnroled() = runBlocking {
        val testClazz = Clazz("Test name")
        val testStudent = Person("student", "Student", "Jones")
        val testParent = Person("parent", "Parent", "Jones")

        repo.createNewClazzAndGroups(testClazz, mockSystemImpl, mapOf(), context)
        testStudent.personUid = repo.personDao.insert(testStudent)
        testParent.personUid = repo.personDao.insert(testParent)

        repo.personParentJoinDao.insertAsync(PersonParentJoin().apply {
            ppjMinorPersonUid = testStudent.personUid
            ppjParentPersonUid = testParent.personUid
        })

        repo.enrolPersonIntoClazzAtLocalTimezone(testStudent, testClazz.clazzUid,
            ClazzEnrolment.ROLE_STUDENT)

        val parentPersonGroups = db.personGroupMemberDao.findAllGroupWherePersonIsIn(
            testParent.personUid)
        Assert.assertEquals("Parent is in parents group",
            testClazz.clazzParentsPersonGroupUid,
            parentPersonGroups.first().groupMemberGroupUid)
        val parentEnrolments = db.clazzEnrolmentDao.findAllClazzesByPersonWithClazzAsListAsync(
            testParent.personUid)
        Assert.assertEquals("Parent is enroled with parent role in class",
            ClazzEnrolment.ROLE_PARENT, parentEnrolments.first().clazzEnrolmentRole)
    }


    @Test
    fun givenExistingSchool_whenEnrolMemberCalled_thenSchoolMemberIsCreatedAndPersonGroupMemberCreated() = runBlocking {
        val testSchool = School("School A")
        testSchool.schoolActive =true
        val testPerson = Person("teacher", "Teacher", "Test")

        testSchool.schoolUid = repo.createNewSchoolAndGroups(testSchool, mockSystemImpl, context)
        testPerson.personUid = repo.personDao.insert(testPerson)

        repo.enrollPersonToSchool(testSchool.schoolUid, testPerson.personUid,
                Role.ROLE_SCHOOL_STAFF_UID)

        val schoolMembers = db.schoolMemberDao.findBySchoolAndPersonAndRole(
                testSchool.schoolUid,
                testPerson.personUid, Role.ROLE_SCHOOL_STAFF_UID)

        Assert.assertTrue("PersonMember was created", schoolMembers.any {
            it.schoolMemberSchoolUid == testSchool.schoolUid })

        val personGroups = db.personGroupMemberDao.findAllGroupWherePersonIsIn(testPerson.personUid)
        Assert.assertEquals("Person is now teacher group",
                testSchool.schoolTeachersPersonGroupUid,
                personGroups.first().groupMemberGroupUid)
    }
}