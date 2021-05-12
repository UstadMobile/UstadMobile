package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.agoda.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.screen.LoginScreen
import com.ustadmobile.port.android.screen.PersonEditScreen
import com.ustadmobile.port.android.screen.RegisterAgeRedirectScreen
import com.ustadmobile.port.android.screen.RegisterMinorWaitForParentScreen
import com.ustadmobile.test.port.android.util.setMessageIdOption
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kodein.di.DIAware

class RegisterEndToEndTest: TestCase() {


    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun givenUserNotLoggedIn_whenUserRegistersAsMinor_shouldCompleteAndReturn() {
        lateinit var activityScenario: ActivityScenario<MainActivity>

        init {
            val mockAccountCreated = UmAccount(personUid = 1L, username = "janedoe",
                    firstName = "Jane", lastName = "Doe")

            mockWebServer.enqueue(MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(Buffer().write(
                            Json.stringify(UmAccount.serializer(), mockAccountCreated).toByteArray())))

            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                val siteObj = Site().apply {
                    registrationAllowed = true
                }
                val di = (context as DIAware).di
                val siteJson = safeStringify(di, Site.serializer(), siteObj)
                val destArgs = mapOf(ARG_SERVER_URL to mockWebServer.url("/").toString(),
                    ARG_SITE to siteJson)

                it.putExtra(UstadView.ARG_NEXT,
                        "${Login2View.VIEW_NAME}?${destArgs.toQueryString()}")
            }
            activityScenario = launchActivity(intent = launchIntent)
        }.run {
            LoginScreen {
                createAccount {
                    click()
                }
            }

            RegisterAgeRedirectScreen {
                datePicker {
                    val minorDob = DateTime.now() - 10.years
                    setDate(minorDob.yearInt, minorDob.month1, minorDob.dayOfMonth)
                }
                nextButton {
                    click()
                }
            }

            PersonEditScreen {
                firstNameTextInput {
                    edit {
                        typeText("Jane")
                    }
                }
                lastNameTextInput {
                    edit {
                        typeText("Doe")
                    }
                }

                closeSoftKeyboard()

                genderValue {
                    setMessageIdOption(this, "Female")
                }



                parentsContactsInput {
                    edit {
                        typeText("parent@email.com")
                    }
                }

                closeSoftKeyboard()

                usernameTextInput {
                    edit {
                        typeText("janedoe")
                    }
                }

                scrollToBottom()
                closeSoftKeyboard()

                passwordTextInput {
                    edit {
                        typeText("secret")
                    }
                }

                closeSoftKeyboard()

                confirmPassTextInput {
                    edit {
                        typeText("secret")
                    }
                }

                closeSoftKeyboard()


                KView {
                    withId(R.id.menu_done)
                } perform {
                    click()
                }

            }

            RegisterMinorWaitForParentScreen {
                usernameText {
                    hasText("janedoe")
                }

                okButton {
                    click()
                }
            }

            LoginScreen {
                loginButton {
                    isDisplayed()
                }
            }

        }


    }


}