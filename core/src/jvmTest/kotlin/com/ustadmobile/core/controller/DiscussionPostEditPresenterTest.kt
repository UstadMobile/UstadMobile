package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DiscussionPostDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
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

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoDiscussionPostDaoSpy: DiscussionPostDao

    private lateinit var di: DI

    var loggedInTestUser: Person? = null

    @Before
    fun setup(){

        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
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