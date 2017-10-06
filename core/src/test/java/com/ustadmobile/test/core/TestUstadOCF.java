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
package com.ustadmobile.test.core;


/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
   $endif$ */

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.core.util.UMFileUtil;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

/* $endif$ */


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestUstadOCF extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public abstract class TestUstadOCF extends TestCase {
/* $endif */    
    
    private byte[] ocfData;
    
    public TestUstadOCF() {
        /* $if umplatform == 1 $ 
        super("com.toughra.ustadmobile", UstadMobileActivity.class);
        $endif */
    }

    protected void setUp() throws Exception {
        super.setUp();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String containerXMLURL = UMFileUtil.joinPaths(new String[] { 
            TestUtils.getInstance().getHTTPRoot(), "container.xml"});
        HTTPResult httpData = impl.makeRequest(containerXMLURL, new Hashtable(), new Hashtable(), 
            "GET");
         ocfData = httpData.getResponse();
    }

    
    protected void tearDown() throws Exception {
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
        ocfData = null;
    }
    
    
    
    public void testUstadOCF() throws IOException, XmlPullParserException{
        //loads testassets/container.xml
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        XmlPullParser xpp = impl.newPullParser();
        xpp.setInput(new ByteArrayInputStream(ocfData), "UTF-8");
        UstadOCF ocf = UstadOCF.loadFromXML(xpp);
        assertEquals("Loaded container has one root", 1, ocf.rootFiles.length);
        
        assertEquals("Root file Full path is correct", "EPUB/package.opf", 
            ocf.rootFiles[0].fullPath);
    }

    public void runTest() throws IOException, XmlPullParserException{
        this.testUstadOCF();
    }
    
}
