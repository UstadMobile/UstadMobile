package com.toughra.ustadmobile;

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

    public CatalogActivityTest() {
        super(CatalogActivity.class);
    }
    @Override
    protected void setUp() throws Exception{
        super.setUp();

        //load impl with skipping the locale load (cant be done before an activity is created)
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance(true);
        String opdsURL = TestUtils.getInstance().getHTTPRoot() + TestConstants.CATALOG_OPDS_ROOT;

        Intent intent = new Intent();
        intent.putExtra(CatalogActivity.EXTRA_CATALOGURL, opdsURL);
        intent.putExtra(CatalogActivity.EXTRA_RESMODE, CatalogController.USER_RESOURCE);
        intent.putExtra(UstadMobileSystemImplAndroid.KEY_CURRENTUSER, TestConstants.LOGIN_USER);
        intent.putExtra(UstadMobileSystemImplAndroid.KEY_CURRENTAUTH, TestConstants.LOGIN_PASS);

        setActivityIntent(intent);
    }


    public void testCatalogActivity() {
        assertNotNull(getActivity());
    }

}
