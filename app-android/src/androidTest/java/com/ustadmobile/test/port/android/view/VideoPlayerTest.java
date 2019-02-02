package com.ustadmobile.test.port.android.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.view.VideoPlayerView;
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
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        String testVideoPath = "/com/ustadmobile/app/android/video.mp4";
        InputStream inputStream = getClass().getResourceAsStream(testVideoPath);
        File path = Environment.getExternalStorageDirectory();
        File targetFile = new File(path, "video.mp4");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int read;
        while((read = inputStream.read(buffer)) != -1){
            outStream.write(buffer, 0, read);
        }

        b.putString(VideoPlayerView.ARG_VIDEO_PATH, targetFile.getPath());
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);


        mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the user
    }

}
