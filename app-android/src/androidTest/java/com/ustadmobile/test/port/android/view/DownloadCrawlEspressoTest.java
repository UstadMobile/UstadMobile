package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;


import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.port.android.view.BasePointActivity;
import com.ustadmobile.port.android.view.CatalogOPDSFragment;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;
import com.ustadmobile.test.port.android.UmViewActions;
import com.ustadmobile.test.sharedse.http.UmHttpTestServerClient;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.test.sharedse.http.UmHttpTestServerClient.throttleParams;
import static com.ustadmobile.test.sharedse.http.UmHttpTestServerClient.throttleServer;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

/**
 * This UI test runs through adding a library and downloading it (recursively).
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class DownloadCrawlEspressoTest {

    @Rule
    public ActivityTestRule<BasePointActivity> mActivityRule =
            new ActivityTestRule<>(BasePointActivity.class);

    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String RES_PATH_CRAWLABLE_FEED =
            "com/ustadmobile/test/sharedse/crawlme/index.opds";

    private static final String CRAWLABLE_FEED_TITLE = "Crawl Test";

    private static final int THROTTLE_BYTES_PER_SECOND = 128000;

    @BeforeClass
    public static void startResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @Before
    public void resetTimeout() {
        IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS);
    }

    private void addFeedToMyLibraries(String urlPath, String expectedTitle){
        onView(withText(R.string.my_libraries)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.activity_basepoint_fab)).check(matches(isDisplayed()));

        onView(withId(R.id.activity_basepoint_fab)).perform(click());

        onView(withId(R.id.fragment_add_feed_dialog_spinner)).perform(click());
        onView(withText(containsString("URL"))).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.fragment_add_feed_url_text)).check(matches(isDisplayed()));

        String rootOpdsUrl = UMFileUtil.joinPaths(urlPath);
        onView(withId(R.id.fragment_add_feed_url_text)).perform(typeText(rootOpdsUrl));
        onView(withText(R.string.add)).perform(click());

        SystemClock.sleep(2000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                withId(R.id.fragment_catalog_recyclerview)
        )).check(matches(hasDescendant(withText(expectedTitle))));
    }

    private void deleteAndRemoveFromMyLibraries(String libraryTitle) {
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(libraryTitle)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.fragment_download_dialog_main_text)).check(
                matches(withText(containsString("Delete"))));
        onView(withId(android.R.id.button1)).perform(click());

        //Now check the downloads have been deleted - status text should be "Download"
        SystemClock.sleep(3000);
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(libraryTitle)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Download")));

        removeFromMyLibrary(libraryTitle);
    }

    private void removeFromMyLibrary(String libraryTitle) {
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                withChild(withText(libraryTitle)))).perform(longClick());
        onView(withId(CatalogOPDSFragment.MENUCMDID_DELETE)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }

    private void startDownload(String libraryTitle, boolean wifiOnly) {
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(libraryTitle)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());

        onView(withId(R.id.fragment_download_dialog_download_wifi_only)).perform(
                UmViewActions.setChecked(wifiOnly));

        onView(withId(android.R.id.button1)).perform(click());

        SystemClock.sleep(1000);
    }

    private void startDownload(String libraryTitle){
        startDownload(libraryTitle, false);
    }

    public void clickDownloadIconAndSelectOption(String libraryTitle, int optionId) {
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(libraryTitle)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());


        onView(withId(optionId)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }


    private void waitForLastDownloadJobToFinish(final int timeout) {
        DownloadJobDao jobDao = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao();
        DownloadJob lastJob = jobDao.findLastCreatedDownloadJob();
        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<DownloadJob> lastJobObserver = (downloadJob) -> {
            if(downloadJob != null && downloadJob.getStatus() >= NetworkTask.STATUS_COMPLETE_MIN){
                latch.countDown();
            }
        };
        UmLiveData<DownloadJob> lastJobLiveData = jobDao.getByIdLive(lastJob.getDownloadJobId());
        lastJobLiveData.observeForever(lastJobObserver);

        try { latch.await(timeout, TimeUnit.MILLISECONDS); }
        catch(InterruptedException e) {}

        lastJobLiveData.removeObserver(lastJobObserver);
    }

    @Test
    public void givenEmptyList_whenAllDownloaded_shouldShowDownloaded() {
        int testServerPort = UmHttpTestServerClient.newServer();
        String opdsUrl = UMFileUtil.joinPaths(
                UmHttpTestServerClient.getServerBasePath(testServerPort),
                "com/ustadmobile/test/sharedse/crawlme/index.opds");
        addFeedToMyLibraries(opdsUrl, "Crawl Test");

        SystemClock.sleep(5000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText("Crawl Test")),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());

        SystemClock.sleep(5000);

        onView(withId(R.id.fragment_download_dialog_download_wifi_only)).perform(
                UmViewActions.setChecked(false));
        onView(withId(R.id.fragment_download_dialog_main_text)).check(
                matches(withText(containsString("4 items"))));


        onView(withId(android.R.id.button1)).perform(click());


        waitForLastDownloadJobToFinish(10000);

        //check the icon is now downloaded
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText("Crawl Test")),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Downloaded")));

        //now delete it
        deleteAndRemoveFromMyLibraries("Crawl Test");
        UmHttpTestServerClient.sendCommand("stop", testServerPort, null,
                InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void givenEmptyList_whenDownloadPausedThenResumed_shouldShowPausedThenDownloaded() {
        int testServerPort = UmHttpTestServerClient.newServer();
        String opdsUrl = UMFileUtil.joinPaths(UmHttpTestServerClient.getServerBasePath(testServerPort),
                RES_PATH_CRAWLABLE_FEED);
        addFeedToMyLibraries(opdsUrl, CRAWLABLE_FEED_TITLE);

        UmHttpTestServerClient.sendCommand("throttle", testServerPort,
                throttleParams(THROTTLE_BYTES_PER_SECOND), PlatformTestUtil.getTargetContext());

        SystemClock.sleep(1000);

        startDownload(CRAWLABLE_FEED_TITLE);

        SystemClock.sleep(1000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());


        onView(withId(R.id.fragment_download_dialog_option_pause)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        SystemClock.sleep(1000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Paused")));


        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).perform(click());

        onView(withId(R.id.fragment_download_dialog_option_resume)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        waitForLastDownloadJobToFinish(30000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Downloaded")));

        deleteAndRemoveFromMyLibraries(CRAWLABLE_FEED_TITLE);
        UmHttpTestServerClient.sendCommand("stop", testServerPort, null,
                InstrumentationRegistry.getTargetContext());
    }


    @Test
    public void givenEmptyList_whenDownloadedThenCancelled_shouldShowNotDownloaded() {
        int testServerPort = UmHttpTestServerClient.newServer();
        addFeedToMyLibraries(
                UMFileUtil.joinPaths(UmHttpTestServerClient.getServerBasePath(testServerPort),
                RES_PATH_CRAWLABLE_FEED), CRAWLABLE_FEED_TITLE);
        throttleServer(testServerPort, THROTTLE_BYTES_PER_SECOND, PlatformTestUtil.getTargetContext());

        startDownload(CRAWLABLE_FEED_TITLE);
        SystemClock.sleep(1000);
        clickDownloadIconAndSelectOption(CRAWLABLE_FEED_TITLE,
                R.id.fragment_download_dialog_option_cancel);

        SystemClock.sleep(1000);
        //Check that the content description shows the option to download - e.g all entries are deleted.
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Download")));

        removeFromMyLibrary(CRAWLABLE_FEED_TITLE);
        UmHttpTestServerClient.sendCommand("stop", testServerPort, null,
                InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void givenDownloadStarted_whenConnectivityInterrupted_shouldWaitAndResume() {
        int testServerPort = UmHttpTestServerClient.newServer();
        throttleServer(testServerPort, THROTTLE_BYTES_PER_SECOND, PlatformTestUtil.getTargetContext());
        addFeedToMyLibraries(UMFileUtil.joinPaths(
                UmHttpTestServerClient.getServerBasePath(testServerPort),
                RES_PATH_CRAWLABLE_FEED), CRAWLABLE_FEED_TITLE);
        startDownload(CRAWLABLE_FEED_TITLE);
        SystemClock.sleep(2000);

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Airplane mode on");


        //check it has paused due to the connectivity being disconnected
        SystemClock.sleep(5000);
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Queued")));

        UmAndroidTestUtil.setAirplaneModeEnabled(false);
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Airplane mode off");

        waitForLastDownloadJobToFinish(30000);

        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText(CRAWLABLE_FEED_TITLE)),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Downloaded")));
        deleteAndRemoveFromMyLibraries(CRAWLABLE_FEED_TITLE);
        UmHttpTestServerClient.sendCommand("stop", testServerPort, null,
                PlatformTestUtil.getTargetContext());
    }

    @Test
    @RequiresDevice
    public void givenDownloadStartedWifiOnly_whenWifiInterrupted_shouldWaitAndResume() {
        addFeedToMyLibraries(UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                "com/ustadmobile/test/sharedse/crawlme-slow/index.opds"), "Crawl Test - Slow");
        startDownload("Crawl Test - Slow", true);
        SystemClock.sleep(1000);



        WifiManager wifiManager = (WifiManager)InstrumentationRegistry.getContext()
                .getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        //check that after wifi being disabled the download is now queued
        SystemClock.sleep(1000);
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText("Crawl Test - Slow")),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Queued")));

        //re-enable wifi and wait for download to complete
        wifiManager.setWifiEnabled(true);

        waitForLastDownloadJobToFinish(30000);
        onView(allOf(
                isDescendantOfA(withTagValue(equalTo("entries:///my_library"))),
                hasSibling(withText("Crawl Test - Slow")),
                withId(R.id.item_opds_entry_card_download_icon)
        )).check(matches(withContentDescription("Downloaded")));
        deleteAndRemoveFromMyLibraries("Crawl Test - Slow");


    }

}
