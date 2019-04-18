package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.view.WebChunkActivity;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.test.port.android.UmAndroidTestUtil.readFromTestResources;

@RunWith(AndroidJUnit4.class)
public class WebChunkEspressoTest {

    @Rule
    public IntentsTestRule<WebChunkActivity> mActivityRule =
            new IntentsTestRule<>(WebChunkActivity.class, false, false);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION);

    String path = "/DummyView?/ContentEntryList?entryid=40/ContentEntryList?entryid=41/ContentEntryDetail?entryid=42/ContentEntryDetail?entryid=43";


    public UmAppDatabase getDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        return db.getRepository("https://localhost", "");
    }

    public void createDummyContent() throws IOException {
        UmAppDatabase repo = getDb();

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContentEntryFileDao contentFileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryFileDao fileDao = repo.getContentEntryFileDao();

        UmAppDatabase app = UmAppDatabase.getInstance(null);
        ContentEntryStatusDao statusDao = app.getContentEntryStatusDao();
        ContentEntryFileStatusDao fileStatusDao = app.getContentEntryFileStatusDao();

        File targetFile = readFromTestResources(
                "/com/ustadmobile/app/android/counting-out-1-20-objects.zip",
                "counting-out-1-20-objects.zip");

        ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
        fileStatus.setCefsUid(1);
        fileStatus.setCefsContentEntryFileUid(2);
        fileStatus.setFilePath(targetFile.getPath());
        fileStatusDao.insert(fileStatus);

        ContentEntryContentEntryFileJoin join = new ContentEntryContentEntryFileJoin();
        join.setCecefjContentEntryFileUid(2);
        join.setCecefjContentEntryUid(1);
        join.setCecefjUid(3435);
        contentEntryFileJoinDao.insert(join);

        ContentEntryFile contentEntryTarget = new ContentEntryFile();
        contentEntryTarget.setMimeType("application/webchunk+zip");
        contentEntryTarget.setLastModified(System.currentTimeMillis());
        contentEntryTarget.setContentEntryFileUid(2);
        fileDao.insert(contentEntryTarget);

        ContentEntryStatus targetEntryStatusComplete = new ContentEntryStatus();
        targetEntryStatusComplete.setCesLeaf(true);
        targetEntryStatusComplete.setDownloadStatus(JobStatus.COMPLETE);
        targetEntryStatusComplete.setTotalSize(1000);
        targetEntryStatusComplete.setBytesDownloadSoFar(1000);
        targetEntryStatusComplete.setCesUid(1);
        statusDao.insert(targetEntryStatusComplete);

        ContentEntry targetEntry = new ContentEntry();
        targetEntry.setContentEntryUid(1);
        targetEntry.setTitle("tiempo de prueba");
        targetEntry.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        targetEntry.setDescription("todo el contenido");
        targetEntry.setPublisher("CK12");
        targetEntry.setAuthor("borrachera");
        targetEntry.setPrimaryLanguageUid(53);
        targetEntry.setLeaf(true);
        contentDao.insert(targetEntry);

        File countingObjects = readFromTestResources(
                "/com/ustadmobile/app/android/counting-objects.zip",
                "counting-objects.zip");


        ContentEntryFileStatus countingFileStatus = new ContentEntryFileStatus();
        countingFileStatus.setCefsUid(3);
        countingFileStatus.setCefsContentEntryFileUid(4);
        countingFileStatus.setFilePath(countingObjects.getPath());
        fileStatusDao.insert(countingFileStatus);

        ContentEntryContentEntryFileJoin secondFileJoin = new ContentEntryContentEntryFileJoin();
        secondFileJoin.setCecefjContentEntryFileUid(4);
        secondFileJoin.setCecefjContentEntryUid(3);
        secondFileJoin.setCecefjUid(3434);
        contentEntryFileJoinDao.insert(secondFileJoin);


        ContentEntryFile contentEntryCounting = new ContentEntryFile();
        contentEntryCounting.setMimeType("application/webchunk+zip");
        contentEntryCounting.setFileSize(10);
        contentEntryCounting.setLastModified(1540728218);
        contentEntryCounting.setContentEntryFileUid(4);
        contentFileDao.insert(contentEntryCounting);

        ContentEntryStatus countingEntryStatusComplete = new ContentEntryStatus();
        countingEntryStatusComplete.setCesLeaf(true);
        countingEntryStatusComplete.setDownloadStatus(JobStatus.COMPLETE);
        countingEntryStatusComplete.setTotalSize(1000);
        countingEntryStatusComplete.setBytesDownloadSoFar(1000);
        countingEntryStatusComplete.setCesUid(3);
        statusDao.insert(countingEntryStatusComplete);

        ContentEntry countingEntry = new ContentEntry();
        countingEntry.setContentEntryUid(3);
        countingEntry.setTitle("tiempo de prueba");
        countingEntry.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        countingEntry.setDescription("todo el contenido");
        countingEntry.setPublisher("CK12");
        countingEntry.setAuthor("borrachera");
        countingEntry.setPrimaryLanguageUid(345);
        countingEntry.setSourceUrl("khan-id://x7d37671e");
        countingEntry.setLeaf(true);
        contentDao.insert(countingEntry);


    }


    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();

        File targetFile = readFromTestResources(
                "/com/ustadmobile/app/android/plix-scraped-content.zip",
                "plix-scraped-content.zip");

        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1l));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the user
        onWebView()
                .withTimeout(1000000, TimeUnit.MILLISECONDS)
                .withElement(findElement(Locator.CLASS_NAME, "questionController"))
                .perform(webClick());
    }

    @Test
    public void givenServerOffline_whenKhanExerciseZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        File targetFile = readFromTestResources(
                "/com/ustadmobile/app/android/counting-objects.zip",
                "counting-objects.zip");

        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1l));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);


        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


    @Test
    public void givenServerOffline_whenNewKhanExerciseZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        File targetFile = readFromTestResources(
                "/com/ustadmobile/app/android/comparison-symbols-review.zip",
                "comparison-symbols-review.zip");


        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1l));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }

    @Test
    public void givenServerOffline_whenNewKhanArticleZippedIsOpened_WebviewLoads() throws IOException {

        createDummyContent();
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        File targetFile = readFromTestResources(
                "/com/ustadmobile/app/android/counting-out-1-20-objects.zip",
                "counting-out-1-20-objects.zip");

        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1l));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


}
