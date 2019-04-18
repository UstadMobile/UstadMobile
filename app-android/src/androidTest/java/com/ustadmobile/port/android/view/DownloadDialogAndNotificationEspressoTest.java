package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.toughra.ustadmobile.BuildConfig;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;
import com.ustadmobile.test.port.android.UmViewActions;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_DOWNLOADED_CONTENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test class to make sure DownloadDialog and DownloadNotification behaves as expected on devices.
 *
 * <b>NOTE:</b>
 *
 * When doing RecyclerView based espresso checking
 * {@link DownloadDialogAndNotificationEspressoTest#givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons() },
 * you must to use {@link ViewMatchers#isDisplayed()} on view matcher,
 * otherwise matcher will match match multiple view with the same Id and test will fail.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DownloadDialogAndNotificationEspressoTest {

    @Rule
    public ActivityTestRule<DummyActivity> mActivityRule
            = new ActivityTestRule<>(DummyActivity.class,false, false);

    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    private static final  long MIN_SLEEP_TIME = TimeUnit.SECONDS.toMillis(1);

    private static final  long MAX_SLEEP_TIME = TimeUnit.SECONDS.toMillis(3);

    private static final int MAX_THRESHOLD = 3;

    private static final int MAX_LATCH_TIME = 15;

    private ContentEntry rootEntry = null;

    private ContentEntry entry1 = null;

    private UmAppDatabase umAppDatabase;

    private Context mContext;

    private UiDevice mDevice;

    private static final String NOTIFICATION_TITLE_PREFIX = "Downloading";

    private static final String NOTIFICATION_ENTRY_PREFIX = "title";

    private static final String DOWNLOADED_CONTENT_DESC = "Downloaded";

    private static final long TEST_CONTENT_ENTRY_FILE_UID = -4103245208651563007L;

    private String testManagerUrl = null;

    private int serverActivePort = 0;

    private static final int THROTTLE_BYTES = 200000;

    private boolean connectedToUnmeteredConnection = false;


    @Before
    public void setEndpoint() throws IOException, JSONException {
        mContext = InstrumentationRegistry.getTargetContext();

        //check active network

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null){
            connectedToUnmeteredConnection = wifiManager.getConnectionInfo()
                    .getSSID().toLowerCase().contains("androidwifi");
        }

        testManagerUrl =  "http://" + BuildConfig.TEST_HOST + ":" + BuildConfig.TEST_PORT + "/";

        JSONObject response = sendCommand("new",0);
        serverActivePort = Integer.parseInt(response.getString("port"));

        String testEndpoint =  "http://" + BuildConfig.TEST_HOST +
                ":" + serverActivePort + "/";

        UmAccount testAccount = new UmAccount(0, "test", "",
                testEndpoint);
        UmAccountManager.setActiveAccount(testAccount, InstrumentationRegistry.getTargetContext());

        prepareContentEntriesAndFiles();
        SystemClock.sleep(MIN_SLEEP_TIME);


        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Intent mIntent = new Intent();
        mIntent.putExtra(ARG_CONTENT_ENTRY_UID, rootEntry.getContentEntryUid());
        mIntent.putExtra(ARG_DOWNLOADED_CONTENT, "");
        mActivityRule.launchActivity(mIntent);

    }

    @After
    public void closeNotificationPanel(){
        if(mDevice != null){
            mDevice.pressBack();
        }
    }

    private JSONObject sendCommand(String command,int bytespersecond) throws IOException, JSONException {
        return new JSONObject(IOUtils.toString(new URL(testManagerUrl +  "?cmd=" +
                (bytespersecond == 0 ? command : command + "&bytespersecond=" + bytespersecond
                        + "&port=" + serverActivePort)).openStream(), StandardCharsets.UTF_8));
    }

    private void startDownloading(boolean wifiOnly){
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo(entry1.getContentEntryUid()))),
                withId(R.id.content_entry_item_download)
        )).perform(click());

        SystemClock.sleep(MIN_SLEEP_TIME);

        onView(withId(R.id.wifi_only_option)).perform(
                UmViewActions.setChecked(wifiOnly));

        onView(withId(android.R.id.button1)).perform(click());
    }


    private void prepareContentEntriesAndFiles(){
        UmAppDatabase.getInstance(mContext).clearAllTables();
        umAppDatabase = UmAppDatabase.getInstance(mContext);
        rootEntry = new ContentEntry("Lorem ipsum title",
                "Lorem ipsum description",false,true);
        rootEntry.setContentEntryUid(TEST_CONTENT_ENTRY_FILE_UID);
        umAppDatabase.getContentEntryDao().insert(rootEntry);

        entry1 = new ContentEntry("title 1", "description 1", true, true);
        ContentEntry entry2 = new ContentEntry("title 2", "description 2", true, true);
        ContentEntry entry3 = new ContentEntry("title 3", "description 3", true, false);
        ContentEntry entry4 = new ContentEntry("title 4", "description 4", true, false);

        entry1.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry1));
        entry2.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry2));
        entry3.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry3));
        entry4.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry4));

        umAppDatabase.getContentEntryParentChildJoinDao().insertList(Arrays.asList(
                new ContentEntryParentChildJoin(rootEntry, entry1, 0),
                new ContentEntryParentChildJoin(rootEntry, entry2, 0),
                new ContentEntryParentChildJoin(rootEntry, entry3, 0),
                new ContentEntryParentChildJoin(rootEntry, entry4, 0)
        ));

        ContentEntryFile entryFile = new ContentEntryFile();
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
        fileJoin4.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin4));

    }

    @Test
    public void givenDownloadIconClickedOnEntryListItem_whenDownloading_shouldStartForegroundServiceAndShowNotification()
            throws IOException, JSONException {
        SystemClock.sleep(MIN_SLEEP_TIME);

        sendCommand("throttle",THROTTLE_BYTES);

        startDownloading(connectedToUnmeteredConnection);

        SystemClock.sleep(MIN_SLEEP_TIME);

        boolean openNotificationTray =  mDevice.openNotification();

        mDevice.wait(Until.hasObject(By.textContains(NOTIFICATION_TITLE_PREFIX)), MIN_SLEEP_TIME);

        UiObject2 title = mDevice.findObject(By.textContains(NOTIFICATION_TITLE_PREFIX));

        UiObject2 entryName = mDevice.findObject(By.textContains(NOTIFICATION_ENTRY_PREFIX));

        assertTrue("Notification tray was opened ",openNotificationTray);

        assertTrue("Download notification was shown",
                title.getText().contains(NOTIFICATION_TITLE_PREFIX));

        assertEquals("Notification shown was for  " + entry1.getTitle(),
                entryName.getText(), entry1.getTitle());

        SystemClock.sleep(MIN_SLEEP_TIME);


    }

    @Test
    public void givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons()
            throws IOException, JSONException {
        SystemClock.sleep(MIN_SLEEP_TIME);

        sendCommand("throttle",THROTTLE_BYTES);

        startDownloading(connectedToUnmeteredConnection);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao().getLastJobLive(),
                MAX_THRESHOLD,TimeUnit.MINUTES, downloadJob -> downloadJob!=null
                        && downloadJob.getDjStatus() == JobStatus.COMPLETE);

        SystemClock.sleep(MIN_SLEEP_TIME);

        onView(allOf(isDisplayed(),
                isDescendantOfA(withTagValue(equalTo(entry1.getContentEntryUid()))),
                withId(R.id.view_download_status_button_img)
        )).check(matches(withContentDescription(equalTo(DOWNLOADED_CONTENT_DESC))));

    }


    @Test
    public void givenDownloadIconClickedOnEntryListItem_whenDownloadingAndConnectionChangedToMetered_shouldStopDownloading()
            throws IOException, JSONException {

        Assume.assumeTrue("Device is connected on metered connection, can execute the test"
                ,connectedToUnmeteredConnection);

        sendCommand("throttle",THROTTLE_BYTES);

        SystemClock.sleep(MIN_SLEEP_TIME);

        startDownloading(connectedToUnmeteredConnection);

        UmAndroidTestUtil.setAirplaneModeEnabled(true);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao().getLastJobLive(),
                MAX_LATCH_TIME,TimeUnit.SECONDS, downloadJob -> downloadJob != null
                        && downloadJob.getDjStatus() == JobStatus.WAITING_FOR_CONNECTION);

        assertEquals("Download task was paused and waiting for connectivity",
                umAppDatabase.getDownloadJobDao().getLastJob().getDjStatus(),
                JobStatus.WAITING_FOR_CONNECTION);


        //reset for next test
        UmAndroidTestUtil.setAirplaneModeEnabled(false);
        WaitForLiveData.observeUntil(umAppDatabase.getConnectivityStatusDao().getStatusLive(),
                MAX_LATCH_TIME,TimeUnit.SECONDS, status -> status != null
                        && status.getConnectivityState() != ConnectivityStatus.STATE_DISCONNECTED);

    }


    @Test
    public void givenDownloadStarted_whenConnectivityInterrupted_shouldResumeAndCompleteDownload()
            throws IOException, JSONException {

        SystemClock.sleep(MIN_SLEEP_TIME);

        sendCommand("throttle",THROTTLE_BYTES * MAX_THRESHOLD);

        startDownloading(connectedToUnmeteredConnection);

        UmAndroidTestUtil.setAirplaneModeEnabled(true);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao().getLastJobLive(),
                MAX_LATCH_TIME, TimeUnit.SECONDS, downloadJob -> downloadJob != null
                        && downloadJob.getDjStatus() == JobStatus.WAITING_FOR_CONNECTION);

        UmAndroidTestUtil.setAirplaneModeEnabled(false);

        SystemClock.sleep(MAX_SLEEP_TIME);

        WaitForLiveData.observeUntil(umAppDatabase.getConnectivityStatusDao().getStatusLive(),
                MAX_LATCH_TIME ,TimeUnit.SECONDS, status -> status != null
                        && status.getConnectivityState() != ConnectivityStatus.STATE_DISCONNECTED);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao().getLastJobLive(),
                MAX_LATCH_TIME, TimeUnit.SECONDS,
                downloadJob -> downloadJob != null
                        && downloadJob.getDjStatus() == JobStatus.COMPLETE);

        assertEquals("Download task was completed successfully",
                umAppDatabase.getDownloadJobDao().getLastJob().getDjStatus(),JobStatus.COMPLETE);
        SystemClock.sleep(MIN_SLEEP_TIME);
    }

}
