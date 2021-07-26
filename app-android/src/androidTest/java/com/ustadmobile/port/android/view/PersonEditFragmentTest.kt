package com.ustadmobile.port.android.view

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.screen.PersonEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.kodein.di.direct
import org.kodein.di.instance


@AdbScreenRecord("PersonEdit screen Test")
class PersonEditFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var serverUrl: String

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }






    @AdbScreenRecord("given person edit opened in normal mode username and password " +
            "should be hidden")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenUsernameAndPasswordShouldBeHidden() {

        init {

        }.run {
            PersonEditScreen {
                launchFragment(PersonEditView.REGISTER_MODE_NONE, fillForm = false, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        context = context, testContext = this@run,
                databinding = dataBindingIdlingResourceRule, crud = crudIdlingResourceRule)

                usernameTextInput {
                    isNotDisplayed()
                }
                passwordTextInput {
                    isNotDisplayed()
                }
                confirmPassTextInput {
                    isNotDisplayed()
                }
            }
        }


    }


    @AdbScreenRecord("given person edit opened in registration mode when username and " +
            "password are not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenUserNameAndPasswordAreNotFilled_thenShouldShowErrors() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(registrationMode = PersonEditView.REGISTER_MODE_ENABLED, leftOutPassword = true, leftOutUsername = true,
                        serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        context = context, testContext = this@run,
                        databinding = dataBindingIdlingResourceRule, crud = crudIdlingResourceRule)


                scrollToBottom()
                usernameTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                passwordTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }

    }

    @AdbScreenRecord("given person edit opened in registration mode when dateOfBirth " +
            "is not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenDateOfBirthAreNotFilled_thenShouldShowErrors() {

        init {


        }.run {
            PersonEditScreen {


                launchFragment(registrationMode = PersonEditView.REGISTER_MODE_ENABLED, leftOutDateOfBirth = true, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        context = context, testContext = this@run,
                        databinding = dataBindingIdlingResourceRule, crud = crudIdlingResourceRule)


                birthdayTextInput {
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }
    }

    @AdbScreenRecord("given person edit opened in registration mode when password " +
            "doesn't match and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenPasswordDoNotMatch_thenShouldShowErrors() {

        init {

        }.run {
            PersonEditScreen {

                launchFragment(registrationMode = PersonEditView.REGISTER_MODE_ENABLED, misMatchPassword = true, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        context = context, testContext = this@run,
                        databinding = dataBindingIdlingResourceRule, crud = crudIdlingResourceRule)

                scrollToBottom()
                passwordTextInput {
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }
                confirmPassTextInput {
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }
            }
        }
    }

    @AdbScreenRecord("given person edit opened in registration mode when try to register " +
            "existing person should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenTryToRegisterExistingPerson_thenShouldShowErrors() {


        init {
            mockWebServer.enqueue(MockResponse().setResponseCode(409))


        }.run {

            PersonEditScreen {

                launchFragment(registrationMode = PersonEditView.REGISTER_MODE_ENABLED, misMatchPassword = false, leftOutUsername = false, serverUrl = serverUrl, systemImplNavRule = systemImplNavRule,
                        context = context, testContext = this@run,
                        databinding = dataBindingIdlingResourceRule, crud = crudIdlingResourceRule)

                scrollToBottom()
                usernameTextInput {
                    hasInputLayoutError(context.getString(R.string.person_exists))
                }
            }

        }
    }


}
