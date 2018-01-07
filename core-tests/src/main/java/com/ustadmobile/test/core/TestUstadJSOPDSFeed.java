package com.ustadmobile.test.core;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Created by mike on 4/22/17.
 */
public class TestUstadJSOPDSFeed {

    public static final int LOAD_TIMEOUT = 1000000;


    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    private UstadJSOPDSFeed loadAcquireMultiFeed(String srcHref) throws Exception {
        InputStream opdsIn = getClass().getResourceAsStream("/com/ustadmobile/test/core/acquire-multi.opds");
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed(srcHref);
        XmlPullParser parser = new KXmlParser();
        parser.setInput(opdsIn, "UTF-8");
        feed.loadFromXpp(parser, null);
        return feed;

    }

    @Test
    public void testFindLinkLanguages() throws Exception{
        UstadJSOPDSFeed feed = loadAcquireMultiFeed(
                "http://www.ustadmobile.com/files/test/acquire-multi.opds");

        UstadJSOPDSEntry entry3 = feed.getEntry(2);
        Vector languageList = entry3.getHrefLanguagesFromLinks(entry3.getAcquisitionLinks(), null);
        Assert.assertEquals("Found 3 languages for entry", 3, languageList.size());
        String[] languages = new String[]{"en", "fa", "ps"};
        for(int i = 0; i < languages.length; i++) {
            Assert.assertTrue("Found " + languages[i] + " in list", languageList.contains(languages[i]));
        }

        languageList = feed.getLinkHrefLanguageOptions(UstadJSOPDSItem.LINK_ACQUIRE, null, true, true, null);
        Assert.assertEquals("Found 4 languages for feed (null for items with no specified language)",
                4, languageList.size());
        languages = new String[]{"en", "fa", "ps", null};
        for(int i = 0; i < languages.length; i++) {
            Assert.assertTrue("Found " + languages[i] + " in list", languageList.contains(languages[i]));
        }
    }

//    @Test
//    public void testSelectAcquisitionLinks() throws Exception{
//        UstadJSOPDSFeed feed = loadAcquireMultiFeed();
//
//        Assert.assertNotNull("Loaded feed from resource stream", feed);
//        String[] preferredFormats = new String[]{"application/epub+zip", "application/pdf"};
//        String[] preferredLangs = new String[]{"en", "fa"};
//        UstadJSOPDSFeed acquireFeed = feed.selectAcquisitionLinks(preferredFormats, preferredLangs, 100, 100);
//        Vector entryLinksVector = acquireFeed.getEntry(0).getAcquisitionLinks();
//        Assert.assertEquals("Acquire feed has 1 link", 1, entryLinksVector.size());
//        String[] entryLinks = (String[])entryLinksVector.elementAt(0);
//        Assert.assertEquals("Link is first preferred format", "application/epub+zip",
//                entryLinks[UstadJSOPDSEntry.LINK_MIMETYPE]);
//
//        UstadJSOPDSEntry entry2 = feed.getEntry(1);
//        String[] links = entry2.getBestAcquisitionLink(new String[]{"application/epub+zip"},
//                new String[]{"en", "fa"}, 100, 100);
//        Assert.assertEquals("Preferred language English provides English link", "en",
//                links[UstadJSOPDSEntry.ATTR_HREFLANG]);
//        links = entry2.getBestAcquisitionLink(new String[]{"application/epub+zip"},
//                new String[]{"fa", "en"}, 100, 100);
//        Assert.assertEquals("Preferred language Farsi provides Farsi link", "fa",
//                links[UstadJSOPDSEntry.ATTR_HREFLANG]);
//
//        UstadJSOPDSEntry entry3 = feed.getEntry(2);
//        links = entry3.getBestAcquisitionLink(new String[]{"application/epub+zip"},
//                new String[]{"ps", "fa"}, 100, 2000);
//        Assert.assertEquals("Prioritizing language weight provides link in desired language first",
//                "ps", links[UstadJSOPDSEntry.ATTR_HREFLANG]);
//        Assert.assertNotEquals("Prioritizing language weight provides link with less desirable mime type",
//                "application/epub+zip", links[UstadJSOPDSEntry.ATTR_MIMETYPE]);
//    }

