
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.core.view.@Entity@DetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.@Entity@Dao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.@Entity@
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
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = clientDbRule.db.@Entity_VariableName@Dao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // @BaseFileName@View.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repo@Entity@DaoSpy, timeout(5000)).findBy@Entity@UidAsFactory()
        verify(mockView, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val testEntity = @Entity@().apply {
            //set variables here
            @Entity_VariableName@Uid = clientDbRule.db.@Entity_VariableName@Dao.insert(this)
        }
        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)


        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(@Entity@DetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())), any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}