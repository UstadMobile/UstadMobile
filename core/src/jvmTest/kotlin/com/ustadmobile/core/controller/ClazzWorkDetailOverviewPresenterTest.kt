
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.db.dao.CommentsDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission
import com.ustadmobile.util.test.ext.insertTestClazzWork
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
class ClazzWorkDetailOverviewPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzWorkDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzWorkDaoSpy: ClazzWorkDao

    private lateinit var repoCommentsDaoSpy: CommentsDao

    private lateinit var di: DI

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

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        repoClazzWorkDaoSpy = spy(repo.clazzWorkDao)
        whenever(repo.clazzWorkDao).thenReturn(repoClazzWorkDaoSpy)

        repoCommentsDaoSpy = spy(repo.commentsDao)
        whenever(repo.commentsDao).thenReturn(repoCommentsDaoSpy)

    }

    @Test
    fun givenClazzWorkExists_whenOnCreateCalled_thenClazzWorkIsSetOnView() {

        val testClazzWork = runBlocking {
            repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), true, -1, true, 0,
            false, true)
        }


        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        nullableArgumentCaptor<ClazzWorkWithSubmission>().apply {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()

            Assert.assertEquals("Expected entity was set on view",
                    testClazzWork.clazzWork.clazzWorkUid, lastValue!!.clazzWorkUid)
        }

        verify(repoCommentsDaoSpy, timeout(5000).atLeastOnce()).findPublicByEntityTypeAndUidLive(
                ClazzWork.CLAZZ_WORK_TABLE_ID, testClazzWork.clazzWork.clazzWorkUid)
        verify(mockView, timeout(5000).atLeastOnce()).clazzWorkPublicComments = any()

        verify(mockView, timeout(5000).atLeastOnce()).timeZone =
                testClazzWork.clazzAndMembers.clazz.clazzTimeZone!!

        verify(mockView, timeout(5000).atLeastOnce()).isStudent = false

        val liveDataSet = nullableArgumentCaptor<DoorMutableLiveData<
                List<ClazzWorkQuestionAndOptionWithResponse>>>().run {
            verify(mockView, timeout(5000).atLeastOnce()).viewOnlyQuizQuestions = capture()
            firstValue
        }
        val valueLiveDataSet = liveDataSet?.getValue()
        Assert.assertEquals("Set Ok", valueLiveDataSet?.size, 5)
    }

    @Test
    fun givenClazzWorkExists_whenClickAddPublicComment_thenShouldPersistComment(){
        val testClazzWork = runBlocking {
            repo.insertTestClazzWork(ClazzWork())
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).entity = any()

        presenter.addComment(ClazzWork.CLAZZ_WORK_TABLE_ID, testClazzWork.clazzWork.clazzWorkUid,
                "Hello World", true, 0L,0L)

        verifyBlocking(repoCommentsDaoSpy, timeout(5000)){
            insertAsync(argThat{ this.commentsText == "Hello World"})
        }

    }

    @Test
    fun givenClazzWorkExists_whenClickSubmit_thenShouldPersistSubmissionResult(){
        val testClazzWork = runBlocking {
            repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork().apply {
                        clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
                    }, true, -1, true, 0,
                    false, true)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        nullableArgumentCaptor<ClazzWorkWithSubmission>().apply {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()

            Assert.assertEquals("Expected entity was set on view",
                    testClazzWork.clazzWork.clazzWorkUid, lastValue!!.clazzWorkUid)
        }

        val liveDataSet = nullableArgumentCaptor<DoorMutableLiveData<
                List<ClazzWorkQuestionAndOptionWithResponse>>>().run {
            verify(mockView, timeout(5000).atLeastOnce()).viewOnlyQuizQuestions = capture()
            firstValue
        }
        val valueLiveDataSet = liveDataSet?.getValue()
        Assert.assertEquals("Set Ok", valueLiveDataSet?.size, 5)

        presenter.handleClickSubmit()

        verify(mockView, timeout(20000).atLeastOnce()).entity = any()

        //Verify submission is set
        GlobalScope.launch {
            val postSubmission = db.clazzWorkSubmissionDao.findByClazzWorkUidAsync(
                    testClazzWork.clazzWork.clazzWorkUid)

            Assert.assertEquals("Submission set",
                    postSubmission.first().clazzWorkSubmissionClazzWorkUid,
                    testClazzWork.clazzWork.clazzWorkUid)
        }
    }


    @Test
    fun givenClazzWorkExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val testEntity = ClazzWork().apply {
            //set variables here
            clazzWorkUid = repo.clazzWorkDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzWorkUid.toString())
        val presenter = ClazzWorkDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickEdit()

        verify(systemImpl, timeout(5000)).go(eq(ClazzWorkEditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.clazzWorkUid.toString())), any())
    }

}