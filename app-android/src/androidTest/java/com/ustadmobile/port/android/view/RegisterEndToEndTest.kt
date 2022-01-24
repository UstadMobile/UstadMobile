package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.port.android.screen.LoginScreen
import com.ustadmobile.port.android.screen.PersonEditScreen
import com.ustadmobile.port.android.screen.RegisterAgeRedirectScreen
import com.ustadmobile.port.android.screen.RegisterMinorWaitForParentScreen
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.test.port.android.util.setMessageIdOption
import io.github.kakaocup.kakao.common.views.KView
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

@AdbScreenRecord("Register EndtoEnd screen Test")
class RegisterEndToEndTest: TestCase() {

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        //Note: mockwebserver will be killed when orchestrator ends the process
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun givenUserNotLoggedIn_whenUserRegistersAsMinor_shouldCompleteAndReturn() {
        init {
            val di = getApplicationDi()
            val repo: UmAppDatabase = di.on(Endpoint(mockWebServer.url("/").toString()))
                .direct.instance(tag = DoorTag.TAG_REPO)
            val siteObj = Site().apply {
                registrationAllowed = true
                authSalt = randomString(20)
                siteUid = repo.siteDao.insert(this)
            }

            val mockAccountCreated = UmAccount(personUid = 1L, username = "janedoe",
                    firstName = "Jane", lastName = "Doe")

            mockWebServer.dispatcher = object: Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    if(request.requestUrl.toString().endsWith("/register")
                        && request.method.equals("post", ignoreCase = true)) {

                        return MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(Json.encodeToString(UmAccount.serializer(), mockAccountCreated))
                    }else {
                        return MockResponse()
                            .setResponseCode(404)
                    }
                }
            }


            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                val siteJson = safeStringify(di, Site.serializer(), siteObj)
                val destArgs = mapOf(ARG_SERVER_URL to mockWebServer.url("/").toString(),
                    ARG_SITE to siteJson)

                it.putExtra(UstadView.ARG_NEXT,
                        "${Login2View.VIEW_NAME}?${destArgs.toQueryString()}")
            }
            launchActivity<MainActivity>(intent = launchIntent)
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
                    setMessageIdOption("Female")
                }



                parentsContactsInput {
                    edit {
                        typeText("parent@email.com")
                    }
                }

                closeSoftKeyboard()

                usernameTextInput {
                    edit {
                        replaceText("janedoe")
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
                    scrollTo()
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