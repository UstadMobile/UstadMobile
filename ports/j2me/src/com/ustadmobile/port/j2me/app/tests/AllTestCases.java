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
package com.ustadmobile.port.j2me.app.tests;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestSuite;
import java.io.IOException;

/**
 *
 * @author varuna
 */
public class AllTestCases extends TestCase {
    
    public AllTestCases(){
        setName("All Test Caes");
    }

    public Test suite() {
        
        try {
            HTTPUtils.httpDebug("loadingTestSettings");
            TestUtils.getInstance().loadTestSettingsResource();
            HTTPUtils.httpDebug("loaded");
            //TestUtils.loadTestSettingsFile();
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        
        TestSuite allTestSuite = new TestSuite("AlltestSuites");
           
        //allTestSuite.addTest(new CoreLoginTest());        
        allTestSuite.addTest(new TestImplementation());
        allTestSuite.addTest(new TestSimpleHTTP());
        allTestSuite.addTest(new TestCustomLWUIT());
        allTestSuite.addTest(new TestFormShow());
        allTestSuite.addTest(new TestSimple());
        allTestSuite.addTest(new TestXmlParse());
        allTestSuite.addTest(new TestOPDSParse());
        allTestSuite.addTest(new TestOPFParse());
        allTestSuite.addTest(new TestDownloadURLToFile());
        allTestSuite.addTest(new TestRMS());
        allTestSuite.addTest(new TestSerializedHashtable());
        //allTestSuite.addTest(new TestRename());
        allTestSuite.addTest(new TestSystemimplJ2ME());
        allTestSuite.addTest(new TestUnzip());
        //allTestSuite.addTest(new TestBigUnzip());
        allTestSuite.addTest(new TestAppPref());
        allTestSuite.addTest(new TestUserPref());
        allTestSuite.addTest(new TestLoginView());
        allTestSuite.addTest(new TestCatalogView());
        allTestSuite.addTest(new TestHttpResult());
        allTestSuite.addTest(new TestEPUBRead());
        
        return allTestSuite;

    }
}
