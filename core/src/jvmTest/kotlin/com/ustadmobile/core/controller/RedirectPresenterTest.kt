package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import org.mockito.kotlin.*
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.util.test.rules.CoroutineDispatcherRule
import com.ustadmobile.util.test.rules.bindPresenterCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class RedirectPresenterTest {

    private lateinit var mockedView: RedirectView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var mPresenter: RedirectPresenter

    private val context = Any()

    private lateinit var di: DI

    private lateinit var mockedAccountManager: UstadAccountManager

    @JvmField
    @Rule
    val dispatcherRule = CoroutineDispatcherRule()

    private val userSession = UserSessionWithPersonAndEndpoint(UserSession().apply {
        usUid = 1
        usStatus = UserSession.STATUS_ACTIVE
    },
    Person().apply {
        firstNames = "test"
        lastName = "user"
        username = "testuser"
    },
    Endpoint("https://app.ustadmobile.com/")
    )

    @Before
    fun setup() {
        mockedView = mock()
        impl = mock()

        mockedAccountManager = mock { }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UstadAccountManager>() with singleton { mockedAccountManager }

            bindPresenterCoroutineRule(dispatcherRule)
        }
    }

    @Test
    fun givenAppLaunched_whenUserHasNotLoggedInBefore_thenShouldNavigateToGetStarted() {
        whenever(impl.getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())).thenReturn(true)
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(0)
        }

        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).go(eq(SiteEnterLinkView.VIEW_NAME), any(), any(), any())
    }

    @Test
    fun givenAppLaunched_whenUserHasLoggedInBefore_thenShouldNavigateFeedList() {
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(1)
            on { activeSession }.thenReturn(userSession)
        }

        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).goToViewLink(eq(ContentEntryList2View.VIEW_NAME_HOME), any(), any())
    }

    @Test
    fun givenNextArgProvided_whenOnCreateCalled_thenShouldGoToNextDest() {
        val viewLink = "${ContentEntryDetailView.VIEW_NAME}?entityUid=42"
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(1)

        }

        mPresenter = RedirectPresenter(context, mapOf(ARG_NEXT to viewLink),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).goToViewLink(eq(viewLink), any(), any())
    }

}