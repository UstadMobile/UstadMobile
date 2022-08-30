
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.@BaseFileName@View
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.@Entity@Dao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.lifecycle.LifecycleObserver

import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.@Entity@
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class @BaseFileName@PresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: @BaseFileName@View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repo@Entity@DaoSpy: @Entity@Dao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(LifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()


        repo@Entity@DaoSpy = spy(repo.@Entity_VariableName@Dao)
        whenever(repo.@Entity_VariableName@Dao).thenReturn(repo@Entity@DaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun given@Entity@Exists_whenOnCreateCalled_then@Entity@IsSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = repo.@Entity_VariableName@Dao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())

        val presenter = @BaseFileName@Presenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.@Entity_VariableName@Uid, entityValSet.@Entity_VariableName@Uid)
    }

    @Test
    fun given@Entity@Exists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = repo.@Entity_VariableName@Dao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())

        val presenter = @BaseFileName@Presenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        presenter.handleClickEdit()

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(@Entity@EditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())), any())
    }

}