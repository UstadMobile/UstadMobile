package com.toughra.ustadmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.view.CatalogActivity;
import com.ustadmobile.test.core.TestConstants;
import com.ustadmobile.test.core.TestUtils;

import java.util.Locale;

/**
 * Created by mike on 9/22/15.
 */
public class CatalogActivityTest extends ActivityInstrumentationTestCase2<CatalogActivity> {

    public static final int SLEEP_AFTER = 10000;

    public CatalogActivityTest() {
        super(CatalogActivity.class);
    }
    @Override
    protected void setUp() throws Exception{
        super.setUp();

        //load impl with skipping the locale load (cant be done before an activity is created)
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String opdsURL = TestUtils.getInstance().getHTTPRoot() + TestConstants.CATALOG_OPDS_ROOT;

        Intent intent = new Intent();
        intent.putExtra(CatalogController.KEY_URL, opdsURL);
        intent.putExtra(CatalogController.KEY_RESMOD, CatalogController.SHARED_RESOURCE);
        intent.putExtra(CatalogController.KEY_FLAGS, CatalogController.CACHE_ENABLED);

        setActivityIntent(intent);
    }


    public void testCatalogActivity() {
        assertNotNull(getActivity());

        /*
         * Not sleeping here causes an illegalstateexception in Android 2.3: what seems to happen
         * is that the system calls onSaveInstanceState... then the catalog itself loads and then
         * we have trouble - the activity is already over.
         */
        try { Thread.sleep(SLEEP_AFTER); }
        catch(InterruptedException e) {}
    }

}
