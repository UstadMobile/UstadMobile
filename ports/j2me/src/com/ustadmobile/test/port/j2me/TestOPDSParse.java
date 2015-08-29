/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.test.port.j2me;
import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;

import com.ustadmobile.core.opds.UstadJSOPDSAuthor;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
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
        String fileURI = "/com/ustadmobile/port/j2me/app/tests/" + 
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
