
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.db.dao.ClazzWorkSubmissionDao
import com.ustadmobile.core.util.*
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzWorkSubmissionMarkingPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzWorkSubmissionMarkingView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzWorkDaoSpy: ClazzWorkDao
    private lateinit var repoClazzWorkSubmissionDaoSpy: ClazzWorkSubmissionDao

    lateinit var testClazzWork: TestClazzWork

    private lateinit var di: DI
    private lateinit var db: UmAppDatabase
    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        di = DI {
            import(ustadTestRule.diModule)
        }

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        context = Any()
        repoClazzWorkDaoSpy = spy(repo.clazzWorkDao)
        repoClazzWorkSubmissionDaoSpy = spy(repo.clazzWorkSubmissionDao)
        whenever(repo.clazzWorkDao).thenReturn(repoClazzWorkDaoSpy)
        whenever(repo.clazzWorkSubmissionDao).thenReturn(repoClazzWorkSubmissionDaoSpy)



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
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, -1,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false)
        }

        //Add content
        runBlocking {
            db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }

        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()
        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        accountManager.activeAccount.personUid = teacherMember.clazzMemberPersonUid
    }

    @Test
    fun givenSubmissionExists_whenLoaded_shouldLoadAllOk() {

        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val presenterArgs = mapOf(ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                                ARG_CLAZZMEMBER_UID to clazzMemberUid.toString())
        val presenter = ClazzWorkSubmissionMarkingPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
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
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val member = runBlocking {
            db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid, clazzMemberUid)
        }

        member?.submission?.clazzWorkSubmissionScore = 42
        whenever(mockView.entity).thenReturn(member)
        Thread.sleep(1000)
        presenter.handleClickSaveAndMarkNext(false)
        Thread.sleep(1000)
        val memberPost = runBlocking {
            db.clazzWorkDao.findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid, clazzMemberUid)
        }

        Assert.assertEquals("Saving marking OK", memberPost?.submission?.clazzWorkSubmissionScore, 42)

    }


}