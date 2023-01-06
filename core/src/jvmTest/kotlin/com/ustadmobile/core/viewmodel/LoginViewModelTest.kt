package com.ustadmobile.core.viewmodel

import app.cash.turbine.test
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.test.viewmodeltest.awaitMatch
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Test
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import org.mockito.kotlin.*
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class LoginViewModelTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private fun MockWebServer.enqueueSiteResponse(site: Site) {
        enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(json.encodeToString(site))
            .setHeader("Content-Type", "application/json"))
    }

    private fun mockAccountManager(
        validUsername: String = VALID_USER,
        validPassword: String = VALID_PASS,
    ): UstadAccountManager {
        return mock {
            onBlocking { login(eq(validUsername), eq(validPassword), any(), any()) }.thenAnswer {
                val url = it.arguments[2] as String
                UmAccount(personUid = 42,
                    username = VALID_USER, firstName = "user", lastName = "last", endpointUrl = url)
            }
        }
    }

    @Test
    fun givenRegistrationIsAllowedOrNot_whenCreated_thenRegistrationButtonVisibilityShouldMatch() {
        listOf(true, false).forEach { testRegistrationAllowed ->
            testViewModel<LoginViewModel>(
                makeViewModel =  {
                    savedStateHandle[UstadView.ARG_SERVER_URL] = "http://localhost:8087/"
                    savedStateHandle[UstadView.ARG_SITE] = json.encodeToString(Site().apply {
                        registrationAllowed = testRegistrationAllowed
                    })
                    LoginViewModel(di, savedStateHandle)
                }
            ) {
                val stateFlow = stateInViewModelScope(viewModel.uiState)
                stateFlow.filter { it.createAccountVisible == testRegistrationAllowed }.test {
                    Assert.assertEquals("Create account button visible",
                        testRegistrationAllowed, awaitItem().createAccountVisible)
                }
            }
        }
    }

    @Test
    fun givenGuestConnectionAllowedOrNot_whenCreated_thenGuestButtonVisibiltyShouldMatch() {
        listOf(true, false).forEach { testGuestAllowed ->
            testViewModel<LoginViewModel>(
                makeViewModel = {
                    savedStateHandle[UstadView.ARG_SERVER_URL] = "http://localhost:8087/"
                    savedStateHandle[UstadView.ARG_SITE] = json.encodeToString(Site().apply {
                        guestLogin = testGuestAllowed
                    })
                    LoginViewModel(di, savedStateHandle)
                }
            ) {
                val stateFlow = stateInViewModelScope(viewModel.uiState)
                stateFlow.filter { it.connectAsGuestVisible == testGuestAllowed }.test {
                    Assert.assertEquals("guest connection visibility matches",
                        testGuestAllowed, awaitItem().connectAsGuestVisible)
                }
            }
        }
    }

    @Test
    fun givenCreateAccountVisible_whenClickCreateAccount_thenShouldNavigateToAgeRedirect() {
        testViewModel<LoginViewModel>(
            makeViewModel = {
                savedStateHandle[UstadView.ARG_SERVER_URL] = "http://localhost:8087/"
                savedStateHandle[UstadView.ARG_SITE] = json.encodeToString(Site().apply {
                    registrationAllowed = true
                })
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.awaitMatch { it.createAccountVisible }

            viewModel.onClickCreateAccount()
            verify(navController).navigate(eq(RegisterAgeRedirectView.VIEW_NAME), any(), any())
        }
    }



    @Test
    fun givenValidUsernameAndPassword_whenFromDestinationArgumentIsProvidedAndHandleLoginClicked_shouldGoToNextScreenAndInvalidateSync() {
        val nextDestination = "nextDummyDestination"
        val mockAccountManager = mockAccountManager()

        testViewModel<LoginViewModel>(
            extendDi = {
               bind<UstadAccountManager>() with singleton {
                   mockAccountManager
               }
            },
            makeViewModel = {
                testContext.mockWebServer.start()
                testContext.mockWebServer.enqueueSiteResponse(Site())

                savedStateHandle[UstadView.ARG_SERVER_URL] = testContext.mockWebServer
                    .url("/").toString()
                savedStateHandle[UstadView.ARG_NEXT] = nextDestination
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.awaitMatch { it.fieldsEnabled }

            viewModel.onUsernameChanged(VALID_USER)
            viewModel.onPasswordChanged(VALID_PASS)

            stateFlow.awaitMatch { it.username == VALID_USER && it.password == VALID_PASS }

            viewModel.onClickLogin()

            verifyBlocking(mockAccountManager, timeout(5000)) {
                login(VALID_USER, VALID_PASS, mockWebServer.url("/").toString())
            }

            verify(navController, timeout(5000)).navigate(eq(nextDestination), any(), argWhere {
                it.clearStack
            })
        }

    }


    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val mockAccountManager: UstadAccountManager = mock {
            onBlocking { login(any(), any(), any(), any()) }.then {
                throw UnauthorizedException("Access denied")
            }
        }

        testViewModel<LoginViewModel>(
            extendDi = {
               bind<UstadAccountManager>() with singleton {
                   mockAccountManager
               }
            },
            makeViewModel = {
                testContext.mockWebServer.start()
                testContext.mockWebServer.enqueueSiteResponse(Site())
                savedStateHandle[UstadView.ARG_SERVER_URL] = testContext.mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.awaitMatch { it.fieldsEnabled }

            viewModel.onUsernameChanged(VALID_USER)
            viewModel.onPasswordChanged("wrongpass")

            viewModel.onClickLogin()

            val systemImpl: UstadMobileSystemImpl = di.direct.instance()

            val expectedErrMsg = systemImpl.getString(MessageID.wrong_user_pass_combo)

            stateFlow.filter { it.errorMessage  == expectedErrMsg}.test(
                name = "wait for expected error message"
            ) {
                val state = awaitItem()
                Assert.assertEquals("Got expected error message",
                    expectedErrMsg, state.errorMessage)
                Assert.assertTrue("Fields are re-enabled", state.fieldsEnabled)
            }
        }
    }

    @Test
    fun givenServerOffline_whenCreated_thenShouldShowErrorMessage() {
        val mockAccountManager: UstadAccountManager = mock {
            onBlocking { login(any(), any(), any(), any()) }.then {
                throw IOException("Server offline")
            }
        }


        testViewModel<LoginViewModel>(
            extendDi = {
                bind<UstadAccountManager>() with singleton {
                    mockAccountManager
                }
            },
            makeViewModel = {
                savedStateHandle[UstadView.ARG_SERVER_URL] = "http://localhost:79/"
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            val systemImpl: UstadMobileSystemImpl by di.instance()
            val expectedErr = systemImpl.getString(MessageID.login_network_error)

            stateFlow.filter { it.errorMessage  == expectedErr }.test {
                val state = awaitItem()
                assertEquals(expectedErr, state.errorMessage)
                assertFalse(state.fieldsEnabled, "fields not enabled when server cannot be reached")
            }
        }
    }

    @Test
    fun givenUsernameOrPasswordContainsSpacePadding_whenLoginCalled_thenShouldTrimSpace() {
        testViewModel<LoginViewModel>(
            extendDi = {
                bind<UstadAccountManager>() with singleton {
                    mockAccountManager()
                }
            },
            makeViewModel = {
                testContext.mockWebServer.start()
                testContext.mockWebServer.enqueueSiteResponse(Site())

                savedStateHandle[UstadView.ARG_SERVER_URL] = testContext.mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.awaitMatch { it.fieldsEnabled }

            viewModel.onUsernameChanged(" $VALID_USER ")
            viewModel.onPasswordChanged(" $VALID_PASS ")

            stateFlow.awaitMatch { it.username == " $VALID_USER " && it.password == " $VALID_PASS " }

            viewModel.onClickLogin()
            val accountManager: UstadAccountManager = di.direct.instance()

            verifyBlocking(accountManager, timeout(5000)) {
                login(VALID_USER, VALID_PASS, mockWebServer.url("/").toString())
            }
        }
    }

    @Test
    fun givenEmptyUsernameAndPassword_whenLoginCalled_thenShouldShowError() {
        testViewModel<LoginViewModel>(
            makeViewModel = {
                testContext.mockWebServer.start()
                testContext.mockWebServer.enqueueSiteResponse(Site())
                savedStateHandle[UstadView.ARG_SERVER_URL] = testContext.mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }
        ) {
            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.awaitMatch { it.fieldsEnabled }

            viewModel.onClickLogin()

            stateFlow.filter { it.fieldsEnabled && it.usernameError != null }.test {
                val stateItem = awaitItem()
                assertNotNull(stateItem.usernameError)
                assertNotNull(stateItem.passwordError)
            }
        }
    }



    companion object {

        private const val VALID_USER = "JohnDoe"

        private const val VALID_PASS = "password"
    }
}