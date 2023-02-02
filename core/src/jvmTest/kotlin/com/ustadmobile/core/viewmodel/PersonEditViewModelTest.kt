package com.ustadmobile.core.viewmodel

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.PersonEditView.Companion.ARG_REGISTRATION_MODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.door.room.InvalidationTrackerObserver
import com.ustadmobile.door.room.RoomDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.kodein.di.*
import org.mockito.kotlin.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@Suppress("RemoveExplicitTypeArguments") // This is incorrect for using testViewModel
class PersonEditViewModelTest {

    @Suppress("SameParameterValue")
    private fun createMockAccountManager(serverUrl: String) : UstadAccountManager {
        return mock {
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on { activeAccount }.thenReturn(UmAccount(0L, "", "", serverUrl))
        }
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenUsernameAndPasswordNotFilledClickSave_shouldShowErrors() {
        testViewModel<PersonEditViewModel> {
            viewModelFactory {
                savedStateHandle[ARG_REGISTRATION_MODE] = PersonEditView.REGISTER_MODE_ENABLED.toString()
                PersonEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived { it.fieldsEnabled && it.person != null }

            viewModel.uiState.test(timeout = 5.seconds) {
                val state = awaitItem()
                viewModel.onEntityChanged(state.person?.shallowCopy {
                    firstNames = "Test"
                    lastName = "User"
                    gender = Person.GENDER_FEMALE
                })

                viewModel.onClickSave()

                val systemImpl: UstadMobileSystemImpl = di.direct.instance()
                val expectedErrMsg = systemImpl.getString(MessageID.field_required_prompt)

                val stateAfterSave = awaitItemWhere { it.usernameError != null }
                assertEquals(expectedErrMsg, stateAfterSave.usernameError,
                    "Username error set")
                assertEquals(expectedErrMsg, stateAfterSave.passwordError,
                    "Password error set")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenDateOfBirthNotFilledClickSave_shouldShowErrors() {
        testViewModel<PersonEditViewModel> {
            viewModelFactory {
                savedStateHandle[ARG_REGISTRATION_MODE] = PersonEditView.REGISTER_MODE_ENABLED.toString()
                PersonEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) { it.fieldsEnabled && it.person != null }

            viewModel.uiState.test {
                val state = awaitItem()
                viewModel.onEntityChanged(state.person?.shallowCopy {
                    firstNames = "Test"
                    lastName = "User"
                    gender = Person.GENDER_FEMALE
                })

                viewModel.onClickSave()

                val systemImpl: UstadMobileSystemImpl = di.direct.instance()
                val expectedErrMsg = systemImpl.getString(MessageID.field_required_prompt)

                val stateAfterSave = awaitItemWhere { it.dateOfBirthError != null }

                assertEquals(expectedErrMsg, stateAfterSave.dateOfBirthError,
                    "Error message set when date of birth ommitted")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenFormFilledAndClickSave_shouldRegisterAPerson() {
        testViewModel<PersonEditViewModel>() {
            val serverUrl = "http://test.com/"
            val accountManager = createMockAccountManager(serverUrl)

            extendDi {
                bind<UstadAccountManager>(overrides = true) with singleton {
                    accountManager
                }
            }

            viewModelFactory {
                savedStateHandle[ARG_REGISTRATION_MODE] = PersonEditView.REGISTER_MODE_ENABLED.toString()
                savedStateHandle[ARG_SERVER_URL] = serverUrl
                PersonEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val initState = awaitItemWhere { it.fieldsEnabled && it.person != null }
                viewModel.onEntityChanged(initState.person?.shallowCopy {
                    firstNames = "Test"
                    lastName = "User"
                    gender = Person.GENDER_FEMALE
                    newPassword = "test#@@12"
                    username = "testuser"
                    dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L) //Approx 20 years old
                })

                viewModel.onClickSave()

                verifyBlocking(accountManager, timeout(5000 * 1000)) {
                    register(argWhere { it.firstNames == "Test" && it.username == "testuser"},
                        eq(serverUrl), argWhere { it.makeAccountActive })
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenPresenterCreatedInNonRegistrationMode_whenFormFilledAndClickSave_shouldSaveAPersonInDb() {
        Napier.base(DebugAntilog())
        testViewModel<PersonEditViewModel>() {
            viewModelFactory {
                PersonEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val initState = awaitItemWhere { it.fieldsEnabled && it.person != null }
                viewModel.onEntityChanged(initState.person?.shallowCopy {
                    firstNames = "bob"
                    lastName = "newtestuser"
                    gender = Person.GENDER_MALE
                })

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onClickSave()

            val db = di.direct.on(activeEndpoint).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

            db.doorFlow(arrayOf("Person")) {
                db.personDao.getAllPerson()
            }.assertItemReceived(timeout = 5.seconds) { list ->
                list.any { it.lastName == "newtestuser" }
            }
        }
    }


}