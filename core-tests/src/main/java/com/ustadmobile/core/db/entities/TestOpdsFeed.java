package com.ustadmobile.core.db.entities;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.test.core.TestCaseCallbackHelper;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import org.junit.Assert;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by mike on 1/14/18.
 */

public class TestOpdsFeed extends TestCase {

    //TODO: Assets for test classes also need assembled from core using the gradle build script
    public static final String OPDS_FEED = "/com/ustadmobile/test/core/acquire-multi.opds";

    private class ItemListCallback implements OpdsEntry.OpdsItemLoadCallback{

        private ArrayList<OpdsEntryWithRelations> entryList = new ArrayList<>();

        @Override
        public void onDone(OpdsEntry item) {

        }

        @Override
        public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {
            entryList.add(childEntry);
        }

        @Override
        public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {

        }

        @Override
        public void onError(OpdsEntry item, Throwable cause) {

        }

        private List<OpdsEntryWithRelations> getEntryList(){
            return entryList;
        }
    }

    public void testLoad() {
        final TestCaseCallbackHelper<InputStream> callbackHelper = new TestCaseCallbackHelper<>(this);
        callbackHelper.add(10000, () -> {
            UstadMobileSystemImpl.getInstance().getAsset(PlatformTestUtil.getTargetContext(), OPDS_FEED,
                    callbackHelper);
        }).add(10000, () -> {
            InputStream in = callbackHelper.getResult();
            assertNotNull(in);
            OpdsEntryWithRelations opdsFeed = new OpdsEntryWithRelations();
            try {
                ItemListCallback listCallback = new ItemListCallback();
                opdsFeed.load(UstadMobileSystemImpl.getInstance().newPullParser(in), listCallback);
                assertEquals("Loaded correct id", "http://umcloud1.ustadmobile.com/opds/courseid/6CM",
                        opdsFeed.getEntryId());
                assertTrue("Loaded child entries", listCallback.getEntryList().size() > 0);

                XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                serializer.setOutput(bout, "UTF-8");
                opdsFeed.serializeFeed(serializer, listCallback.getEntryList());
                byte[] feedSerialized = bout.toByteArray();
                String feedSerializedStr = new String(bout.toByteArray(), "UTF-8");

                //now load it and test that it still works
                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser();
                xpp.setInput(new ByteArrayInputStream(feedSerialized), "UTF-8");
                OpdsEntryWithRelations loadedFeed = new OpdsEntryWithRelations();
                ItemListCallback loadedListCallback = new ItemListCallback();
                loadedFeed.load(xpp, loadedListCallback);
                Assert.assertEquals("Got the same entry id when loading from serialized string",
                        loadedFeed.getEntryId(), opdsFeed.getEntryId());

                Assert.assertEquals("Same number of entries in feed loaded from serialized string",
                        listCallback.getEntryList().size(), loadedListCallback.getEntryList().size());
            }catch(XmlPullParserException x) {
                x.printStackTrace();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }).add(10000, () ->{

        }).start();
    }



}



