package com.ustadmobile.core.db.entities;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.TestCaseCallbackHelper;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by mike on 1/14/18.
 */

public class TestOpdsFeed extends TestCase {

    public static final String OPDS_FEED = "com/ustadmobile/test/core/acquire-multi.opds";

//    public void testLoad() {
//        final TestCaseCallbackHelper<InputStream> callbackHelper = new TestCaseCallbackHelper<>(this);
//        callbackHelper.add(10000, () -> {
//            UstadMobileSystemImpl.getInstance().getAsset(PlatformTestUtil.getTargetContext(), OPDS_FEED,
//                    callbackHelper);
//        }).add(10000, () -> {
//            InputStream in = callbackHelper.getResult();
//            assertNotNull(in);
//            OpdsFeedWithRelations opdsFeed = new OpdsFeedWithRelations();
//            try {
//                opdsFeed.load(UstadMobileSystemImpl.getInstance().newPullParser(in), null);
//                assertEquals("Loaded correct id", "http://umcloud1.ustadmobile.com/opds/courseid/6CM",
//                        opdsFeed.getItemId());
//            }catch(XmlPullParserException x) {
//
//            }catch(IOException e) {
//
//            }
//        }).start();
//    }



}



