package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LearnerGroupMemberDao
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntilAsyncOrTimeout
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*

class LearnerGroupMemberListPresenterTest {

    private lateinit var entryOpener: ContentEntryOpener

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: LearnerGroupMemberListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoLearnerGroupMemberDaoSpy: LearnerGroupMemberDao

    private lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        entryOpener = mock()
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContentEntryOpener>() with singleton { entryOpener }
        }
        accountManager = di.direct.instance()

        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        repoLearnerGroupMemberDaoSpy = spy(repo.learnerGroupMemberDao)
        whenever(repo.learnerGroupMemberDao).thenReturn(repoLearnerGroupMemberDaoSpy)

        Person().apply {
            personUid = 1
            admin = true
            firstNames = "Test"
            lastName = "Teacher"
            repo.personDao.insert(this)
        }

        ContentEntry().apply {
            contentEntryUid = 1
            repo.contentEntryDao.insert(this)
        }

        LearnerGroup().apply {
            learnerGroupUid = 1
            learnerGroupName = "Test"
            repo.learnerGroupDao.insert(this)
        }

        LearnerGroupMember().apply {
            learnerGroupMemberLgUid = 1
            learnerGroupMemberPersonUid = 1
            learnerGroupMemberRole = LearnerGroupMember.PRIMARY_ROLE
            repo.learnerGroupMemberDao.insert(this)
        }

        GroupLearningSession().apply {
            groupLearningSessionUid = 1
            groupLearningSessionContentUid = 1
            groupLearningSessionLearnerGroupUid = 1
            repo.groupLearningSessionDao.insert(this)
        }

    }

    @Test
    fun givenPresenterNotCreated_whenOnCreate_thenShowListOfMembers() {

        val presenterArgs = mapOf<String, String>(ARG_LEARNER_GROUP_UID to "1", ARG_CONTENT_ENTRY_UID to "1")
        val presenter = LearnerGroupMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        runBlocking {
            verify(repoLearnerGroupMemberDaoSpy, timeout(5000))
                    .findLearnerGroupMembersByGroupIdAndEntry(1, 1)
        }

        verify(mockView, timeout(5000)).list = any()

    }

    @Test
    fun givenUserIsTeacher_whenAddingNewMember_thenAddedToList() {

        val presenterArgs = mapOf<String, String>(ARG_LEARNER_GROUP_UID to "1", ARG_CONTENT_ENTRY_UID to "1")
        val presenter = LearnerGroupMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        val person = Person().apply {
            personUid = 2
            firstNames = "ustad"
            lastName = "mobile"
            repo.personDao.insert(this)
        }

        presenter.onCreate(null)

        runBlocking {
            verify(repoLearnerGroupMemberDaoSpy, timeout(5000))
                    .findLearnerGroupMembersByGroupIdAndEntry(1, 1)
        }

        verify(mockView, timeout(5000)).list = any()

        presenter.handleNewMemberToGroup(person)

        runBlocking {
            repo.waitUntilAsyncOrTimeout(5000, listOf("LearnerGroupMember")) {
                repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntryList(
                    1, 1).size == 2
            }
        }

        runBlocking {
            val list = repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntryList(1, 1)
            assertEquals("member added", 2, list.size)
            assertEquals("new member in the list", "ustad mobile", list[1].person!!.fullName())
        }

    }

    @Test
    fun givenMembersSelected_whenClickedDone_thenNavigateToContentScreen() {

        val presenterArgs = mapOf<String, String>(ARG_LEARNER_GROUP_UID to "1", ARG_CONTENT_ENTRY_UID to "1")
        val presenter = LearnerGroupMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        runBlocking {
            verify(repoLearnerGroupMemberDaoSpy, timeout(5000))
                    .findLearnerGroupMembersByGroupIdAndEntry(1, 1)
        }

        verify(mockView, timeout(5000)).list = any()

        presenter.handleClickGroupSelectionDone()

        runBlocking {
            verify(entryOpener, timeout(5000)).openEntry(context, 1,
                    true, false, false,
                    1)
        }


    }


}