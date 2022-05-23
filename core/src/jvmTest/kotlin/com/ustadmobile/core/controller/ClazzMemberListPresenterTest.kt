
package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.weeks
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.processEnrolmentIntoClass
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT_PENDING
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.mockito.kotlin.*


class ClazzMemberListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzMemberListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzEnrolmentDaoSpy: ClazzEnrolmentDao

    private lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        accountManager = di.direct.instance()

        db = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)

        repoClazzEnrolmentDaoSpy = spy(repo.clazzEnrolmentDao)
        whenever(repo.clazzEnrolmentDao).thenReturn(repoClazzEnrolmentDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenActiveUserDoesNotHaveAddPermissions_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnViewAndSetAddVisibleToFalse() {
        //TODO: insert any entities that are used only in this test
        val testEntity = ClazzEnrolment().apply {
            //set variables here
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>(ARG_CLAZZUID to "42")
        val presenter = ClazzMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzEnrolmentDaoSpy, timeout(5000)).findByClazzUidAndRole(eq(42L),
                eq(ClazzEnrolment.ROLE_STUDENT), eq(1), eq("%"), eq(1), any(), any())
        verify(repoClazzEnrolmentDaoSpy, timeout(5000)).findByClazzUidAndRole(eq(42L),
                eq(ClazzEnrolment.ROLE_TEACHER), eq(1), eq("%"), eq(1), any(), any())

        verify(mockView, timeout(5000)).list = any()
        verify(mockView, timeout(5000)).studentList = any()

        verify(mockView, timeout(5000)).addStudentVisible = false
        verify(mockView, timeout(5000)).addTeacherVisible = false
    }

    @Test
    fun givenActiveAccountHasAddPermissions_whenOnCreateCalled_thenShouldSetAddOptionsToBeVisible() {
        val testClazz = Clazz("Test clazz").apply {
            clazzUid = repo.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }

        runBlocking {
            repo.grantScopedPermission(activePerson,
                Role.PERMISSION_CLAZZ_ADD_STUDENT or Role.PERMISSION_CLAZZ_ADD_TEACHER,
                Clazz.TABLE_ID, testClazz.clazzUid)
        }

        val endpointUrl = accountManager.activeEndpoint.url

        accountManager.startLocalTestSessionBlocking(activePerson, endpointUrl)
        val presenterArgs = mapOf(ARG_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).addStudentVisible = true
        verify(mockView, timeout(5000)).addTeacherVisible = true
    }

    @Test
    fun givenActiveAccountHasAddPermissions_whenPendingStudentApproved_thenShouldUpdate() {
        val testClazz = Clazz("Test clazz")
        runBlocking { repo.createNewClazzAndGroups(testClazz, di.direct.instance(), mapOf(),
            context) }

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }

        val pendingPerson = Person().apply {
            firstNames = "Pending"
            lastName = "Student"
            username = "pending"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }


        val enrolment = ClazzEnrolment().apply {
            clazzEnrolmentPersonUid = pendingPerson.personUid
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentDateJoined = (DateTime(testClazz.clazzStartTime) + 1.weeks).unixMillisLong
            clazzEnrolmentDateLeft = Long.MAX_VALUE
            clazzEnrolmentRole = ROLE_STUDENT_PENDING
        }
        val pendingEnrolment = PersonWithClazzEnrolmentDetails().apply {
            personUid = pendingPerson.personUid
        }
        runBlocking {
            repo.processEnrolmentIntoClass(enrolment)

            repo.grantScopedPermission(activePerson,
                Role.PERMISSION_CLAZZ_ADD_STUDENT or Role.PERMISSION_CLAZZ_ADD_TEACHER,
                Clazz.TABLE_ID, testClazz.clazzUid)
        }

        val endpointUrl = accountManager.activeEndpoint.url
        accountManager.startLocalTestSessionBlocking(activePerson, endpointUrl)

        val presenterArgs = mapOf<String,String>(ARG_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //wait for it to load
        verify(mockView, timeout(5000)).addStudentVisible = true

        presenter.handleClickPendingRequest(pendingEnrolment!!, true)

        runBlocking {
            db.waitUntil(5000, listOf("ClazzEnrolment", "PersonGroupMember")) {
                runBlocking { db.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(pendingEnrolment!!.personUid,
                    testClazz.clazzUid)?.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT }
                && runBlocking {
                    db.personGroupMemberDao.findAllGroupWherePersonIsIn(pendingEnrolment!!.personUid).any {
                        it.groupMemberGroupUid == testClazz.clazzStudentsPersonGroupUid
                    }
                }
            }
        }

        val clazzEnrolment = runBlocking { repo.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(pendingEnrolment!!.personUid,
                testClazz.clazzUid) }
        Assert.assertEquals("Clazz member approved is now a student", ClazzEnrolment.ROLE_STUDENT,
                clazzEnrolment?.clazzEnrolmentRole)

        runBlocking {
            val personInStudentGroup = db.personGroupMemberDao.findAllGroupWherePersonIsIn(pendingEnrolment!!.personUid).any {
                it.groupMemberGroupUid == testClazz.clazzStudentsPersonGroupUid
            }
            Assert.assertTrue("Pending member is now in student group", personInStudentGroup)
        }
    }

}