    @Test
    public void testSerialize() throws Exception{
        UstadJSOPDSFeed feed = loadAcquireMultiFeed("http://www.ustadmobile.com/files/test/acquire-multi.opds");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        feed.serialize(bout, true);

        String serializedFeed = new String(bout.toByteArray(), "UTF-8");
        System.out.println(serializedFeed);

        UstadJSOPDSFeed deserializedFeed = new UstadJSOPDSFeed();

        deserializedFeed.loadFromString(new String(bout.toByteArray(), "UTF-8"));
        Assert.assertEquals("Serializer set absolute self href, then restored to href property",
                "http://www.ustadmobile.com/files/test/acquire-multi.opds",
                deserializedFeed.getHref());
        Assert.assertEquals("Feed loaded with correct id", feed.getItemId(),
                deserializedFeed.getItemId());
        for(int i = 0; i < feed.size(); i++) {
            Assert.assertEquals("Feed entry " + i + " has same id ", feed.getEntry(i).getItemId(),
                    deserializedFeed.getEntry(i).getItemId());
        }

    }


    @Test
    public void testAsyncHttpLoadFeed() {
        final Object lock = new Object();
        String opdsUrl = UMFileUtil.joinPaths(new String[]{ResourcesHttpdTestServer.getHttpRoot(),
                "file.opds"});
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();
        final boolean[] onDoneCalled = new boolean[]{false};
        feed.loadFromUrlAsync(opdsUrl, null, PlatformTestUtil.getTargetContext(),
                new UstadJSOPDSItem.OpdsItemLoadCallback() {
            @Override
            public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entry) {

            }

            @Override
            public void onDone(UstadJSOPDSItem item) {
                onDoneCalled[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onError(UstadJSOPDSItem item, Throwable cause) {

            }
        });

        synchronized (lock){
            try { lock.wait(LOAD_TIMEOUT); }
            catch(InterruptedException e) {}
        }

        Assert.assertEquals("Feed loaded correct id",
                "http://umcloud1.ustadmobile.com/opds/courseid/6CM", feed.getItemId());
        Assert.assertNotNull("Feed expected entry is present",
                feed.getEntryById("4f382c43-1e92-4fe9-bce0-e03b6c11336f"));
        Assert.assertEquals("Feed has 1 entry", feed.size(), 1);
    }

    @Test
    public void testAsyncHttpLoadEntry() {
        final Object lock = new Object();
        String opdsUrl = UMFileUtil.joinPaths(new String[]{ResourcesHttpdTestServer.getHttpRoot(),
                "entry.opds"});
        UstadJSOPDSEntry entry = new UstadJSOPDSEntry(null);
        final boolean[] onDoneCalled = new boolean[]{false};

        entry.loadFromUrlAsync(opdsUrl, null, PlatformTestUtil.getTargetContext(),
                new UstadJSOPDSItem.OpdsItemLoadCallback() {
            @Override
            public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entry) {

            }

            @Override
            public void onDone(UstadJSOPDSItem item) {
                onDoneCalled[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onError(UstadJSOPDSItem item, Throwable cause) {

            }
        });

        synchronized (lock) {
            try { lock.wait(LOAD_TIMEOUT); }
            catch(InterruptedException e) {}
        }

        Assert.assertEquals("Entry loaded id matches id in OPDS file",
                "4f382c43-1e92-4fe9-bce0-e03b6c11336f", entry.getItemId());
        UmOpdsLink acquisitionLinks = entry.getBestAcquisitionLink(new String[]{"application/epub+zip"});
        Assert.assertEquals("Acquisition link matches expected", "small.epub",
                acquisitionLinks.getHref());
    }

    @Test
    public void testAsyncHttpLoadOnError() {
        final Object lock = new Object();
        String opdsUrl = UMFileUtil.joinPaths(new String[]{ResourcesHttpdTestServer.getHttpRoot(),
                "doesnotexist.opds"});
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();
        final Throwable[] onErrorThrowable = new Throwable[1];
        feed.loadFromUrlAsync(opdsUrl, null, PlatformTestUtil.getTargetContext(),
                new UstadJSOPDSItem.OpdsItemLoadCallback() {
            @Override
            public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entry) {

            }

            @Override
            public void onDone(UstadJSOPDSItem item) {

            }

            @Override
            public void onError(UstadJSOPDSItem item, Throwable cause) {
                onErrorThrowable[0] = cause;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try { lock.wait(LOAD_TIMEOUT); }
            catch(InterruptedException e) {}
        }

        Assert.assertNotNull("Loading a url that does not exist results in call to onFailure",
                onErrorThrowable[0]);
    }


}
