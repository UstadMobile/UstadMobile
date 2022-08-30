package com.ustadmobile.core.controller


import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LeavingReasonDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance


class LeavingReasonEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: LeavingReasonEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoLeavingReasonDaoSpy: LeavingReasonDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoLeavingReasonDaoSpy = spy(repo.leavingReasonDao)
        whenever(repo.leavingReasonDao).thenReturn(repoLeavingReasonDaoSpy)


    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val presenterArgs = mapOf<String, String>()
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenter = LeavingReasonEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity.leavingReasonTitle = "Moved Aboard"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntil(5000, listOf("LeavingReason")) {
                repo.leavingReasonDao.findAllReasonsLive().size == 1
            }
        }

        val entitySaved = repo.leavingReasonDao.findAllReasonsLive()[0]
        Assert.assertEquals("Entity was saved to database", "Moved Aboard",
                entitySaved.leavingReasonTitle)


    }

    @Test
    fun givenExistingLeavingReason_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = LeavingReason().apply {
            leavingReasonTitle = "Moved"
            leavingReasonUid = repo.leavingReasonDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.leavingReasonUid.toString())
        val presenter = LeavingReasonEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.leavingReasonTitle = "Moved Aboard"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            runBlocking {
                repo.waitUntil(5000, listOf("LeavingReason")) {
                    repo.leavingReasonDao.findAllReasonsLive().size == 1
                }
            }

            val reason = repo.leavingReasonDao.findByUidAsync(testEntity.leavingReasonUid)
            Assert.assertEquals("Name was saved and updated",
                    "Moved Aboard", reason!!.leavingReasonTitle)

        }
    }


}
