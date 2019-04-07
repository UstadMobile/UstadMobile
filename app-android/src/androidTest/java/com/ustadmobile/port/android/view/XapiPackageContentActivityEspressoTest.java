package com.ustadmobile.port.android.view;


import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.v4.content.ContextCompat;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static android.support.test.espresso.web.assertion.WebViewAssertions.webContent;
import static android.support.test.espresso.web.matcher.DomMatchers.hasElementWithId;
import static android.support.test.espresso.web.sugar.Web.onWebView;


public class XapiPackageContentActivityEspressoTest {

    @Rule
    public IntentsTestRule<XapiPackageContentActivity> mActivityRule =
            new IntentsTestRule<>(XapiPackageContentActivity.class, false, false);


    private UmAppDatabase db;

    private UmAppDatabase repo;

    private UmFileUtilSe.TempZipContainer tempXapiPackageContainer;

    @Before
    public void setup() throws IOException {
        db = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        File storageDir = ContextCompat.getExternalFilesDirs(
                InstrumentationRegistry.getTargetContext(), null)[0];
        File containerTmpDir = new File(storageDir, "XapiPackageCOntentActivityEspressoTest."
            + System.currentTimeMillis());
        containerTmpDir.mkdirs();

        tempXapiPackageContainer = UmFileUtilSe.makeTempContainerFromClassResource(db, repo,
                "/com/ustadmobile/port/android/view/XapiPackage-JsTetris_TCAPI.zip",
                containerTmpDir);
    }

    @After
    public void tearDown() throws IOException{
        if(tempXapiPackageContainer != null)
            UmFileUtilSe.deleteRecursively(tempXapiPackageContainer.getContainerFileDir());
    }

    public void launchActivity() {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(XapiPackageContentView.ARG_CONTAINER_UID,
                String.valueOf(tempXapiPackageContainer.getContainer().getContainerUid()));
        mActivityRule.launchActivity(launchIntent);
    }

    @Test
    public void givenValidXapiZip_whenCreated_thenShouldShowContents() {
        launchActivity();
        SystemClock.sleep(1000);
        onWebView().check(webContent(hasElementWithId("tetris")));
    }


}
