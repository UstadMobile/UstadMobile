package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddActivityChangeDialogView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Assert
import org.junit.Test


class AddActivityChangeDialogPresenterTest : AbstractSetup() {

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
            : Pair<AddActivityChangeDialogView, AddActivityChangeDialogPresenter> {
        val mockView = mock<AddActivityChangeDialogView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = AddActivityChangeDialogPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenLoaded_shouldLoadMeasurementTypesPresets() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenHandleActivityChangeCalled_shouldPersistActivityChangeAndCallFinish() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenCancelActivityChangeCalled_shouldNullActivityChange() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenHandleMeasurementSelectedCalledWithType_shouldUpdateUnitOfMeasurement() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenHandleTitleChangeCalled_shouldUpdateTitle() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }
}