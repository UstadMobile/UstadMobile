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
import com.ustadmobile.core.opf.UstadJSOPF;
import j2meunit.framework.TestCase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.kxml2.io.KXmlParser;
/**
 *
 * @author varuna
 */
public class TestOPFParse extends TestCase{
    
    public TestOPFParse(){
        setName("OPFParse Test");
    }
    
    public void runTest() throws Throwable{
        
        /*InputStream bais = TestUtils.getFileBytes(
                TestUtils.testSettings.get("appDataURI").toString() + "/" +
                TestUtils.testSettings.get("opfxml").toString());
        */
        String fileURI = "/com/ustadmobile/test/port/j2me/" + 
                TestUtils.testSettings.get("opfxml").toString();
        InputStream bais = getClass().getResourceAsStream(
                fileURI); 
        
        /*ByteArrayInputStream bais = 
                TestUtils.getHTTPBytes(
                    TestUtils.testSettings.get("opfxml").toString());*/
        KXmlParser parser = new KXmlParser();
        parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
        UstadJSOPF feed = UstadJSOPF.loadFromOPF(parser);
        
        assertEquals("Spine made successfully", 
                "cover.xhtml", feed.spine[0].href);
        assertEquals("Mime Exception Stored successfully", 
                "application/mime+ex", 
                feed.getMimeType("mime_exception.mex"));
        assertEquals("Mime Default test ok", 
                feed.DEFAULT_MIMETYPE, 
                feed.getMimeType("blahfile.txt"));
    }
    
}
