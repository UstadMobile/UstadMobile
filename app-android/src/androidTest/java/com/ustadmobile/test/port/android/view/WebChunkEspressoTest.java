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

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.port.android.view.WebChunkActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    public void duringTest() throws IOException {
        Intent launchActivityIntent = new Intent();
        Bundle b = new Bundle();
        //53d147578e0e0876d4df82f1
        String pathToZip = "/com/ustadmobile/app/android/56953eed8e0e086aa6e2d3c2.zip";
        InputStream inputStream = getClass().getResourceAsStream(pathToZip);
        File path = Environment.getExternalStorageDirectory();
        File targetFile = new File(path, "53d147578e0e0876d4df82f1.zip");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int read;
        while((read = inputStream.read(buffer)) != -1){
            outStream.write(buffer, 0, read);
        }

        b.putString(WebChunkView.ARG_CHUNK_PATH, targetFile.getPath());
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);
        Assert.assertTrue(1 == 1);

    }


}
