package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.RegisterMinorWaitForParentView
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PARENT_CONTACT
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PASSWORD
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_USERNAME
import com.ustadmobile.core.view.UstadView
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.lang.IllegalArgumentException

class RegisterMinorWaitForParentPresenterTest {

    private lateinit var di: DI

    private lateinit var mockSystemImpl: UstadMobileSystemImpl

    private lateinit var mockView: RegisterMinorWaitForParentView

    @Before
    fun setup() {
        mockSystemImpl = mock { }
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { mockSystemImpl }
        }
        mockView = mock { }
    }

    @Test
    fun givenValidArgs_whenOkClicked_thenShouldPopBack() {
        val args = mapOf(
                ARG_USERNAME to "childuser",
                ARG_PASSWORD to "secret",
                ARG_PARENT_CONTACT to "parent@email.com")

        val presenter = RegisterMinorWaitForParentPresenter(Any(), args, mockView, di)
        presenter.onCreate(null)
        presenter.handleClickOk()

        verify(mockView).username = args[ARG_USERNAME] ?: throw IllegalArgumentException()

        verify(mockSystemImpl).popBack(eq(UstadView.CURRENT_DEST), eq(true), any())
    }

}