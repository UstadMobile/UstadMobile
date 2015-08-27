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

import com.ustadmobile.port.j2me.app.DeviceRoots;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import j2meunit.framework.TestCase;
import com.ustadmobile.port.j2me.app.ZipUtils;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.util.Vector;
/**
 *
 * @author varuna
 */
public class TestBigUnzip extends TestCase {
    private UstadMobileSystemImpl ustadMobileSystemImpl;
    public TestBigUnzip(){
        setName("Big Unzip Test");
    }
    
    public void runTest() throws Throwable{
        //ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        //String getSharedContentDir = ustadMobileSystemImpl.getSharedContentDir();

        //Test big zip file.
        DeviceRoots bestRoot = FileUtils.getBestRoot();
        String bestRootURI = bestRoot.path;
        String hugeZipFileURI = FileUtils.joinPath(bestRootURI, "hugefile.zip");
        String hugeUnzipURI = FileUtils.joinPath(bestRootURI, "hugefile");
        
        //delete existing.
        FileUtils.deleteRecursively(hugeUnzipURI, true);
        
        if (!FileUtils.checkFile(hugeZipFileURI)){
            assertTrue(false);
        }else{
            //net.sf.jazzlib.CRC32
            ZipUtils.unZipFile(hugeZipFileURI, null);
        }
        
        if (FileUtils.checkDir(hugeUnzipURI)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
    }
}
