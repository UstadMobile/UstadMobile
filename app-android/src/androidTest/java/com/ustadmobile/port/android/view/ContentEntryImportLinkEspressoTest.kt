package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter.Companion.GOOGLE_DRIVE_LINK
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.H5PImportData
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.test.core.impl.ProgressIdlingResource
import com.ustadmobile.util.test.AbstractImportLinkTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.*
import java.io.IOException

//TODO fix firebase issue
class ContentEntryImportLinkEspressoTest : AbstractImportLinkTest() {

    @get:Rule
    var mActivityRule = IntentsTestRule(ContentEntryImportLinkActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var serverDb: UmAppDatabase

    private lateinit var defaultDb: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    var mockWebServer = MockWebServer()

    var h5pServer = MockWebServer()

    private var idleProgress: ProgressIdlingResource? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        serverDb = UmAppDatabase.getInstance(context, "serverdb")
        defaultDb = UmAppDatabase.getInstance(context)


        repo = defaultDb //db!!.getRepository("http://localhost/dummy/", "")
        defaultDb.clearAllTables()
        serverDb.clearAllTables()

        createDb(defaultDb)
        createDb(serverDb)
    }

    @After
    fun close() {
        IdlingRegistry.getInstance().unregister(idleProgress)
    }

    //@Test
    fun givenUserTypesInvalidUrl_thenShowUserErrorMessageWithInvalidUrl() {

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText("hello"), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error == UstadMobileSystemImpl.instance.getString(MessageID.import_link_invalid_url, context))
    }

    //@Test
    fun givenUserTypesNonH5PUrl_thenShowUserErrorMessageWithUnSupportedContent() {


        mockWebServer.enqueue(MockResponse().setBody("no h5p here").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(mockWebServer.url("/noh5p").toString()), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error == UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))

        mockWebServer.close()


    }

    //@Test
    fun givenUserTypesH5PUrl_thenShowNoErrorShouldAppear() {


        mockWebServer.enqueue(MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        var urlString = mockWebServer.url("/somehp5here").toString()


        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error.isNullOrBlank())
        mockWebServer.close()


    }

    //@Test
    fun givenClicksOnDone() {

        mockWebServer.enqueue(MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setBody("H5PIntegration").setResponseCode(200))
        mockWebServer.start()

        val entryDao = serverDb.contentEntryDao
        val parentChildJoinDao = serverDb.contentEntryParentChildJoinDao
        val containerDao = serverDb.containerDao

        val contentEntry = ContentEntry()
        contentEntry.contentEntryUid = entryDao.insert(contentEntry)

        val parentChildJoin = ContentEntryParentChildJoin()
        parentChildJoin.cepcjParentContentEntryUid = -101
        parentChildJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
        parentChildJoinDao.insert(parentChildJoin)

        val container = Container(contentEntry)
        container.containerUid = containerDao.insert(container)

        val import = H5PImportData(contentEntry, container, parentChildJoin)

        val gson = GsonBuilder().create()
        val importJson = gson.toJson(import)

        var mockResponse = MockResponse()
        mockResponse.addHeader("Content-Type", "application/json")
        mockResponse.setBody(importJson)
        mockResponse.setResponseCode(200)

        h5pServer.enqueue(mockResponse)
        h5pServer.enqueue(mockResponse)
        h5pServer.start()

        var urlString = mockWebServer.url("/somehp5here").toString()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        intent.putExtra(ContentEntryImportLinkView.END_POINT_URL, h5pServer.url("").toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.import_link_done)).perform(click())
        Assert.assertTrue(defaultDb.contentEntryParentChildJoinDao.findListOfChildsByParentUuid(-101).isNotEmpty())

        mockWebServer.close()

    }

   // @Test
    fun givenUserTypesVideoLink_thenShowVideoTitle() {

        mockWebServer.enqueue(MockResponse().setHeader("Content-Length", 11).setHeader("Content-Type", "video/").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)

        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        var urlString = mockWebServer.url("/videohere").toString()

        onView(withId(R.id.entry_import_link_titleInput)).check(matches(not(isDisplayed())))

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.entry_import_link_titleInput)).check(matches(isDisplayed()))

        onView(withId(R.id.import_link_done)).check(matches(not(isEnabled())))

        onView(withId(R.id.entry_import_link_title_editText)).perform(replaceText("Video Title"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.import_link_done)).check(matches(isEnabled()))

        mockWebServer.close()


    }


    //@Test
    fun givenUserTypesGoogleDrive_whenLinkNotValid_showError() {


        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "video/")
                .setHeader("location", mockWebServer.url("/noVideoHere"))
                .setResponseCode(302))

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "audio/")
                .setResponseCode(200))


        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        GOOGLE_DRIVE_LINK = mockWebServer.url("/drive.google.com&id=abcde").toString()

        var urlString = mockWebServer.url("/drive.google.com&id=abcde").toString()

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error == UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))


        mockWebServer.close()


    }

    //@Test
    fun givenUserTypesVideoLink_whenFileSizeTooBig_showError() {

        mockWebServer.enqueue(MockResponse().setHeader("content-length", 104857600).setHeader("content-type", "video/").setResponseCode(200))
        mockWebServer.start()

        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        var urlString = mockWebServer.url("/videohere").toString()

        onView(withId(R.id.entry_import_link_titleInput)).check(matches(not(isDisplayed())))

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.entry_import_link_titleInput)).check(matches(not(isDisplayed())))

        onView(withId(R.id.import_link_done)).check(matches(not(isEnabled())))

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error == UstadMobileSystemImpl.instance.getString(MessageID.import_link_big_size, context))


    }


    //@Test
    fun givenUserTypesGoogleDriveShareLink_whenLinkNotValid_showError() {


        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "video/")
                .setHeader("location", mockWebServer.url("/noVideoHere"))
                .setResponseCode(302))

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "audio/")
                .setResponseCode(200))


        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        GOOGLE_DRIVE_LINK = mockWebServer.url("/drive.google.com&id=abcde").toString()

        var urlString = "https://drive.google.com/file/d/16wa2sh7pwQgnpR2H0X_EC9XFM5G0wISR/view?usp=sharing"

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        val textInput = activity.findViewById<TextInputLayout>(R.id.entry_import_link_textInput)
        Assert.assertTrue(textInput.error == UstadMobileSystemImpl.instance.getString(MessageID.import_link_content_not_supported, context))


        mockWebServer.close()


    }

    @Test
    fun givenUserTypesGoogleDriveShareLink_whenLinkValid_clickDone() {

        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
                .setHeader("content-type", "video/")
                .setHeader("location", mockWebServer.url("/noVideoHere"))
                .setResponseCode(302))

        mockWebServer.enqueue(MockResponse()
                .setHeader("content-type", "video/")
                .setResponseCode(200))


        val intent = Intent()
        intent.putExtra(ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        var activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        GOOGLE_DRIVE_LINK = mockWebServer.url("/drive.google.com&id=abcde").toString()

        var urlString = "https://drive.google.com/file/d/16wa2sh7pwQgnpR2H0X_EC9XFM5G0wISR/view?usp=sharing"

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.entry_import_link_titleInput)).check(matches(isDisplayed()))

        onView(withId(R.id.import_link_done)).check(matches(not(isEnabled())))

        onView(withId(R.id.entry_import_link_title_editText)).perform(replaceText("Video Title"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.import_link_done)).check(matches(isEnabled()))

        mockWebServer.close()


    }


}