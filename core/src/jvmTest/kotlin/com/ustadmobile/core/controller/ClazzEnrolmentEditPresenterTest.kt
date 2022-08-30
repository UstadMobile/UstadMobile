package com.ustadmobile.core.controller


import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzEnrolmentEditPresenterTest {

    private lateinit var activePerson: Person
    private lateinit var testClazz: Clazz

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzEnrolmentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzEnrolmentDaoSpy: ClazzEnrolmentDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoClazzEnrolmentDaoSpy = spy(repo.clazzEnrolmentDao)
        whenever(repo.clazzEnrolmentDao).thenReturn(repoClazzEnrolmentDaoSpy)

        testClazz = Clazz("Test clazz").apply {
            clazzStartTime = DateTime(2020, 10, 10).unixMillisLong
            clazzUid = repo.clazzDao.insert(this)
        }

        val existingClazz = repo.clazzDao.findByUid(testClazz.clazzUid)
        println(existingClazz)

        activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }

    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val presenterArgs = mapOf<String, String>(
                UstadView.ARG_PERSON_UID to activePerson.personUid.toString(),
                UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString(),
                UstadView.ARG_SAVE_TO_DB to true.toString())
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenter = ClazzEnrolmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue(timeoutMillis = 5000)!!

        initialEntity.clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntil(5000, listOf("ClazzEnrolment")) {
                runBlocking {
                    repo.clazzEnrolmentDao.findAllClazzesByPersonWithClazzAsListAsync(
                            activePerson.personUid).isNotEmpty()
                }
            }
        }

        runBlocking {
            val list = repo.clazzEnrolmentDao.findAllClazzesByPersonWithClazzAsListAsync(
                    activePerson.personUid)
            Assert.assertEquals("Entity was saved to database", ClazzEnrolment.ROLE_STUDENT,
                    list[0].clazzEnrolmentRole)
        }


    }

    @Test
    fun givenExistingLeavingReason_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = ClazzEnrolment().apply {
            clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
            clazzEnrolmentDateLeft = Long.MAX_VALUE
            clazzEnrolmentPersonUid = activePerson.personUid
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentDateJoined = DateTime(2020,10,11).unixMillisLong
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val presenterArgs = mapOf<String, String>(
                UstadView.ARG_PERSON_UID to activePerson.personUid.toString(),
                UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString(),
                UstadView.ARG_SAVE_TO_DB to true.toString(),
                UstadView.ARG_ENTITY_UID to testEntity.clazzEnrolmentUid.toString())
        val presenter = ClazzEnrolmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.clazzEnrolmentDateLeft = DateTime(2021, 10, 10).unixMillisLong
        initialEntity.clazzEnrolmentLeavingReasonUid = LeavingReason.FAMILY_PROBLEM_UID
        initialEntity.clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_FAILED

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntil(120000, listOf("ClazzEnrolment")) {
                runBlocking {
                    repo.clazzEnrolmentDao.findByUid(testEntity.clazzEnrolmentUid)?.clazzEnrolmentLeavingReasonUid == LeavingReason.FAMILY_PROBLEM_UID
                }
            }
        }

        runBlocking {
            val enrolment = repo.clazzEnrolmentDao.findByUid(testEntity.clazzEnrolmentUid)
            Assert.assertEquals("Entity was updated to database", LeavingReason.FAMILY_PROBLEM_UID,
                    enrolment?.clazzEnrolmentLeavingReasonUid)
        }

    }


}
