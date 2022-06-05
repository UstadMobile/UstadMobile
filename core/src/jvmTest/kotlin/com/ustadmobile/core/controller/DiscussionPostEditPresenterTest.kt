package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DiscussionPostDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import org.junit.Rule
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DiscussionPostEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: DiscussionPostEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoDiscussionPostDaoSpy: DiscussionPostDao

    private lateinit var di: DI

    var loggedInTestUser: Person? = null

    @Before
    fun setup(){

        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoDiscussionPostDaoSpy = spy(repo.discussionPostDao)
        whenever(repo.discussionPostDao).thenReturn(repoDiscussionPostDaoSpy)


    }



    @Test
    fun g_w_t(){

    }
}