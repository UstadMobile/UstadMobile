package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.SystemClock
import android.util.Base64
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.toughra.ustadmobile.BuildConfig
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.test.port.android.UmAndroidTestUtil
import com.ustadmobile.test.port.android.UmViewActions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.allOf
import org.json.JSONException
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


/**
 * Test class to make sure DownloadDialog and DownloadNotification behaves as expected on devices.
 *
 * **NOTE:**
 *
 * When doing RecyclerView based espresso checking
 * [DownloadDialogAndNotificationEspressoTest.givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons],
 * you must to use [ViewMatchers.isDisplayed] on view matcher,
 * otherwise matcher will match match multiple view with the same Id and test will fail.
 *
 * **How to run this test**
 * 1. Get your local machine IP address
 * 2. buildconfig.local.properties and change test.um_http_testserver value to your acquired IP address
 * 3. Start local test server by running this task = lib-http-testserver:runHttpTestServer
 */

// TODO tests are disabled until jenkins are create its own server
@ExperimentalStdlibApi
//@RunWith(AndroidJUnit4::class)
//@LargeTest
class DownloadDialogAndNotificationEspressoTest {

    @get:Rule
    public var mHomeActivityRule = ActivityTestRule(HomeActivity::class.java, false, false)


    @get:Rule
    public var mPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var rootEntry: ContentEntry

    private lateinit var entry1: ContentEntry
    private lateinit var entry2: ContentEntry

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umRepo: UmAppDatabase

    private lateinit var mContext: Context

    private var mDevice: UiDevice? = null

    private var testManagerUrl: String? = null

    private var serverActivePort = 0

    private var connectedToUnMeteredConnection = false


    @Before
    @Throws(IOException::class, JSONException::class)
    fun setEndpoint() {
        mContext = InstrumentationRegistry.getInstrumentation().context
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP

        //check active network

        val wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        val ssid = wifiManager?.connectionInfo?.ssid?.toLowerCase()!!
        connectedToUnMeteredConnection = !ssid.contains("androidwifi")
                && !ssid.contains("unknown ssid")

        testManagerUrl = "http://" + BuildConfig.TEST_HOST + ":" + BuildConfig.TEST_PORT + "/"

        val response = sendCommand("new", 0)
        serverActivePort = Integer.parseInt(response.getString("port"))

        val testEndpoint = "http://" + BuildConfig.TEST_HOST +
               ":" + serverActivePort + "/"

        val testAccount = UmAccount(0, "test", "",testEndpoint)

        UmAccountManager.setActiveAccount(testAccount, mContext)

        umRepo = UmAccountManager.getRepositoryForActiveAccount(mContext)


        prepareContentEntries()
        SystemClock.sleep(WAIT_TIME_MIN)


        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val mIntent = Intent()
        mIntent.putExtra(ARG_CONTENT_ENTRY_UID, rootEntry.contentEntryUid)
        mIntent.putExtra(ARG_DOWNLOADED_CONTENT, "")
        mHomeActivityRule.launchActivity(mIntent)

    }

    @After
    fun closeNotificationPanel() {
        if (mDevice != null) {
            mDevice!!.pressBack()
        }
    }

    @Throws(IOException::class, JSONException::class)
    private fun sendCommand(command: String, bytespersecond: Long): JSONObject {
        return JSONObject(IOUtils.toString(URL("$testManagerUrl?cmd=" +
                if (bytespersecond == 0L)
                    command
                else
                    command + "&bytespersecond=" + bytespersecond
                            + "&port=" + serverActivePort).openStream(), StandardCharsets.UTF_8))
    }

