package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UserSessionWithPersonAndLearningSpace
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccountManager.Companion.GUEST_PERSON
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
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
            verify(mockOpenLinkUseCase).invoke("https://www.google.com/", LinkTarget.DEFAULT)
        }
    }

    @Test
    fun givenPlainViewUri_whenNavigateToLinkIsCalledWithoutForceAccountSelect_thenShouldNavigateToLinkDirectly() {
        val link = "ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndLearningSpace(
                userSession = UserSession(),
                learningSpace = LearningSpace("http://localhost:8087/"),
                person = Person()
            ))
        }
        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }


        verify(mockNavController, timeout(5000)).navigate(eq("ContentEntryList"), argWhere {
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
        verify(mockNavController, timeout(5000)).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            val nextArg = UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!)
            nextArg.substringBefore('?') == "ContentEntryList" &&
                UMFileUtil.parseURLQueryString(nextArg)["parentUid"] == "1234"
        }, any())
    }

    @Test
    fun givenDeepLinkOnSameEndpointAsActiveAccount_whenNavigateToLinkCalledWithoutForceAccountSelect_thenShouldNavigateToLinkDirectly() {
        val learningSpaceUrl = "https://school.ustadmobile.app/"
        val link = "${learningSpaceUrl}umapp/#/ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { activeLearningSpace }.thenReturn(LearningSpace(learningSpaceUrl))
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndLearningSpace(
                userSession = UserSession(),
                learningSpace = LearningSpace(learningSpaceUrl),
                person = Person()
            ))
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController, timeout(5000)).navigate(eq("ContentEntryList"), argWhere {
            it["parentUid"] == "1234"
        }, any())
    }

    @Test
    fun givenDeepLinkOnSameEndpointAsActiveAccount_whenNavigateToLinkCalledWithForceAccountSelect_thenShouldNavigateToAccountList() {
        val learningSpaceUrl = "https://school.ustadmobile.app/"
        val link = "${learningSpaceUrl}umapp/#/ContentEntryList?parentUid=1234"
        mockAccountManager.stub {
            on { activeLearningSpace }.thenReturn(LearningSpace(learningSpaceUrl))
            on { currentUserSession }.thenReturn(UserSessionWithPersonAndLearningSpace(
                userSession = UserSession(),
                learningSpace = LearningSpace(learningSpaceUrl),
                person = Person()
            ))
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(1)
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase,
                forceAccountSelection = true)
        }
        verify(mockNavController, timeout(5000)).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListViewModel.ARG_FILTER_BY_LEARNINGSPACE]!!) == learningSpaceUrl
        }, any())
    }

    @Test
    fun givenDeepLinkOnDifferentEndpointToActiveAccount_whenStoredAccountAvailableOnSameEndpoint_thenShouldNavigateToAccountListWithFilter() {
        val linkEndpointUrl = "https://school.ustadmobile.app/"
        val link = "${linkEndpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        val activeLearningSpaceUrl = "https://mmu.ustadmobile.app/"

        mockAccountManager.stub {
            on { activeLearningSpace }.thenReturn(LearningSpace(activeLearningSpaceUrl))
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                1
            }
            on { currentUserSession }.thenReturn(
                UserSessionWithPersonAndLearningSpace(
                    userSession = UserSession().apply {
                        usSessionType= UserSession.TYPE_STANDARD
                        usStatus = UserSession.STATUS_ACTIVE
                    },
                    person = GUEST_PERSON,
                    learningSpace = LearningSpace(activeLearningSpaceUrl),
                )
            )
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController, timeout(5000)).navigate(eq(AccountListViewModel.DEST_NAME), argWhere { args ->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            } && UMURLEncoder.decodeUTF8(args[AccountListViewModel.ARG_FILTER_BY_LEARNINGSPACE]!!) == linkEndpointUrl
        }, any())
    }

    @Test
    fun givenDeepLink_whenNoStoredAccountsAvailableOnSameEndpoint_thenShouldNavigateToLogin() {
        val linkEndpointUrl = "https://school.ustadmobile.app/"
        val link = "${linkEndpointUrl}umapp/#/ContentEntryList?parentUid=1234"
        val activeEndpointUrl = "https://mmu.ustadmobile.app/"

        mockAccountManager.stub {
            on { activeLearningSpace }.thenReturn(LearningSpace(activeEndpointUrl))
            on { currentUserSession }.thenReturn(
                UserSessionWithPersonAndLearningSpace(
                    userSession = UserSession().apply {
                        usSessionType = UserSession.TYPE_TEMP_LOCAL or UserSession.TYPE_GUEST
                    },
                    person = GUEST_PERSON,
                    learningSpace = LearningSpace(activeEndpointUrl)
                )
            )
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                0L
            }
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase)
        }

        verify(mockNavController, timeout(5000)).navigate(eq(LoginViewModel.DEST_NAME), argWhere { args ->
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
            on { activeLearningSpace }.thenReturn(LearningSpace(""))
            onBlocking { activeSessionCount(any(), any()) }.thenReturn(0)
        }

        runBlocking {
            mockNavController.navigateToLink(link, mockAccountManager, mockOpenLinkUseCase,
                userCanSelectServer = false, forceAccountSelection = true)
        }

        verify(mockNavController, timeout(5000)).navigate(eq(LoginViewModel.DEST_NAME), argWhere { args->
            UMURLEncoder.decodeUTF8(args[UstadView.ARG_NEXT]!!).let {
                it.substringBefore("?") == "ContentEntryList" &&
                    UMFileUtil.parseURLQueryString(it)["parentUid"] == "1234"
            }
        }, any())
    }

}