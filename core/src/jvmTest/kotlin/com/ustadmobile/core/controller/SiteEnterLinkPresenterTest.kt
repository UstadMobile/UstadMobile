package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.util.test.rules.CoroutineDispatcherRule
import com.ustadmobile.util.test.rules.bindPresenterCoroutineRule
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.json.*
import io.ktor.serialization.gson.*
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.mockito.ArgumentMatchers


class SiteEnterLinkPresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: SiteEnterLinkView

    private lateinit var presenter:SiteEnterLinkPresenter

    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    private val defaultTimeout: Long = 5000

    private lateinit var di: DI

    @JvmField
    @Rule
    val presenterScopeRule  = CoroutineDispatcherRule()

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
            bind<HttpClient>() with singleton {
                HttpClient() {
                    install(ContentNegotiation) {
                        gson()
                    }
                    install(HttpTimeout)
                }
            }
            bindPresenterCoroutineRule(presenterScopeRule)
        }

        presenter = SiteEnterLinkPresenter(context, mapOf(), view, di)
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun givenValidWorkSpaceLink_whenCheckedAndIsValid_shouldAllowToGoToNextScreen() {
        val workSpace = Json.encodeToString(Site.serializer(), Site().apply {
                    siteName = "Dummy site"
                    registrationAllowed = true
                    guestLogin = true
                })

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(workSpace.toByteArray())))

        val workSpacelink = "${mockWebServer.url("/")}"

        val presenter = SiteEnterLinkPresenter(context,
                mapOf(), view, di)

        presenter.handleCheckLinkText(workSpacelink)
        verify(view, timeout(defaultTimeout)).validLink = eq(true)
    }

    @Test
    fun givenInValidWorkSpaceLink_whenCheckedAndIsValid_shouldNotAllowToGoToNextScreen() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        val workSpacelink = "${mockWebServer.url("/")}"
        val presenter = SiteEnterLinkPresenter(context,
                mapOf(), view, di)

        presenter.handleCheckLinkText(workSpacelink)
        verify(view, timeout(defaultTimeout)).validLink = eq(false)
    }

}