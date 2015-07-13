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
package com.ustadmobile.app.tests;

import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.controller.CatalogController;
import com.ustadmobile.model.CatalogModel;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import j2meunit.framework.TestCase;
import java.io.IOException;
import java.io.InputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class TestCatalogView extends TestCase {
    public TestCatalogView(){
        setName("CatalogView Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        
        String fileURI = "/com/ustadmobile/app/tests/" + 
                TestUtils.testSettings.get("opdsxml").toString();
        InputStream bais = getClass().getResourceAsStream(
                fileURI);       
        KXmlParser parser = new KXmlParser();
        parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
        UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);
        
        CatalogModel feedModel = new CatalogModel(feed);
        CatalogController catalogController = new CatalogController(feedModel);
        
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                try {
                    String fileURI = "/com/ustadmobile/app/tests/" + 
                    TestUtils.testSettings.get("opdsxml").toString();
                    InputStream bais = getClass().getResourceAsStream(
                            fileURI);       
                    KXmlParser parser = new KXmlParser();
                    parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
                    UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);

                    CatalogModel feedModel = new CatalogModel(feed);
                    CatalogController catalogController = 
                            new CatalogController(feedModel);
                    catalogController.show();
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        assertTrue("Completed first callSerially and wait", true);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
         
    }
}
