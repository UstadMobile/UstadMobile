package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.controller.XapiPackagePresenter;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.port.android.view.XapiPackageActivity;
import com.ustadmobile.test.core.view.TestXapiPackageView;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 12/25/17.
 */

public class TestXapiPackageViewAndroid extends TestXapiPackageView {

    private ActivityTestRule<XapiPackageActivity> activityTestRule =
            new ActivityTestRule<>(XapiPackageActivity.class, true, false);

    private static File testXapiFile;

    @BeforeClass
    public static void copyTestFile() throws IOException{
        InputStream in = null;
        FileOutputStream fout = null;
        IOException ioe = null;

        try {
            testXapiFile = File.createTempFile("TestXapiPackage", "zip");
            in = TestXapiPackageView.class.getResourceAsStream(
                    "/com/ustadmobile/test/core/JsTetris_TCAPI.zip");
            fout = new FileOutputStream(testXapiFile);
            UMIOUtils.readFully(in, fout, 8*1024);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(in);
            UMIOUtils.closeOutputStream(fout);
            UMIOUtils.throwIfNotNullIO(ioe);
        }
    }

    @AfterClass
    public static void cleanupTestFile() {
        if(testXapiFile != null)
            testXapiFile.delete();
    }

    @Override
    public XapiPackageView getView() {
        Intent launchIntent = new Intent(InstrumentationRegistry.getTargetContext(),
                XapiPackageActivity.class);
        launchIntent.putExtra(ContainerController.ARG_CONTAINERURI, testXapiFile.getAbsolutePath());

        activityTestRule.launchActivity(launchIntent);

        return activityTestRule.getActivity();
    }
}
