package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.BrowserLinkOpener
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class NavControllerExtTest {

    private lateinit var mockNavController: UstadNavController

    private lateinit var mockAccountManager: UstadAccountManager

    private lateinit var mockBrowserLinkOpener: BrowserLinkOpener

    @Before
    fun setup(){
        mockBrowserLinkOpener = mock { }
        mockNavController = mock { }
        mockAccountManager = mock { }
    }

    @Test
    fun givenNonUstadLink_whenNavigateToLinkIsCalled_thenShouldCallOpenInBrowser() {
        runBlocking {
            mockNavController.navigateToLink("https://www.google.com/",
                mockAccountManager, mockBrowserLinkOpener)
            verify(mockBrowserLinkOpener).onOpenLink("https://www.google.com/")
        }
    }

    @Test
    fun givenPlainViewUri_whenNavigateToLinkIsCalledWithoutForceAccountSelect_thenShouldNavigateToLinkDirectly() {
        val link = "ContentEntryList?parentUid=1234"
        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener)
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
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener,
                forceAccountSelection = true)
        }
        verify(mockNavController).navigate(eq(AccountListView.VIEW_NAME), argWhere { args ->
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
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener)
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
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(1)
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener,
                forceAccountSelection = true)
        }
        verify(mockNavController).navigate(eq(AccountListView.VIEW_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListView.ARG_FILTER_BY_ENDPOINT]!!) == endpointUrl
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
                val filter = it.arguments[1] as UstadAccountManager.EndpointFilter
                listOf(activeEndpointUrl, linkEndpointUrl).count { filter.filterEndpoint(it) }
            }
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener)
        }

        verify(mockNavController).navigate(eq(AccountListView.VIEW_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListView.ARG_FILTER_BY_ENDPOINT]!!) == linkEndpointUrl
        }, any())
    }

    @Test
    fun givenDeepLink_whenNoStoredAccountsAvailableOnSameEndpoint_thenShouldNavigateToLogin() {
        val linkEndpointUrl = "https://school.ustadmobile.app/"
        val link = "${linkEndpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        val activeEndpointUrl = "https://mmu.ustadmobile.app/"

        mockAccountManager.stub {
            on { activeEndpoint }.thenReturn(Endpoint(activeEndpointUrl))
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                val filter = it.arguments[1] as UstadAccountManager.EndpointFilter
                listOf(activeEndpointUrl).count { filter.filterEndpoint(it) }
            }
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener)
        }

        verify(mockNavController).navigate(eq(Login2View.VIEW_NAME), argWhere { args ->
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
            mockNavController.navigateToLink(link, mockAccountManager, mockBrowserLinkOpener,
                userCanSelectServer = false, forceAccountSelection = true)
        }

        verify(mockNavController).navigate(eq(Login2View.VIEW_NAME), argWhere { args->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            }
        }, any())
    }

    /**
     *
     */
    fun givenViewUri_whenUserCanSelectServer_thenShouldNavigateToEnterSiteLink() {

    }

}