package com.ustadmobile.port.android.view

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.port.android.screen.SiteEnterLinkScreen
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Workspace screen Test")
@ExperimentalStdlibApi
class SiteEnterLinkFragmentTest : TestCase(){

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup(){
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("given valid workspace link when checked should show enable button")
    @Test
    fun givenValidWorkSpaceLink_whenCheckedAndIsValid_shouldAllowToGoToNextScreen() {


        init{

            val site = Json.encodeToString(Site.serializer(), Site().apply {
                siteName = "Dummy workspace"
                registrationAllowed = true
                guestLogin = true
            })

            val buffer = Buffer()
            buffer.write((site.toByteArray()))
            buffer.flush()

            mockWebServer.enqueue(MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(buffer))

            buffer.clear()

        }.run{

            SiteEnterLinkScreen{

                launchFragment(mockWebServer.url("/").toString(), systemImplNavRule)
                nextButton{
                    isDisplayed()
                }
                enterLinkTextInput{
                    not(hasInputLayoutError(context.getString(R.string.invalid_link)))
                }

            }

        }


    }

    @AdbScreenRecord("given invalid workspace link when checked should not show next button")
    @Test
    fun givenInValidWorkSpaceLink_whenCheckedAndIsValid_shouldNotAllowToGoToNextScreen() {


        init{
            mockWebServer.enqueue(MockResponse().setResponseCode(404))
        }.run {

            SiteEnterLinkScreen {

                launchFragment(mockWebServer.url("/").toString(), systemImplNavRule)

                nextButton{
                    isNotDisplayed()
                }
                enterLinkTextInput {
                    hasInputLayoutError(context.getString(R.string.invalid_link))
                }

            }

        }



    }

}
