package com.ustadmobile.test.core;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;

import org.junit.Assert;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

/**
 * Created by mike on 4/22/17.
 */
public class TestUstadJSOPDSFeed {


    private UstadJSOPDSFeed loadAcquireMultiFeed() throws Exception {
        InputStream opdsIn = getClass().getResourceAsStream("/com/ustadmobile/test/core/acquire-multi.opds");
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();
        XmlPullParser parser = new KXmlParser();
        parser.setInput(opdsIn, "UTF-8");
        feed.loadFromXpp(parser);
        return feed;

    }

    @Test
    public void testFindLinkLanguages() throws Exception{
        UstadJSOPDSFeed feed = loadAcquireMultiFeed();

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

    @Test
    public void testSelectAcquisitionLinks() throws Exception{
        UstadJSOPDSFeed feed = loadAcquireMultiFeed();

        Assert.assertNotNull("Loaded feed from resource stream", feed);
        String[] preferredFormats = new String[]{"application/epub+zip", "application/pdf"};
        String[] preferredLangs = new String[]{"en", "fa"};
        UstadJSOPDSFeed acquireFeed = feed.selectAcquisitionLinks(preferredFormats, preferredLangs, 100, 100);
        Vector entryLinksVector = acquireFeed.getEntry(0).getAcquisitionLinks();
        Assert.assertEquals("Acquire feed has 1 link", 1, entryLinksVector.size());
        String[] entryLinks = (String[])entryLinksVector.elementAt(0);
        Assert.assertEquals("Link is first preferred format", "application/epub+zip",
                entryLinks[UstadJSOPDSEntry.LINK_MIMETYPE]);

        UstadJSOPDSEntry entry2 = feed.getEntry(1);
        String[] links = entry2.getBestAcquisitionLink(new String[]{"application/epub+zip"},
                new String[]{"en", "fa"}, 100, 100);
        Assert.assertEquals("Preferred language English provides English link", "en",
                links[UstadJSOPDSEntry.ATTR_HREFLANG]);
        links = entry2.getBestAcquisitionLink(new String[]{"application/epub+zip"},
                new String[]{"fa", "en"}, 100, 100);
        Assert.assertEquals("Preferred language Farsi provides Farsi link", "fa",
                links[UstadJSOPDSEntry.ATTR_HREFLANG]);

        UstadJSOPDSEntry entry3 = feed.getEntry(2);
        links = entry3.getBestAcquisitionLink(new String[]{"application/epub+zip"},
                new String[]{"ps", "fa"}, 100, 2000);
        Assert.assertEquals("Prioritizing language weight provides link in desired language first",
                "ps", links[UstadJSOPDSEntry.ATTR_HREFLANG]);
        Assert.assertNotEquals("Prioritizing language weight provides link with less desirable mime type",
                "application/epub+zip", links[UstadJSOPDSEntry.ATTR_MIMETYPE]);
    }

    @Test
    public void testSerialize() throws Exception{
        UstadJSOPDSFeed feed = loadAcquireMultiFeed();
        feed.href = "http://www.ustadmobile.com/files/test/acquire-multi.opds";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        feed.serialize(bout);

        UstadJSOPDSFeed deserializedFeed = new UstadJSOPDSFeed();
        deserializedFeed.loadFromString(new String(bout.toByteArray(), "UTF-8"));
        Assert.assertEquals("Serializer set absolute self href", feed.href,
                deserializedFeed.getAbsoluteSelfLink()[UstadJSOPDSEntry.LINK_HREF]);
        Assert.assertEquals("Feed loaded with correct id", feed.id, deserializedFeed.id);
        for(int i = 0; i < feed.getNumEntries(); i++) {
            Assert.assertEquals("Feed entry " + i + " has same id ", feed.getEntry(i).id,
                    deserializedFeed.getEntry(i).id);
        }

    }

}
