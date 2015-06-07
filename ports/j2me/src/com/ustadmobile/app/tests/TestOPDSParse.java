/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;
import com.ustadmobile.app.controller.UstadMobileAppController;

import com.ustadmobile.app.opds.UstadJSOPDSAuthor;
import com.ustadmobile.app.opds.UstadJSOPDSEntry;
import com.ustadmobile.app.opds.UstadJSOPDSFeed;
import j2meunit.framework.*;
//import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;
import org.kxml2.io.KXmlParser;

/**
 *
 * @author varuna
 */
public class TestOPDSParse extends TestCase{

    public TestOPDSParse() {
        setName("OPDSParse Test");
    }
    
    protected void runTest() throws Throwable {
        String fileURI = "/com/ustadmobile/app/tests/" + 
                TestUtils.testSettings.get("opdsxml").toString();
        InputStream bais = getClass().getResourceAsStream(
                fileURI);       
        /*String fileURI = TestUtils.testSettings.get("appDataURI").toString() + "/" +
                TestUtils.testSettings.get("opdsxml").toString(); 
        InputStream bais = TestUtils.getFileBytes(fileURI);*/
        
        /*ByteArrayInputStream bais = 
                TestUtils.getHTTPBytes(TestUtils.testSettings.get("opdsxml").toString());*/
        KXmlParser parser = new KXmlParser();
        parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
        UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);
        assertEquals("Test got correct title from feed",
                "Ustad Mobile Public OPDS Catalog", 
                feed.title);
        String[] s = new String[]{"self", 
                    "application/atom+xml;profile=opds-catalog;kind=navigation",
                    "/opds/public/"};
        String[] c = new String[3];
        Vector link = feed.getLinks("self", "application/atom+xml;profile=opds-catalog;kind=navigation", true, true);
        c =(String[]) link.firstElement();
        for (int i=0; i<c.length; i++){
            assertEquals("Test got correct link from feed ", c[i], s[i]);
        }
        assertTrue(feed.updated.startsWith("2015"));
        UstadJSOPDSEntry firstEntry;
        firstEntry = feed.entries[0];
        assertEquals("Entry test okay", firstEntry.title, "Recent Courses");
        String[] es = new String[]{null, 
            "application/atom+xml;profile=opds-catalog;kind=navigation", 
            "/opds/public/recent"};
        String [] ec = new String[3];
        Vector elink = firstEntry.getLinks(null, 
                "application/atom+xml;profile=opds-catalog;kind=navigation", 
                true, true);
        ec = (String[]) elink.firstElement();
        
        for (int i=0;i<ec.length; i++){
            assertEquals("Test got correct entry links", es[i], ec[i]);
        }
        
        assertEquals("Get feed by ID test okay", 
                "Recent Courses", 
                feed.getEntryById("http://umcloud1.ustadmobile.com/opds/public/recent").title);
        
        assertEquals("Get Summary test okay", 
                "The most recent courses from Ustad Mobile in the last month",
                firstEntry.summary);
        
        UstadJSOPDSAuthor feed1 = (UstadJSOPDSAuthor) feed.authors.elementAt(0);
        UstadJSOPDSAuthor feed2 = (UstadJSOPDSAuthor) feed.authors.elementAt(1);
        UstadJSOPDSAuthor entry1 = (UstadJSOPDSAuthor) feed.entries[0].authors.elementAt(0);
        UstadJSOPDSAuthor entry2 = (UstadJSOPDSAuthor) feed.entries[0].authors.elementAt(1);
        
        assertEquals("Got author okay", "OPDS God",
                feed1.name);
        assertEquals("Got 2nd author okay", "Ustad Mobile Public",
                feed2.name);
        
        assertEquals("Got 1st entry's 1st author okay", "Mike Dawson",
                entry1.name);
        assertEquals("Got 1st entry's 2nd author okay", "Varuna Singh",
                entry2.name);
        
        assertEquals("Got publisher deets", "Ustad Mobile Inc.", 
                firstEntry.publisher);
    }
    
    
}
