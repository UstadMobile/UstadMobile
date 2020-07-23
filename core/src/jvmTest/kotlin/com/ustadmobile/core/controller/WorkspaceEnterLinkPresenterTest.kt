package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import com.ustadmobile.lib.db.entities.WorkSpace
import io.ktor.http.ContentType
import io.ktor.http.cio.HttpHeadersMap
import kotlinx.serialization.json.Json
import okhttp3.internal.http.HttpHeaders
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.mockito.ArgumentMatchers


class WorkspaceEnterLinkPresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: WorkspaceEnterLinkView

    private lateinit var presenter:WorkspaceEnterLinkPresenter

    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    private val defaultTimeout: Long = 5000

    private lateinit var di: DI

    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        impl = mock{}
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
        }

        presenter = WorkspaceEnterLinkPresenter(context, mapOf(), view, di)
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun givenValidWorkSpaceLink_whenCheckedAndIsValid_shouldAllowToGoToNextScreen() {
        val workSpace = Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
                    name = "Dummy workspace"
                    registrationAllowed = true
                    guestLogin = true
                })

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(workSpace.toByteArray())))

        val workSpacelink = "${mockWebServer.url("/")}"

        val presenter = WorkspaceEnterLinkPresenter(context,
                mapOf(), view, di)

        presenter.handleCheckLinkText(workSpacelink)
        verify(view, timeout(defaultTimeout)).validLink = eq(true)
    }

    @Test
    fun givenInValidWorkSpaceLink_whenCheckedAndIsValid_shouldNotAllowToGoToNextScreen() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        val workSpacelink = "${mockWebServer.url("/")}"
        val presenter = WorkspaceEnterLinkPresenter(context,
                mapOf(), view, di)

        presenter.handleCheckLinkText(workSpacelink)
        verify(view, timeout(defaultTimeout)).validLink = eq(false)
    }

}