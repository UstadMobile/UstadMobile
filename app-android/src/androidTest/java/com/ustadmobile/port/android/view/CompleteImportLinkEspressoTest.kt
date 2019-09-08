package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Intent
import android.net.Network
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.toughra.ustadmobile.BuildConfig
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.NetworkManagerBleHelper
import com.ustadmobile.util.test.AbstractImportLinkTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.rule.ServiceTestRule
import android.os.IBinder
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.sharedse.network.NetworkManagerBleAndroidService

// TODO tests are disabled until jenkins are create its own server
class CompleteImportLinkEspressoTest : AbstractImportLinkTest() {


    @get:Rule
    var mActivityRule = IntentsTestRule(ContentEntryListActivity::class.java, false, false)

    @get:Rule
    val mServiceRule = ServiceTestRule()

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var serverDb: UmAppDatabase

    private lateinit var defaultDb: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    lateinit var activity: Activity

    private lateinit var mockImpl: UstadMobileSystemImpl


    @Before
    fun setup() {
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        serverDb = UmAppDatabase.getInstance(context, "serverdb")
        defaultDb = UmAppDatabase.getInstance(context)

        mServiceRule.startService(Intent(context, NetworkManagerBleAndroidService::class.java))
        val binder = mServiceRule.bindService(
                Intent(context, NetworkManagerBleAndroidService::class.java))
        val service = (binder as NetworkManagerBleAndroidService.LocalServiceBinder).service

        repo = defaultDb //db!!.getRepository("http://localhost/dummy/", "")
        defaultDb.clearAllTables()
        serverDb.clearAllTables()

        createDb(defaultDb)
        createDb(serverDb)

        val intent = Intent()
        intent.putExtra(ContentEntryListFragmentPresenter.ARG_CONTENT_ENTRY_UID, (-101).toString())
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        val testEndpoint = "http://" + BuildConfig.TEST_HOST +
                ":" + 8087 + "/"

        val testAccount = UmAccount(0, "test", "", testEndpoint)
        UmAccountManager.setActiveAccount(testAccount, context)


    }

    //@Test
    fun endToEnd() {

        var urlString = "https://h5p.org/h5p/embed/612"

        onView(withId(R.id.create_new_content)).perform(click())
        Thread.sleep(100)
        onView(withId(R.id.content_import_link)).perform(click())

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString))

        Thread.sleep(500)

        onView(withId(R.id.import_link_done)).perform(click())


    }

    //@Test
    fun videoEndToEnd() {

        var urlString = "https://www.ustadmobile.com/files/vso/cleansurroundings.mp4"

        onView(withId(R.id.create_new_content)).perform(click())
        Thread.sleep(100)
        onView(withId(R.id.content_import_link)).perform(click())

        onView(withId(R.id.entry_import_link_editText)).perform(click())
        onView(withId(R.id.entry_import_link_editText)).perform(replaceText(urlString))

        Thread.sleep(1000)

        onView(withId(R.id.import_link_done)).perform(click())


    }



}