    private fun startDownloading(wifiOnly: Boolean) {
        onView(allOf<View>(
                withTagValue(equalTo(entry1.contentEntryUid)),
                withId(R.id.entry_holder)
        )).perform(click())

        SystemClock.sleep(WAIT_TIME_MIN)

        onView(allOf<View>(
                isDescendantOfA(withTagValue(equalTo(entry2.contentEntryUid))),
                withId(R.id.content_entry_item_download)
        )).perform(click())

        SystemClock.sleep(WAIT_TIME_MIN)

        onView(withId(R.id.wifi_only_option)).perform(
                UmViewActions.setChecked(wifiOnly))

        SystemClock.sleep(WAIT_TIME_MIN)

        onView(withId(android.R.id.button1)).perform(click())
    }


    private fun prepareContentEntries() {
        UmAppDatabase.getInstance(mContext).clearAllTables()

        umAppDatabase = UmAppDatabase.getInstance(mContext)
        rootEntry = ContentEntry("Lorem ipsum title",
                "Lorem ipsum description", leaf = false, publik = true)
        rootEntry.contentEntryUid = CONTENT_ENTRY_UID
        umAppDatabase.contentEntryDao.insert(rootEntry)

        entry1 = ContentEntry("title 1", "description 1", leaf = false, publik = true)


        entry2 = ContentEntry("title 2", "description 2", leaf = true, publik = true)
        entry2.contentEntryUid = CONTAINER_UID
        val entry3 = ContentEntry("title 3", "description 3", leaf = true, publik = false)
        val entry4 = ContentEntry("title 4", "description 4", leaf = true, publik = false)

        entry1.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry1)
        entry2.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry2)
        entry3.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry3)
        entry4.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry4)

        val container = Container(entry2)
        container.containerUid = entry2.contentEntryUid
        container.fileSize = 4678063L
        container.lastModified = System.currentTimeMillis()
        container.cntNumEntries = 1

        umRepo.containerDao.insert(container)

        umAppDatabase.contentEntryParentChildJoinDao.insertList(
                listOf(ContentEntryParentChildJoin(rootEntry, entry1, 0),
                        ContentEntryParentChildJoin(entry1, entry2, 0),
                        ContentEntryParentChildJoin(rootEntry, entry3, 0),
                        ContentEntryParentChildJoin(entry1, entry4, 0)))

        val entryFile = ContainerEntryFile(Base64.encodeToString(CONTAINER_UID.toString().encodeToByteArray(),
                Base64.DEFAULT),container.fileSize,container.fileSize,0,System.currentTimeMillis())

        val newEntries = mutableListOf<ContainerEntryWithContainerEntryFile>()
        entryFile.cefUid = umAppDatabase.containerEntryFileDao.insert(entryFile)
        newEntries.add(ContainerEntryWithContainerEntryFile(CONTAINER_UID.toString(), container,
                entryFile))

        umAppDatabase.containerEntryDao.insertAndSetIds(newEntries)
    }

    private suspend fun  waitForStatus(checkerStatus: Int, timeout: Long){
        val channel = Channel<Boolean>(1)
        val networkManager = UstadMobileSystemImpl.instance.networkManager as NetworkManagerBle

        networkManager.addDownloadChangeListener(object : OnDownloadJobItemChangeListener{
            override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
                if(status != null){
                    if(status.status == checkerStatus)
                        channel.offer(status.status == checkerStatus)
                }
            }

        })

        withTimeoutOrNull(timeout) { channel.receive() }
    }

    //@Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloading_shouldStartForegroundServiceAndShowNotification() {
        SystemClock.sleep(WAIT_TIME_MIN)

        sendCommand("throttle", SLOW_THROTTLE_BYTES)

        startDownloading(connectedToUnMeteredConnection)

        val openNotificationTray = mDevice!!.openNotification()

        mDevice!!.wait(Until.hasObject(By.textContains(NOTIFICATION_TITLE_PREFIX)), WAIT_TIME_MIN)

        val title = mDevice!!.findObject(By.textContains(NOTIFICATION_TITLE_PREFIX))

        val entryName = mDevice!!.findObject(By.textContains(NOTIFICATION_ENTRY_PREFIX))

        assertTrue("Notification tray was opened ", openNotificationTray)

        assertTrue("Download notification was shown",
                title.text.contains(NOTIFICATION_TITLE_PREFIX))

        assertEquals("Notification shown was for  " + entry2.title!!,
                entryName.text, entry2.title)

        SystemClock.sleep(WAIT_TIME_MIN)
    }

    //@Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons() {
        SystemClock.sleep(WAIT_TIME_MIN)

        sendCommand("throttle", AVG_THROTTLE_BYTES)

        startDownloading(connectedToUnMeteredConnection)

        runBlocking { waitForStatus(JobStatus.COMPLETE, TimeUnit.SECONDS.toMillis(30))}

        SystemClock.sleep(WAIT_TIME_MIN)

        onView(allOf<View>(isDisplayed(),
                isDescendantOfA(withTagValue(equalTo(entry2.contentEntryUid))),
                withId(R.id.view_download_status_button_img)

        )).check(matches(withContentDescription(equalTo(DOWNLOADED_CONTENT_DESC))))

    }


    //@Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloadingAndConnectionChangedToMetered_shouldStopDownloading() {

        Assume.assumeTrue("Device is connected on un-metered connection, can execute the test", connectedToUnMeteredConnection)

        sendCommand("throttle", AVG_THROTTLE_BYTES)

        SystemClock.sleep(WAIT_TIME_MIN)

        startDownloading(connectedToUnMeteredConnection)

        UmAndroidTestUtil.setAirplaneModeEnabled(enabled = true, backTwice = false)

        runBlocking { waitForStatus(JobStatus.WAITING_FOR_CONNECTION, TimeUnit.SECONDS.toMillis(3))}

        assertEquals("Download task was paused and waiting for connectivity",
                umAppDatabase.downloadJobDao.lastJob()?.djStatus,
                JobStatus.WAITING_FOR_CONNECTION)


        //reset for next test
        UmAndroidTestUtil.setAirplaneModeEnabled(false)

    }


    //@Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadStarted_whenConnectivityInterrupted_shouldResumeAndCompleteDownload() {

        SystemClock.sleep(WAIT_TIME_MIN)

        sendCommand("throttle", FAST_THROTTLE_BYTES)

        SystemClock.sleep(WAIT_TIME_MIN)

        startDownloading(connectedToUnMeteredConnection)

        SystemClock.sleep(WAIT_TIME_MAX)

        UmAndroidTestUtil.setAirplaneModeEnabled(enabled = true, backTwice = false)

        SystemClock.sleep(WAIT_TIME_MAX)

        assertEquals("Download task was stopped and waiting for connection",
                umAppDatabase.downloadJobDao.lastJob()?.djStatus, JobStatus.WAITING_FOR_CONNECTION)

        runBlocking { waitForStatus(JobStatus.WAITING_FOR_CONNECTION, TimeUnit.SECONDS.toMillis(3))}

        UmAndroidTestUtil.setAirplaneModeEnabled(enabled = false, backTwice = false)

        runBlocking { waitForStatus(JobStatus.COMPLETE, TimeUnit.SECONDS.toMillis(30))}

        SystemClock.sleep(WAIT_TIME_MIN)

        assertEquals("Download task was completed successfully",
                umAppDatabase.downloadJobDao.lastJob()?.djStatus, JobStatus.COMPLETE)

    }

    companion object {


        private const val NOTIFICATION_TITLE_PREFIX = "Downloading"

        private const val NOTIFICATION_ENTRY_PREFIX = "title"

        private const val DOWNLOADED_CONTENT_DESC = "Downloaded"

        private const val CONTENT_ENTRY_UID = -4103245208651563007L

        private const val CONTAINER_UID = -4103245208651563017L

        private const val FAST_THROTTLE_BYTES = 350000L

        private const val AVG_THROTTLE_BYTES = 200000L

        private const val SLOW_THROTTLE_BYTES = 100000L

        private const val WAIT_TIME_MIN = 2000L

        private const val WAIT_TIME_MAX = 4500L
    }

}
