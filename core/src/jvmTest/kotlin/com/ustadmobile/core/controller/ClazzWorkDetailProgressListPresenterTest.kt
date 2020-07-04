
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.dao.ClazzWorkContentJoinDao
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzWorkDetailProgressListPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzWorkDetailProgressListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var clazzWorkDaoSpy: ClazzWorkDao
    private lateinit var clazzWorkContentJoinDaoSpy: ClazzWorkContentJoinDao

    private lateinit var testClazzWork: TestClazzWork

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        clazzWorkDaoSpy = spy(clientDbRule.db.clazzWorkDao)
        clazzWorkContentJoinDaoSpy = spy(clientDbRule.db.clazzWorkContentJoinDao)
        whenever(clientDbRule.db.clazzWorkDao).thenReturn(clazzWorkDaoSpy)
        whenever(clientDbRule.db.clazzWorkContentJoinDao).thenReturn(clazzWorkContentJoinDaoSpy)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        testClazzWork = runBlocking {
            clientDbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        val contentEntriesWithJoin = runBlocking {
            clientDbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {

        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailProgressListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        GlobalScope.launch {
            verify(clazzWorkDaoSpy, timeout(5000)).findClazzWorkWithMetricsByClazzWorkUidAsync(
                    testClazzWork.clazzWork.clazzWorkUid)
            verify(mockView, timeout(5000)).list = any()
        }

        verify(clazzWorkContentJoinDaoSpy, timeout(5000)).findAllContentByClazzWorkUid(
                testClazzWork.clazzWork.clazzWorkUid, 0)
        verify(mockView, timeout(5000)).list = any()

        verify(clazzWorkDaoSpy, timeout(5000)).findStudentProgressByClazzWork(
                testClazzWork.clazzWork.clazzWorkUid)
        verify(mockView, timeout(5000)).list = any()

    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailProgressListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        val list = runBlocking {
            clientDbRule.db.clazzWorkDao.findStudentProgressByClazzWorkTest(
                    testClazzWork.clazzWork.clazzWorkUid)
        }

        presenter.handleClickEntry(list.get(0))

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ClazzWorkSubmissionMarkingView.VIEW_NAME),
                eq(mapOf(ARG_CLAZZWORK_UID to testClazzWork.clazzWork.clazzWorkUid.toString(), ARG_CLAZZMEMBER_UID to
                list.get(0).mClazzMember?.clazzMemberUid.toString())), any())
    }


}