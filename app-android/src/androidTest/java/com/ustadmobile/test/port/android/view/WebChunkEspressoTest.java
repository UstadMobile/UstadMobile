package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.port.android.view.WebChunkActivity;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.UmZipUtils;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;

import static android.support.test.espresso.web.sugar.Web.onWebView;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.test.port.android.UmAndroidTestUtil.readAllFilesInDirectory;

@RunWith(AndroidJUnit4.class)
public class WebChunkEspressoTest {

    @Rule
    public IntentsTestRule<WebChunkActivity> mActivityRule =
            new IntentsTestRule<>(WebChunkActivity.class, false, false);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION);

    String path = "/DummyView?/ContentEntryList?entryid=40/ContentEntryList?entryid=41/ContentEntryDetail?entryid=10/ContentEntryDetail?entryid=11/webChunk?";
    private File tmpDir;
    private UmAppDatabase repo;
    private UmAppDatabase db;
    private File dir;


    public UmAppDatabase getDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        return db.getRepository("https://localhost", "");
    }

    public void createDummyContent() throws IOException {
        repo = getDb();

        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContainerDao containerDao = repo.getContainerDao();

        UmAppDatabase app = UmAppDatabase.getInstance(null);
        ContentEntryStatusDao statusDao = app.getContentEntryStatusDao();

        dir = Environment.getExternalStorageDirectory();
        tmpDir = Files.createTempDirectory("testWebChunk").toFile();
        tmpDir.mkdirs();

        File countingFolder = new File(tmpDir, "counting-out");
        countingFolder.mkdirs();
        File chunkCountingOut = new File(tmpDir, "counting-out.zip");
        unZipAndCreateManager(countingFolder, chunkCountingOut, 1L, 10,
                getClass().getResourceAsStream("/com/ustadmobile/app/android/counting-out-1-20-objects.zip"));


        ContentEntryStatus targetEntryStatusComplete = new ContentEntryStatus();
        targetEntryStatusComplete.setCesLeaf(true);
        targetEntryStatusComplete.setDownloadStatus(JobStatus.COMPLETE);
        targetEntryStatusComplete.setTotalSize(1000);
        targetEntryStatusComplete.setBytesDownloadSoFar(1000);
        targetEntryStatusComplete.setCesUid(1L);
        statusDao.insert(targetEntryStatusComplete);

        ContentEntry targetEntry = new ContentEntry();
        targetEntry.setContentEntryUid(1L);
        targetEntry.setTitle("tiempo de prueba");
        targetEntry.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        targetEntry.setDescription("todo el contenido");
        targetEntry.setPublisher("CK12");
        targetEntry.setAuthor("borrachera");
        targetEntry.setPrimaryLanguageUid(53);
        targetEntry.setLeaf(true);
        contentDao.insert(targetEntry);


        File countingObjectsFolder = new File(tmpDir, "counting-objects");
        countingObjectsFolder.mkdirs();
        File chunkcountingObjects = new File(tmpDir, "counting-objects.zip");
        unZipAndCreateManager(countingObjectsFolder, chunkcountingObjects, 3L, 11,
                getClass().getResourceAsStream("/com/ustadmobile/app/android/counting-objects.zip"));

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

    private void unZipAndCreateManager(File countingFolder, File chunkCountingOut, long contentEntryUid, int containerUid, InputStream resourceAsStream) throws IOException {

        FileUtils.copyInputStreamToFile(
                resourceAsStream,
                chunkCountingOut);

        UmZipUtils.unzip(chunkCountingOut, countingFolder);
        HashMap<File, String> countingMap = new HashMap<>();
        readAllFilesInDirectory(countingFolder, countingMap);

        Container container = new Container();
        container.setMimeType("application/webchunk+zip");
        container.setContainerContentEntryUid(contentEntryUid);
        container.setContainerUid(containerUid);
        repo.getContainerDao().insert(container);

        ContainerManager manager = new ContainerManager(container, db,
                repo, dir.getAbsolutePath());
        manager.addEntries(countingMap, true);
    }


    @Test
    public void givenServerOffline_whenKhanExerciseZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        createDummyContent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();

        b.putString(WebChunkView.ARG_CONTAINER_UID, String.valueOf(10L));
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1L));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);


        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


    @Test
    public void givenServerOffline_whenNewKhanExerciseZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        createDummyContent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();

        File countingFolder = new File(tmpDir, "review");
        countingFolder.mkdirs();
        File chunkCountingOut = new File(tmpDir, "review-out.zip");
        unZipAndCreateManager(countingFolder, chunkCountingOut, 1L, 12,
                getClass().getResourceAsStream("/com/ustadmobile/app/android/comparison-symbols-review.zip"));


        b.putString(WebChunkView.ARG_CONTAINER_UID, String.valueOf(12));
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

        createDummyContent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();

        b.putString(WebChunkView.ARG_CONTAINER_UID, String.valueOf(11L));
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(1l));
        b.putString(ARG_REFERRER, path);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


}
