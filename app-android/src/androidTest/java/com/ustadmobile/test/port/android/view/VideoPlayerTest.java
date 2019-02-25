package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.port.android.view.VideoPlayerActivity;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(AndroidJUnit4.class)
public class VideoPlayerTest {

    @Rule
    public IntentsTestRule<VideoPlayerActivity> mActivityRule =
            new IntentsTestRule<>(VideoPlayerActivity.class, false, false);
    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION);


    public UmAppDatabase getDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase db = UmAppDatabase.getInstance(context);
        db.clearAllTables();
        return db.getRepository("https://localhost", "");
    }

    public void createDummyContent() {
        UmAppDatabase repo = getDb();
        ContentEntryDao contentDao = repo.getContentEntryDao();


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

    }


    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        createDummyContent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        String testVideoPath = "/com/ustadmobile/app/android/video1.webm";
        InputStream inputStream = getClass().getResourceAsStream(testVideoPath);
        File path = Environment.getExternalStorageDirectory();
        File targetFile = new File(path, "video1.webm");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, read);
        }

        String testAudioPath = "/com/ustadmobile/app/android/video1-codec2-version2.c2";
        InputStream audioIs = getClass().getResourceAsStream(testAudioPath);
        File audioPath = Environment.getExternalStorageDirectory();
        File audioTargetFile = new File(audioPath, "video1-codec2-version2.c2");
        OutputStream audiooutStream = new FileOutputStream(audioTargetFile);
        byte[] audiobuffer = new byte[1024];
        int audioread;
        while ((audioread = audioIs.read(audiobuffer)) != -1) {
            audiooutStream.write(audiobuffer, 0, audioread);
        }


        b.putString(VideoPlayerView.ARG_VIDEO_PATH, targetFile.getPath());
        b.putString(VideoPlayerView.ARG_AUDIO_PATH, audioTargetFile.getPath());
        b.putLong(VideoPlayerView.ARG_CONTENT_ENTRY_ID, 14L);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        //  mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the user
    }

}
