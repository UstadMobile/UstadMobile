package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Assert
import org.junit.Test


class AddCustomFieldOptionDialogPresenterTest : AbstractSetup() {

    lateinit var systemImplSpy: UstadMobileSystemImpl


    @Before
    fun setUp() {
        checkJndiSetup()
        val impl = UstadMobileSystemImpl.instance

        val db = UmAppDatabase.getInstance(Any())

        //do inserts
        insert(db, true)

        //Set active logged in account
        UmAccountManager.setActiveAccount(umAccount!!, Any(), impl)
        systemImplSpy = spy(impl)

    }

    @After
    fun tearDown() {
    }


    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
            : Pair<AddCustomFieldOptionDialogView, AddCustomFieldOptionDialogPresenter> {
        val mockView = mock<AddCustomFieldOptionDialogView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = AddCustomFieldOptionDialogPresenter(mockContext,
                presenterArgs, mockView)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreatedWithOptionUid_whenLoaded_shouldSetOptionValueOnView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreatedWithoutOptionUid_whenLoaded_shouldInsertNewOptionAndSetOnView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenLoadedAndHandleClickOkClicked_shouldUpdateOptionAndPersistAndFinishView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenLoadedAndSetOptionValueCalledWithOptionString_shouldUpdateOptionValue() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }
}