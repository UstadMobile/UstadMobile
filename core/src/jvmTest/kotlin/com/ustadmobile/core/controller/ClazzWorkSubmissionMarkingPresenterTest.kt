
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.db.dao.ClazzWorkSubmissionDao
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzWorkSubmissionMarkingPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzWorkSubmissionMarkingView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzWorkDaoSpy: ClazzWorkDao
    private lateinit var repoClazzWorkSubmissionDaoSpy: ClazzWorkSubmissionDao

    lateinit var testClazzWork: TestClazzWork

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzWorkDaoSpy = spy(clientDbRule.db.clazzWorkDao)
        repoClazzWorkSubmissionDaoSpy = spy(clientDbRule.db.clazzWorkSubmissionDao)
        whenever(clientDbRule.db.clazzWorkDao).thenReturn(repoClazzWorkDaoSpy)
        whenever(clientDbRule.db.clazzWorkSubmissionDao).thenReturn(repoClazzWorkSubmissionDaoSpy)



        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
            clazzWorkActive = true
        }

        val dateNow: Long = UMCalendarUtil.getDateInMilliPlusDays(0)

        testClazzWork = runBlocking {
            clientDbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, -1,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false)
        }

        //Add content
        runBlocking {
            clientDbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        clientDbRule.account.personUid = teacherMember.clazzMemberPersonUid
    }

    @Test
    fun givenSubmissionExists_whenLoaded_shouldLoadAllOk() {

        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val presenterArgs = mapOf(ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                                ARG_CLAZZMEMBER_UID to clazzMemberUid.toString())
        val presenter = ClazzWorkSubmissionMarkingPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        GlobalScope.launch {
            verify(repoClazzWorkDaoSpy, timeout(5000)).findClazzMemberWithAndSubmissionWithPerson(
                    clazzWorkUid, clazzMemberUid)
            verify(mockView, timeout(5000)).entity = any()
        }

       //TODO: Complete this

    }

    @Test
    fun givenSubmissionExistsAndLoaded_whenMarked_shouldSave() {
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val presenterArgs = mapOf(ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                ARG_CLAZZMEMBER_UID to clazzMemberUid.toString())
        val presenter = ClazzWorkSubmissionMarkingPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val member = runBlocking {
            clientDbRule.db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid, clazzMemberUid)
        }

        member?.submission?.clazzWorkSubmissionScore = 42
        whenever(mockView.entity).thenReturn(member)
        presenter.handleClickSaveAndMarkNext(member, false)

        val memberPost = runBlocking {
            clientDbRule.db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid, clazzMemberUid)
        }

        Assert.assertEquals("Saving marking OK", memberPost?.submission?.clazzWorkSubmissionScore, 42)





//        val testEntity = ClazzMemberAndClazzWorkWithSubmission().apply {
//            someName = "Spelling Clazz"
//            clazzWorkSubmissionWithPersonUid = clientDbRule.repo.clazzWorkSubmissionWithPersonDao.insert(this)
//        }
//
//        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzWorkSubmissionWithPersonUid.toString())
//        val presenter = ClazzWorkSubmissionMarkingPresenter(context,
//                presenterArgs, mockView, mockLifecycleOwner,
//                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
//                clientDbRule.accountLiveData)
//        presenter.onCreate(null)
//
//        val initialEntity = mockView.captureLastEntityValue()!!
//
//        //Make some changes to the entity (e.g. as the user would do using data binding)
//        //e.g. initialEntity!!.someName = "New Spelling Clazz"
//
//        presenter.handleClickSave(initialEntity)
//
//        val entitySaved = runBlocking {
//            clientDbRule.db.clazzWorkSubmissionWithPersonDao.findByUidLive(testEntity.clazzWorkSubmissionWithPersonUid)
//                    .waitUntil(5000) { it?.someName == "New Spelling Clazz" }.getValue()
//        }
//
//        Assert.assertEquals("Name was saved and updated",
//                "New Spelling Clazz", entitySaved!!.someName)
    }


}