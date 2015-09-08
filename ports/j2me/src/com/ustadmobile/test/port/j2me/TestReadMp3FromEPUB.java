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

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import j2meunit.framework.TestCase;
import java.io.InputStream;
import javax.microedition.media.Manager;

/**
 *
 * @author varuna
 */
public class TestReadMp3FromEPUB extends TestCase {
    public TestReadMp3FromEPUB(){
        setName("Test Mp3 Read From Zip Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        
        //download and get Zip 
        String contentDir = 
                UstadMobileSystemImpl.getInstance().getSharedContentDir();
        String mp3EPUBTestFile = FileUtils.joinPath(contentDir, "forasportal.epub");
         if (!FileUtils.checkFile(mp3EPUBTestFile)){
             //download the file
             String mp3EPUBTestFileURL = 
                     "http://umcloud1.ustadmobile.com/media/eXeUpload/forasportal.epub";
             HTTPUtils.downloadURLToFile(mp3EPUBTestFileURL, contentDir, "");
             
         }
         
         if (!FileUtils.checkFile(mp3EPUBTestFile)){
             assertTrue(false);
         }else{
             HTTPUtils.httpDebug("forasportal.epub exists..");
             assertTrue(true);
         }

        HTTPUtils.httpDebug("Starting to play the mp3..");
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        String supportedTypes[] = Manager.getSupportedContentTypes(null);
        for (int i = 0; i < supportedTypes.length; i++) {
           if (supportedTypes[i].startsWith("audio")) {
              HTTPUtils.httpDebug("Device supports " + supportedTypes[i]);
           }
        }
   
        
        
        assertTrue(true);
        
        
         
         
    }
}
