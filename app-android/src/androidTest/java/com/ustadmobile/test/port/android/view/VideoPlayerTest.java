package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.port.android.view.VideoPlayerActivity;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;

import static com.ustadmobile.test.port.android.UmAndroidTestUtil.readFromTestResources;

@RunWith(AndroidJUnit4.class)
public class VideoPlayerTest {

    @Rule
    public IntentsTestRule<VideoPlayerActivity> mActivityRule =
            new IntentsTestRule<>(VideoPlayerActivity.class, false, false);
    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION);
    private long containerUid;


    public UmAppDatabase getDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        return db.getRepository("https://localhost", "");
    }

    public void createDummyContent() throws IOException {
        UmAppDatabase db = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext());
        UmAppDatabase repo = getDb();
        ContentEntryDao contentDao = repo.getContentEntryDao();
        ContainerDao containerDao = repo.getContainerDao();

        ContentEntry spanishQuiz = new ContentEntry();
        spanishQuiz.setContentEntryUid(14);
        spanishQuiz.setTitle("tiempo de prueba");
        spanishQuiz.setThumbnailUrl("https://www.africanstorybook.org/img/asb120.png");
        spanishQuiz.setDescription("todo el contenido");
        spanishQuiz.setPublisher("CK12");
        spanishQuiz.setAuthor("borrachera");
        spanishQuiz.setPrimaryLanguageUid(3);
        spanishQuiz.setLeaf(true);
        contentDao.insert(spanishQuiz);

        File tmpDir = Files.createTempDirectory("testVideoPlayer").toFile();
        tmpDir.mkdirs();
        File videoFile = new File(tmpDir, "video1.webm");
        File audioTempFile = new File(tmpDir, "audio.c2");
        File srtTmpFile = new File(tmpDir, "subtitle.srt");

        FileUtils.copyInputStreamToFile(
                getClass().getResourceAsStream("/com/ustadmobile/app/android/video1.webm"),
                videoFile);

        FileUtils.copyInputStreamToFile(
                getClass().getResourceAsStream("/com/ustadmobile/app/android/video1-codec2-version2.c2"),
                audioTempFile);
        FileUtils.copyInputStreamToFile(
                getClass().getResourceAsStream("/com/ustadmobile/app/android/srtfile.srt"),
                srtTmpFile);

        File dir = Environment.getExternalStorageDirectory();

        Container container = new Container();
        container.setContainerContentEntryUid(14L);
        containerUid = containerDao.insert(container);
        container.setContainerUid(containerUid);

        ContainerManager manager = new ContainerManager(container, db,
                repo, dir.getAbsolutePath());

        HashMap<File, String> fileMap = new HashMap<>();
        fileMap.put(videoFile, "video1.webm");
        fileMap.put(audioTempFile, "audio.c2");
        fileMap.put(srtTmpFile, "subtitle.srt");
        manager.addEntries(fileMap, true);

    }


    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        createDummyContent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        b.putString(VideoPlayerView.ARG_CONTAINER_UID, String.valueOf(containerUid));
        b.putString(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(14L));
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        //  mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the user
    }


}
