package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers


class WorkspaceEnterLinkPresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: WorkspaceEnterLinkView

    private lateinit var presenter:WorkspaceEnterLinkPresenter

    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    private val defaultTimeout: Long = 5000

    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        impl = mock{}
        presenter = WorkspaceEnterLinkPresenter(context, mapOf(), view, impl)
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }



    @Test
    fun givenValidWorkSpaceLink_whenCheckedAndIsValid_shouldAllowToGoToNextScreen() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val workSpacelink = "${mockWebServer.url("/")}workspace"

        whenever(view.workspaceLink).thenReturn(workSpacelink)

        val presenter = WorkspaceEnterLinkPresenter(context,
                mapOf(), view, impl)
        presenter.onCreate(null)
        presenter.checkLinkValidity()
        verify(view, timeout(defaultTimeout)).validLink = eq(true)
    }

    @Test
    fun givenInValidWorkSpaceLink_whenCheckedAndIsValid_shouldNotAllowToGoToNextScreen() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        val workSpacelink = "${mockWebServer.url("/")}workspace"

        whenever(view.workspaceLink).thenReturn(workSpacelink)

        val presenter = WorkspaceEnterLinkPresenter(context,
                mapOf(), view, impl)
        presenter.onCreate(null)
        presenter.checkLinkValidity()
        verify(view, timeout(defaultTimeout)).validLink = eq(false)
    }

}