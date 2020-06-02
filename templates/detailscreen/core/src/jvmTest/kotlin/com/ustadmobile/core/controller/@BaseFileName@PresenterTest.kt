
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.core.view.@Entity@DetailView
import com.ustadmobile.core.view.@Entity@EditView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.@Entity@Dao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class @BaseFileName@PresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: @BaseFileName@View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repo@Entity@DaoSpy: @Entity@Dao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repo@Entity@DaoSpy = spy(clientDbRule.db.@Entity_VariableName@Dao)
        whenever(clientDbRule.db.@Entity_VariableName@Dao).thenReturn(repo@Entity@DaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun given@Entity@Exists_whenOnCreateCalled_then@Entity@IsSetOnView() {
        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = clientDbRule.db.@Entity_VariableName@Dao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())
        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)


        presenter.onCreate(null)


        nullableArgumentCaptor<@DisplayEntity@>().apply {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()

            Assert.assertEquals("Expected entity was set on view",
                    testEntity.@Entity_VariableName@Uid, lastValue!!.@Entity_VariableName@Uid)
        }
    }

    @Test
    fun given@Entity@Exists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = clientDbRule.db.@Entity_VariableName@Dao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())
        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)

        presenter.onCreate(null)

        presenter.handleClickEdit()

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(@Entity@EditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())), any())
    }

}