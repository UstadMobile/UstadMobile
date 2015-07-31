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

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.Storage;
import com.ustadmobile.app.EpubUtils;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.app.ZipUtils;
import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.app.forms.HTMLComp;
import com.ustadmobile.app.forms.HTMLForm;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.opf.UstadJSOPFItem;
import gnu.classpath.java.util.zip.ZipInputStream;
import j2meunit.framework.TestCase;
import java.io.InputStream;
import java.io.OutputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 *Tests if you can read the EPUB file.
 * More specifically: Can get content list, asset files in EPUB file.
 * @author varuna
 */
public class TestEPUBRead extends TestCase {
    public TestEPUBRead(){
        setName("TestEPUBRead Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("TestEPUBRead Test start", 2, 1+1);
        
        //download and get EPUB 
        String contentDir = 
                UstadMobileSystemImpl.getInstance().getSharedContentDir();
        String epubTestFile = FileUtils.joinPath(contentDir, "test.epub");
         if (!FileUtils.checkFile(epubTestFile)){
             //download the file
             String epubTestFileUrl = "http://umcloud1.ustadmobile.com/media/eXeUpload/test.epub";
             HTTPUtils.downloadURLToFile(epubTestFileUrl, contentDir, "test.epub");
         }
        String opfLocation = EpubUtils.getOpfLocationFromEpub(epubTestFile);
        UstadJSOPF opfObj = EpubUtils.getOpfFromEpub(epubTestFile);
        UstadJSOPFItem[] opfItem = opfObj.spine;
        assertEquals("Getting Opf data from EPUB", opfItem[1].href,
                "free_text_text.xhtml");
        
        //Open the epub 
        String itemToOpen = FileUtils.joinPath(opfLocation, opfItem[1].href);
        System.out.println(itemToOpen);
        int z =0;
        
        
        NetworkManager.getInstance().start();
        Storage.init(this);
        
        Display.init(this);
        
//        Form f = new Form();
//        f = HTMLComp.loadTestForm(epubTestFile,itemToOpen);
//        f.show();
        
        
        
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                Form f = new Form();
                f = HTMLForm.loadTestForm();
                f.show();
            }
        });
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        
        //Make form
        
        //Open file
        
        
    }
}
