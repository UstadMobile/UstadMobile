package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccountManager.Companion.GUEST_PERSON
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJvm
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class NavControllerExtTest {

    private lateinit var mockNavController: UstadNavController

    private lateinit var mockAccountManager: UstadAccountManager

    private lateinit var mockOpenLinkUseCase: OpenExternalLinkUseCaseJvm

    @Before
    fun setup(){
        mockOpenLinkUseCase = mock { }
        mockNavController = mock { }
        mockAccountManager = mock { }
    }

    @Test
    fun givenNonUstadLink_whenNavigateToLinkIsCalled_thenShouldCallOpenInBrowser() {
        runBlocking {
            mockNavController.navigateToLink("https://www.google.com/",
                mockAccountManager, mockOpenLinkUseCase)
            verify(mockOpenLinkUseCase).invoke("https://www.google.com/")
        }
    }

    @Test
    fun givenPlainViewUri_whenNavigateToLinkIsCalledWithoutForceAccountSelect_thenShouldNavigateToLinkDirectly() {
        val link = "ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndEndpoint(
                userSession = UserSession(),
                endpoint = Endpoint("http://localhost:8087/"),
                person = Person()
            ))
        }
        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }


        verify(mockNavController).navigate(eq("ContentEntryList"), argWhere {
            it["parentUid"] == "1234"
        }, any())
    }

    @Test
    fun givenPlainViewUri_whenNavigateToLinkIsCalledWithForceAccountSelected_thenShouldNavigateToAccountList() {
        val link = "ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer { 1 }
        }
        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase,
                forceAccountSelection = true)
        }
        verify(mockNavController).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            val nextArg = UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!)
            nextArg.substringBefore('?') == "ContentEntryList" &&
                UMFileUtil.parseURLQueryString(nextArg)["parentUid"] == "1234"
        }, any())
    }

    @Test
    fun givenDeepLinkOnSameEndpointAsActiveAccount_whenNavigateToLinkCalledWithoutForceAccountSelect_thenShouldNavigateToLinkDirectly() {
        val endpointUrl = "https://school.ustadmobile.app/"
        val link = "${endpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(endpointUrl))
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndEndpoint(
                userSession = UserSession(),
                endpoint = Endpoint(endpointUrl),
                person = Person()
            ))
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController).navigate(eq("ContentEntryList"), argWhere {
            it["parentUid"] == "1234"
        }, any())
    }

    @Test
    fun givenDeepLinkOnSameEndpointAsActiveAccount_whenNavigateToLinkCalledWithForceAccountSelect_thenShouldNavigateToAccountList() {
        val endpointUrl = "https://school.ustadmobile.app/"
        val link = "${endpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(endpointUrl))
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndEndpoint(
                userSession = UserSession(),
                endpoint = Endpoint(endpointUrl),
                person = Person()
            ))
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(1)
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase,
                forceAccountSelection = true)
        }
        verify(mockNavController).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListViewModel.ARG_FILTER_BY_ENDPOINT]!!) == endpointUrl
        }, any())
    }

    @Test
    fun givenDeepLinkOnDifferentEndpointToActiveAccount_whenStoredAccountAvailableOnSameEndpoint_thenShouldNavigateToAccountListWithFilter() {
        val linkEndpointUrl = "https://school.ustadmobile.app/"
        val link = "${linkEndpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        val activeEndpointUrl = "https://mmu.ustadmobile.app/"

        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(activeEndpointUrl))
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                1
            }
            on { currentUserSession }.thenReturn(
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                        usSessionType= UserSession.TYPE_STANDARD
                        usStatus = UserSession.STATUS_ACTIVE
                    },
                    person = GUEST_PERSON,
                    endpoint = Endpoint(activeEndpointUrl),
                )
            )
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListViewModel.ARG_FILTER_BY_ENDPOINT]!!) == linkEndpointUrl
        }, any())
    }

    @Test
    fun givenDeepLink_whenNoStoredAccountsAvailableOnSameEndpoint_thenShouldNavigateToLogin() {
        val linkEndpointUrl = "https://school.ustadmobile.app/"
        val link = "${linkEndpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        val activeEndpointUrl = "https://mmu.ustadmobile.app/"

        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(activeEndpointUrl))
            on { currentUserSession }.thenReturn(
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                        usSessionType = UserSession.TYPE_TEMP_LOCAL or UserSession.TYPE_GUEST
                    },
                    person = GUEST_PERSON,
                    endpoint = Endpoint(activeEndpointUrl)
                )
            )
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                0L
            }
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController).navigate(eq(LoginViewModel.DEST_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[UstadView.ARG_API_URL]!!) == linkEndpointUrl
        }, any())
    }

    @Test
    fun givenViewUriLink_whenUserCannotSelectServerAndNoAccountsAreStoredAndForceAccountSelectionEnabled_thenShouldNavigateToLogin() {
        val link = "ContentEntryList?parentUid=1234"

        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(""))
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(0)
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase,
                userCanSelectServer = false, forceAccountSelection = true)
        }

        verify(mockNavController).navigate(eq(LoginViewModel.DEST_NAME), argWhere { args->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            }
        }, any())
    }

}