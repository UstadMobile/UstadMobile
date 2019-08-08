package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.util.test.AbstractImportLinkTest
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ContentEntryImportLinkEspressoTest : AbstractImportLinkTest() {

    @get:Rule
    var mActivityRule = IntentsTestRule(ContentEntryImportLinkActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    var mockWebServer = MockWebServer()

    var h5pServer = MockWebServer()

    @Before
    @Throws(IOException::class)
    fun setup() {

        db = UmAppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().context)
        repo = db //db!!.getRepository("http://localhost/dummy/", "")
        db.clearAllTables()

        createDb(db)
    }

    @Test
    fun givenUserTypesInvalidUrl_thenShowUserErrorMessageWithInvalidUrl() {

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText("hello"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.textinput_error)).check(matches(withText("Invalid Url")))

    }

    @Test
    fun givenUserTypesNonH5PUrl_thenShowUserErrorMessageWithUnSupportedContent() {


        mockWebServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)



        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(mockWebServer.url("/noh5p").toString()), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.textinput_error)).check(matches(withText("Content not supported")))

    }

    @Test
    fun givenUserTypesH5PUrl_thenShowNoErrorShouldAppear() {


        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        var urlString = mockWebServer.url("/somehp5here").toString()


        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error.isNullOrBlank())

    }

    @Test
    fun givenClicksOnDone() {

        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()

        h5pServer.enqueue(MockResponse().setBody("{}").setResponseCode(200))
        h5pServer.start()

        var urlString = mockWebServer.url("/somehp5here").toString()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        intent.putExtra(ContentEntryImportLinkView.END_POINT_URL, h5pServer.url("").toString())
        mActivityRule.launchActivity(intent)


        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        runBlocking {

            onView(withId(R.id.import_link_done)).perform(click())
        }
    }


}