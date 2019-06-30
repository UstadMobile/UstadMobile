package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.SystemClock
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
import com.ustadmobile.core.db.WaitForLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.test.port.android.UmAndroidTestUtil
import com.ustadmobile.test.port.android.UmViewActions
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


/**
 * Test class to make sure DownloadDialog and DownloadNotification behaves as expected on devices.
 *
 * **NOTE:**
 *
 * When doing RecyclerView based espresso checking
 * [DownloadDialogAndNotificationEspressoTest.givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons],
 * you must to use [ViewMatchers.isDisplayed] on view matcher,
 * otherwise matcher will match match multiple view with the same Id and test will fail.
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class DownloadDialogAndNotificationEspressoTest {

    @get:Rule
    public var mActivityRule = ActivityTestRule(HomeActivity::class.java, false, false)

    @get:Rule
    public var mPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var rootEntry: ContentEntry

    private lateinit var entry1: ContentEntry

    private var umAppDatabase: UmAppDatabase? = null

    private lateinit var mContext: Context

    private var mDevice: UiDevice? = null

    private var testManagerUrl: String? = null

    private var serverActivePort = 0

    private var connectedToUnMeteredConnection = false


    @Before
    @Throws(IOException::class, JSONException::class)
    fun setEndpoint() {
        mContext = InstrumentationRegistry.getInstrumentation().context

        //check active network

        val wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        connectedToUnMeteredConnection = wifiManager?.connectionInfo
                ?.ssid?.toLowerCase()?.contains("androidwifi") ?: false

        // TODO for Lukundo
       testManagerUrl = "http://" + BuildConfig.TEST_HOST + ":" + BuildConfig.TEST_PORT + "/"

        val response = sendCommand("new", 0)
        serverActivePort = Integer.parseInt(response.getString("port"))

       // val testEndpoint = "http://" + BuildConfig.TEST_HOST +
        //        ":" + serverActivePort + "/"

      //  val testAccount = UmAccount(0, "test", "",
      //          testEndpoint)
      //  UmAccountManager.setActiveAccount(testAccount, InstrumentationRegistry.getTargetContext())

        prepareContentEntriesAndFiles()
        SystemClock.sleep(MIN_SLEEP_TIME)


        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val mIntent = Intent()
        mIntent.putExtra(ARG_CONTENT_ENTRY_UID, rootEntry.contentEntryUid)
        mIntent.putExtra(ARG_DOWNLOADED_CONTENT, "")
        mActivityRule.launchActivity(mIntent)

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
                isDescendantOfA(withTagValue(equalTo(entry1.contentEntryUid))),
                withId(R.id.content_entry_item_download)
        )).perform(click())

        SystemClock.sleep(MIN_SLEEP_TIME)

        onView(withId(R.id.wifi_only_option)).perform(
                UmViewActions.setChecked(wifiOnly))

        onView(withId(android.R.id.button1)).perform(click())
    }


    private fun prepareContentEntriesAndFiles() {
        UmAppDatabase.getInstance(mContext).clearAllTables()
        umAppDatabase = UmAppDatabase.getInstance(mContext)
        rootEntry = ContentEntry("Lorem ipsum title",
                "Lorem ipsum description", leaf = false, publik = true)
        rootEntry.contentEntryUid = TEST_CONTENT_ENTRY_FILE_UID
        umAppDatabase!!.contentEntryDao.insert(rootEntry)

        entry1 = ContentEntry("title 1", "description 1", leaf = true, publik = true)
        val entry2 = ContentEntry("title 2", "description 2", leaf = true, publik = true)
        val entry3 = ContentEntry("title 3", "description 3", leaf = true, publik = false)
        val entry4 = ContentEntry("title 4", "description 4", leaf = true, publik = false)

        entry1.contentEntryUid = umAppDatabase!!.contentEntryDao.insert(entry1)
        entry2.contentEntryUid = umAppDatabase!!.contentEntryDao.insert(entry2)
        entry3.contentEntryUid = umAppDatabase!!.contentEntryDao.insert(entry3)
        entry4.contentEntryUid = umAppDatabase!!.contentEntryDao.insert(entry4)

        umAppDatabase!!.contentEntryParentChildJoinDao.insertList(
                listOf(ContentEntryParentChildJoin(rootEntry, entry1, 0),
                        ContentEntryParentChildJoin(rootEntry, entry2, 0),
                        ContentEntryParentChildJoin(rootEntry, entry3, 0),
                        ContentEntryParentChildJoin(rootEntry, entry4, 0)))

        /*   ContentEntryFile entryFile = new ContentEntryFile();
        entryFile.setLastModified(System.currentTimeMillis());
        entryFile.setFileSize(5100000);
        entryFile.setContentEntryFileUid(TEST_CONTENT_ENTRY_FILE_UID);
        umAppDatabase.getContentEntryFileDao().insert(entryFile);

        ContentEntryContentEntryFileJoin fileJoin1 = new ContentEntryContentEntryFileJoin(entry1, entryFile);
        fileJoin1.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin1));

        ContentEntryContentEntryFileJoin fileJoin2 = new ContentEntryContentEntryFileJoin(entry2, entryFile);
        fileJoin2.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin2));

        ContentEntryContentEntryFileJoin fileJoin3 = new ContentEntryContentEntryFileJoin(entry3, entryFile);
        fileJoin3.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin3));

        ContentEntryContentEntryFileJoin fileJoin4 = new ContentEntryContentEntryFileJoin(entry4, entryFile);
        fileJoin4.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin4)); */

    }

    @Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloading_shouldStartForegroundServiceAndShowNotification() {
        SystemClock.sleep(MIN_SLEEP_TIME)

        sendCommand("throttle", THROTTLE_BYTES)

        startDownloading(connectedToUnMeteredConnection)

        SystemClock.sleep(MIN_SLEEP_TIME)

        val openNotificationTray = mDevice!!.openNotification()

        mDevice!!.wait(Until.hasObject(By.textContains(NOTIFICATION_TITLE_PREFIX)), MIN_SLEEP_TIME)

        val title = mDevice!!.findObject(By.textContains(NOTIFICATION_TITLE_PREFIX))

        val entryName = mDevice!!.findObject(By.textContains(NOTIFICATION_ENTRY_PREFIX))

        assertTrue("Notification tray was opened ", openNotificationTray)

        assertTrue("Download notification was shown",
                title.text.contains(NOTIFICATION_TITLE_PREFIX))

        assertEquals("Notification shown was for  " + entry1.title!!,
                entryName.text, entry1.title)

        SystemClock.sleep(MIN_SLEEP_TIME)


    }

    @Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons() {
        SystemClock.sleep(MIN_SLEEP_TIME)

        sendCommand("throttle", THROTTLE_BYTES)

        startDownloading(connectedToUnMeteredConnection)

        WaitForLiveData.observeUntil(umAppDatabase!!.downloadJobDao.lastJobLive(),
                MAX_THRESHOLD * 60000, object : WaitForLiveData.WaitForChecker<DownloadJob> {
            override fun done(value: DownloadJob): Boolean {
                return value.djStatus == JobStatus.COMPLETE
            }
        })

        SystemClock.sleep(MIN_SLEEP_TIME)

        onView(allOf<View>(isDisplayed(),
                isDescendantOfA(withTagValue(equalTo(entry1.contentEntryUid))),
                withId(R.id.view_download_status_button_img)
        )).check(matches(withContentDescription(equalTo(DOWNLOADED_CONTENT_DESC))))

    }


    @Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadIconClickedOnEntryListItem_whenDownloadingAndConnectionChangedToMetered_shouldStopDownloading() {

        Assume.assumeTrue("Device is connected on metered connection, can execute the test", connectedToUnMeteredConnection)

        sendCommand("throttle", THROTTLE_BYTES)

        SystemClock.sleep(MIN_SLEEP_TIME)

        startDownloading(connectedToUnMeteredConnection)

        UmAndroidTestUtil.setAirplaneModeEnabled(true)

        WaitForLiveData.observeUntil(umAppDatabase!!.downloadJobDao.lastJobLive(),
                MAX_LATCH_TIME * 1000, object : WaitForLiveData.WaitForChecker<DownloadJob> {
            override fun done(value: DownloadJob): Boolean {
                return value.djStatus == JobStatus.WAITING_FOR_CONNECTION
            }
        })

        assertEquals("Download task was paused and waiting for connectivity",
                umAppDatabase!!.downloadJobDao.lastJob()?.djStatus,
                JobStatus.WAITING_FOR_CONNECTION)


        //reset for next test
        UmAndroidTestUtil.setAirplaneModeEnabled(false)
        WaitForLiveData.observeUntil(umAppDatabase!!.connectivityStatusDao.statusLive(),
                MAX_LATCH_TIME * 1000, object : WaitForLiveData.WaitForChecker<ConnectivityStatus> {
            override fun done(value: ConnectivityStatus): Boolean {
                return value.connectivityState != ConnectivityStatus.STATE_DISCONNECTED
            }
        })

    }


    @Test
    @Throws(IOException::class, JSONException::class)
    fun givenDownloadStarted_whenConnectivityInterrupted_shouldResumeAndCompleteDownload() {

        SystemClock.sleep(MIN_SLEEP_TIME)

        sendCommand("throttle", (THROTTLE_BYTES * MAX_THRESHOLD))

        startDownloading(connectedToUnMeteredConnection)

        UmAndroidTestUtil.setAirplaneModeEnabled(true)

        WaitForLiveData.observeUntil(umAppDatabase!!.downloadJobDao.lastJobLive(),
                MAX_LATCH_TIME * 1000, object : WaitForLiveData.WaitForChecker<DownloadJob> {
            override fun done(value: DownloadJob): Boolean {
                return value.djStatus == JobStatus.WAITING_FOR_CONNECTION
            }
        })

        UmAndroidTestUtil.setAirplaneModeEnabled(false)

        SystemClock.sleep(MAX_SLEEP_TIME)

        WaitForLiveData.observeUntil(umAppDatabase!!.connectivityStatusDao.statusLive(),
                MAX_LATCH_TIME * 1000, object : WaitForLiveData.WaitForChecker<ConnectivityStatus> {
            override fun done(value: ConnectivityStatus): Boolean {
                return value.connectivityState != ConnectivityStatus.STATE_DISCONNECTED
            }
        })

        WaitForLiveData.observeUntil(umAppDatabase!!.downloadJobDao.lastJobLive(),
                MAX_LATCH_TIME * 1000, object : WaitForLiveData.WaitForChecker<DownloadJob> {
            override fun done(value: DownloadJob): Boolean {
                return value.djStatus == JobStatus.COMPLETE
            }
        })

        assertEquals("Download task was completed successfully",
                umAppDatabase!!.downloadJobDao.lastJob()?.djStatus, JobStatus.COMPLETE)
        SystemClock.sleep(MIN_SLEEP_TIME)
    }

    companion object {

        private const val MIN_SLEEP_TIME = 1000L

        private const val MAX_SLEEP_TIME = 3000L

        private const val MAX_THRESHOLD = 3L

        private const val MAX_LATCH_TIME = 15L

        private const val NOTIFICATION_TITLE_PREFIX = "Downloading"

        private const val NOTIFICATION_ENTRY_PREFIX = "title"

        private const val DOWNLOADED_CONTENT_DESC = "Downloaded"

        private const val TEST_CONTENT_ENTRY_FILE_UID = -4103245208651563007L

        private const val THROTTLE_BYTES = 200000L
    }

}
