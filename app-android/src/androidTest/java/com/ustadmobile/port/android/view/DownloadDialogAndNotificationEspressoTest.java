package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.toughra.ustadmobile.BuildConfig;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.port.android.UmViewActions;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
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
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_DOWNLOADED_CONTENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DownloadDialogAndNotificationEspressoTest {

    @Rule
    public ActivityTestRule<DummyActivity> mActivityRule
            = new ActivityTestRule<>(DummyActivity.class,false, false);

    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION);

    private static final  long MAX_WAIT_TIME = TimeUnit.SECONDS.toMillis(2);

    private ContentEntry rootEntry = null;

    private ContentEntry entry1 = null;

    private UmAppDatabase umAppDatabase;

    private Context mContext;

    private UiDevice mDevice;

    private static final String NOTIFICATION_TITLE_PREFIX = "Downloading";

    private static final String NOTIFICATION_ENTRY_PREFIX = "title";

    private static final long TEST_CONTENT_ENTRY_FILE_UID = -4103245208651563007L;


    @Before
    public void setEndpoint() throws IOException, JSONException {
        String testManagerUrl =  "http://" + BuildConfig.TEST_HOST + ":" + BuildConfig.TEST_PORT + "/";
        JSONObject testPortResponse = new JSONObject(
                IOUtils.toString(new URL(testManagerUrl + "?cmd=new").openStream(),
                        StandardCharsets.UTF_8));
        String testEndpoint =  "http://" + BuildConfig.TEST_HOST +
                ":" + testPortResponse.getString("port") + "/";
        UmAccount testAccount = new UmAccount(0, "test", "",
                testEndpoint);
        UmAccountManager.setActiveAccount(testAccount, InstrumentationRegistry.getTargetContext());

        mContext = InstrumentationRegistry.getTargetContext();
        prepareContentEntriesAndFiles();
        SystemClock.sleep(MAX_WAIT_TIME);
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Intent mIntent = new Intent();
        mIntent.putExtra(ARG_CONTENT_ENTRY_UID, rootEntry.getContentEntryUid());
        mIntent.putExtra(ARG_DOWNLOADED_CONTENT, "");
        mActivityRule.launchActivity(mIntent);
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
        entryFile.setFileSize(2800000);
        entryFile.setContentEntryFileUid(-3831212382533713414L);
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
    public void givenDownloadIconClickedOnEntryListItem_whenDownloading_shouldStartForegroundServiceAndShowNotification(){
        SystemClock.sleep(MAX_WAIT_TIME);
        startDownloading(true);

        boolean openNotificationTray =  mDevice.openNotification();

        SystemClock.sleep(MAX_WAIT_TIME);

        mDevice.wait(Until.hasObject(By.textContains(NOTIFICATION_TITLE_PREFIX)), MAX_WAIT_TIME);

        UiObject2 title = mDevice.findObject(By.textContains(NOTIFICATION_TITLE_PREFIX));

        UiObject2 entryName = mDevice.findObject(By.textContains(NOTIFICATION_ENTRY_PREFIX));

        assertTrue("Notification tray was opened ",openNotificationTray);

        assertTrue("Download notification was shown",
                title.getText().contains(NOTIFICATION_TITLE_PREFIX));

        assertEquals("Notification shown was for  " + entry1.getTitle(),
                entryName.getText(), entry1.getTitle());


    }

    public void givenDownloadIconClickedOnEntryListItem_whenDownloadCompleted_shouldChangeTheIcons(){
        SystemClock.sleep(MAX_WAIT_TIME);
        startDownloading(true);

        SystemClock.sleep(MAX_WAIT_TIME * 3);
    }

    @RequiresDevice
    public void givenDownloadIconClickedOnEntryListItem_whenDownloadingAndWiFiConnectionGoesOff_shouldStopDownloading(){

    }

    private void startDownloading(boolean wifiOnly){
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo(entry1.getContentEntryUid()))),
                withId(R.id.content_entry_item_download)
        )).perform(click());

        SystemClock.sleep(MAX_WAIT_TIME);

        onView(withId(R.id.wifi_only_option)).perform(
                UmViewActions.setChecked(wifiOnly));

        onView(withId(android.R.id.button1)).perform(click());

        SystemClock.sleep(MAX_WAIT_TIME);
    }
}
