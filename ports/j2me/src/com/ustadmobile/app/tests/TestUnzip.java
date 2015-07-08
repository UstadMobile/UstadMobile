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

import com.ustadmobile.app.DeviceRoots;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.ZipUtils;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplFactory;
import java.util.Hashtable;
import java.util.Vector;
/**
 *
 * @author varuna
 */
public class TestUnzip extends TestCase {
    private UstadMobileSystemImpl ustadMobileSystemImpl;
    public TestUnzip(){
        setName("Unzip Test");
    }
    
    public void runTest() throws Throwable{
        ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        String getSharedContentDir = ustadMobileSystemImpl.getSharedContentDir();
        
        //Lets download a zip file shall we?
        String url = "http://www.ustadmobile.com/unzipme.zip";        
        HTTPUtils.downloadURLToFile(url, getSharedContentDir, "");
        
        String zipURI = FileUtils.joinPath(getSharedContentDir, "unzipme.zip");
        if (FileUtils.checkFile(zipURI)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        String unzipURI = FileUtils.joinPath(getSharedContentDir, "unzippedfolder");
        
        //Delete if folder exists..
        if (FileUtils.checkDir(unzipURI)){
            FileUtils.deleteRecursively(unzipURI, true);
        }
        
        //Test if you can list files in a zip
        String[] zipListStringArray = null;
        zipListStringArray = ZipUtils.listFiles(zipURI);
        
        if (zipListStringArray.length < 1){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
        
        //Check if file in the list..
        if(FileUtils.isStringInStringArray("fileinafolder.txt",
                zipListStringArray)){
           assertTrue(true); //yay
        }else{
            assertTrue(false);
        }
        
        //Lets unzip it..
        ZipUtils.unZipFile(zipURI, unzipURI);
        if (FileUtils.checkDir(unzipURI)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //getting list of files in directory..
        Vector unZipDirListVector = FileUtils.listFilesRecursivelyInDirectory(unzipURI, unzipURI);
        String[] unZipDirListStr = FileUtils.vectorToStringArray(unZipDirListVector);
        
        //Check if file in the list..
        if(FileUtils.isStringInStringArray("fileinafolder.txt",
                unZipDirListStr)){
           assertTrue(true);
        }else{
            assertTrue(false);
        }
        
    }
}
