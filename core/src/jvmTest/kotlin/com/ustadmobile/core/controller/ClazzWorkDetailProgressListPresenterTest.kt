
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzWorkContentJoinDao
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
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
class ClazzWorkDetailProgressListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzWorkDetailProgressListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var clazzWorkDaoSpy: ClazzWorkDao
    private lateinit var clazzWorkContentJoinDaoSpy: ClazzWorkContentJoinDao

    private lateinit var testClazzWork: TestClazzWork

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
        clazzWorkDaoSpy = spy(repo.clazzWorkDao)
        clazzWorkContentJoinDaoSpy = spy(repo.clazzWorkContentJoinDao)
        whenever(repo.clazzWorkDao).thenReturn(clazzWorkDaoSpy)
        whenever(repo.clazzWorkContentJoinDao).thenReturn(clazzWorkContentJoinDaoSpy)


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
            repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        val contentEntriesWithJoin = runBlocking {
            repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {

        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailProgressListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)


        verify(clazzWorkDaoSpy, timeout(5000)).findClazzWorkWithMetricsByClazzWorkUid(
                eq(testClazzWork.clazzWork.clazzWorkUid), any())
        verify(mockView, timeout(5000)).list = any()


        verify(clazzWorkDaoSpy, timeout(5000)).findStudentProgressByClazzWork(
                eq(testClazzWork.clazzWork.clazzWorkUid), eq(ClazzWorkDao.SORT_FIRST_NAME_ASC),
                any(), any())
        verify(mockView, timeout(5000)).list = any()

    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailProgressListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        val list = runBlocking {
            db.clazzWorkDao.findStudentProgressByClazzWorkTest(
                    testClazzWork.clazzWork.clazzWorkUid,
                    ClazzWorkDao.SORT_FIRST_NAME_ASC, currentTime = systemTimeInMillis())
        }

        presenter.handleClickEntry(list.get(0))

        verify(systemImpl, timeout(5000)).go(eq(ClazzWorkSubmissionMarkingView.VIEW_NAME),
                eq(mapOf(ARG_CLAZZWORK_UID to testClazzWork.clazzWork.clazzWorkUid.toString(), ARG_PERSON_UID to
                list.get(0).personUid.toString())), any())
    }


}