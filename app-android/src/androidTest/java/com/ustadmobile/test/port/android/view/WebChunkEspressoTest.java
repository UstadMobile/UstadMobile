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

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.port.android.view.WebChunkActivity;
import com.ustadmobile.test.port.android.UmAndroidTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;

@RunWith(AndroidJUnit4.class)
public class WebChunkEspressoTest {

    @Rule
    public IntentsTestRule<WebChunkActivity> mActivityRule =
            new IntentsTestRule<>(WebChunkActivity.class, false, false);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION);


    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        //Start the activity


    }

    @Test
    public void givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() throws IOException {
        Intent launchActivityIntent = new Intent();

        UmAndroidTestUtil.setAirplaneModeEnabled(true);
        Bundle b = new Bundle();
        String testScrapedPlixPath = "/com/ustadmobile/app/android/plix-scraped-content.zip";
        InputStream inputStream = getClass().getResourceAsStream(testScrapedPlixPath);
        File path = Environment.getExternalStorageDirectory();
        File targetFile = new File(path, "plix-scraped-content.zip");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int read;
        while((read = inputStream.read(buffer)) != -1){
            outStream.write(buffer, 0, read);
        }

        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the user
        onWebView()
                .withTimeout(1000000, TimeUnit.MILLISECONDS)
                .withElement(findElement(Locator.CLASS_NAME,"questionController"))
                .perform(webClick());
    }


}
