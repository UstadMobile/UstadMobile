package com.ustadmobile.core.viewmodel.login

import app.cash.turbine.test
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class LoginViewModelTest : AbstractMainDispatcherTest(){

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
            onBlocking { login(eq(validUsername), eq(validPassword), any(), any(), any()) }.thenAnswer {
                val url = it.arguments[2] as String
                UmAccount(personUid = 42,
                    username = VALID_USER, firstName = "user", lastName = "last", endpointUrl = url)
            }

            on { activeLearningSpace }.thenReturn(LearningSpace("http://localhost:8087/"))
        }
    }

    @Test
    fun givenGuestConnectionAllowedOrNot_whenCreated_thenGuestButtonVisibiltyShouldMatch() {
        listOf(true, false).forEach { testGuestAllowed ->
            testViewModel<LoginViewModel> {
                viewModelFactory {
                    savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = "http://localhost:8087/"
                    savedStateHandle[UstadView.ARG_SITE] = json.encodeToString(Site().apply {
                        guestLogin = testGuestAllowed
                    })
                    LoginViewModel(di, savedStateHandle)
                }


                val stateFlow = stateInViewModelScope(viewModel.uiState)
                stateFlow.filter { it.connectAsGuestVisible == testGuestAllowed }.test {
                    assertEquals(testGuestAllowed, awaitItem().connectAsGuestVisible,
                        "guest connection visibility matches")
                }
            }
        }
    }

    @Test
    fun givenValidUsernameAndPassword_whenFromDestinationArgumentIsProvidedAndHandleLoginClicked_shouldGoToNextScreenAndInvalidateSync() {
        val nextDestination = "nextDummyDestination"
        val mockAccountManager = mockAccountManager()

        testViewModel<LoginViewModel>() {
            extendDi {
                bind<UstadAccountManager>(overrides = true) with singleton {
                    mockAccountManager
                }
            }

            viewModelFactory {
                mockWebServer.start()
                mockWebServer.enqueueSiteResponse(Site())

                savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = mockWebServer
                    .url("/").toString()
                savedStateHandle[UstadView.ARG_NEXT] = nextDestination
                LoginViewModel(di, savedStateHandle)
            }

            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.assertItemReceived { it.fieldsEnabled }

            viewModel.onUsernameChanged(VALID_USER)
            viewModel.onPasswordChanged(VALID_PASS)

            stateFlow.assertItemReceived { it.username == VALID_USER && it.password == VALID_PASS }

            viewModel.navCommandFlow.filter {
                (it as? NavigateNavCommand)?.viewName == nextDestination && it.goOptions.clearStack
            }.test(name = "Receive navigate to next destination command", timeout = 5.seconds) {
                viewModel.onClickLogin()
                assertNotNull(awaitItem())
            }

            verifyBlocking(mockAccountManager, timeout(5000)) {
                login(VALID_USER, VALID_PASS, mockWebServer.url("/").toString())
            }
        }

    }


    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val mockAccountManager: UstadAccountManager = mock {
            onBlocking { login(any(), any(), any(), any(), any()) }.then {
                throw UnauthorizedException("Access denied")
            }
            on { activeLearningSpace }.thenReturn(LearningSpace("http://localhost:8087/"))
        }

        testViewModel<LoginViewModel> {
            extendDi {
                bind<UstadAccountManager>(overrides = true) with singleton {
                    mockAccountManager
                }
            }

            viewModelFactory {
                mockWebServer.start()
                mockWebServer.enqueueSiteResponse(Site())
                savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }

            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.assertItemReceived { it.fieldsEnabled }

            viewModel.onUsernameChanged(VALID_USER)
            viewModel.onPasswordChanged("wrongpass")

            viewModel.onClickLogin()

            val systemImpl: UstadMobileSystemImpl = di.direct.instance()

            val expectedErrMsg = systemImpl.getString(MR.strings.wrong_user_pass_combo)

            stateFlow.filter { it.errorMessage  == expectedErrMsg }.test(
                name = "wait for expected error message"
            ) {
                val state = awaitItem()
                assertEquals(expectedErrMsg, state.errorMessage,
                    "Got expected error message",)
                assertTrue(state.fieldsEnabled, "Fields are re-enabled", )
            }
        }
    }

    @Test
    fun givenServerOffline_whenCreated_thenShouldShowErrorMessage() {
        val mockAccountManager: UstadAccountManager = mock {
            onBlocking { login(any(), any(), any(), any(), any()) }.then {
                throw IOException("Server offline")
            }
            on { activeLearningSpace }.thenReturn(LearningSpace("http://localhost:79/"))
        }


        testViewModel<LoginViewModel> {
            extendDi {
                bind<UstadAccountManager>(overrides = true) with singleton {
                    mockAccountManager
                }
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = "http://localhost:79/"
                LoginViewModel(di, savedStateHandle)
            }

            val stateFlow = stateInViewModelScope(viewModel.uiState)
            val systemImpl: UstadMobileSystemImpl by di.instance()
            val expectedErr = systemImpl.getString(MR.strings.login_network_error)

            stateFlow.filter { it.errorMessage  == expectedErr }.test {
                val state = awaitItem()
                assertEquals(expectedErr, state.errorMessage)
                assertFalse(state.fieldsEnabled, "fields not enabled when server cannot be reached")
            }
        }
    }

    @Test
    fun givenUsernameOrPasswordContainsSpacePadding_whenLoginCalled_thenShouldTrimSpace() {
        testViewModel<LoginViewModel> {
            extendDi {
                bind<UstadAccountManager>(overrides = true) with singleton {
                    mockAccountManager()
                }
            }

            viewModelFactory {
                mockWebServer.start()
                mockWebServer.enqueueSiteResponse(Site())

                savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }

            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.assertItemReceived { it.fieldsEnabled }

            viewModel.onUsernameChanged(" $VALID_USER ")
            viewModel.onPasswordChanged(" $VALID_PASS ")

            stateFlow.assertItemReceived { it.username == " $VALID_USER " && it.password == " $VALID_PASS " }

            viewModel.onClickLogin()
            val accountManager: UstadAccountManager = di.direct.instance()

            verifyBlocking(accountManager, timeout(5000)) {
                login(VALID_USER, VALID_PASS, mockWebServer.url("/").toString())
            }
        }
    }

    @Test
    fun givenEmptyUsernameAndPassword_whenLoginCalled_thenShouldShowError() {
        testViewModel<LoginViewModel> {
            viewModelFactory {
                mockWebServer.start()
                mockWebServer.enqueueSiteResponse(Site())
                savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] = mockWebServer
                    .url("/").toString()
                LoginViewModel(di, savedStateHandle)
            }

            val stateFlow = stateInViewModelScope(viewModel.uiState)
            stateFlow.assertItemReceived { it.fieldsEnabled }

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