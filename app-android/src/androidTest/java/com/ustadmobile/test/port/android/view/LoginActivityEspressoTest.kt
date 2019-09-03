package com.ustadmobile.test.port.android.view

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.gson.Gson
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LoginPresenter
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.view.LoginActivity
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginActivityEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(LoginActivity::class.java, false, false)

    private lateinit var mockRestServer: MockWebServer

    @Before
    @Throws(IOException::class)
    fun setUp() {
        mockRestServer = MockWebServer()
        mockRestServer.start()
        mockRestServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val url = request.requestUrl
                return if (TEST_VALID_USERNAME == url.queryParameter("user") && TEST_VALID_PASSWORD == url.queryParameter("password")) {
                    val account = UmAccount(1, TEST_VALID_USERNAME,
                            TEST_VALID_AUTH_TOKEN, "")
                    MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(Gson().toJson(account))
                } else {
                    MockResponse().setResponseCode(204)
                }
            }
        })
    }

    @Test
    fun givenValidUsernameAndPassword_whenLoginClicked_thenShouldFireIntent() {
        val launchIntent = Intent()
        launchIntent.putExtra(LoginPresenter.ARG_SERVER_URL,
                mockRestServer.url("/").toString())
        mActivityRule.launchActivity(launchIntent)

        onView(withId(R.id.activity_login_username)).perform(typeText(TEST_VALID_USERNAME))
        onView(withId(R.id.activity_login_password)).perform(typeText(TEST_VALID_PASSWORD))
        onView(withId(R.id.activity_login_button_login)).perform(click())
    }

    companion object {

        const val TEST_VALID_USERNAME = "username"

        const val TEST_VALID_PASSWORD = "secret"

        const val TEST_VALID_AUTH_TOKEN = "token"
    }

}
