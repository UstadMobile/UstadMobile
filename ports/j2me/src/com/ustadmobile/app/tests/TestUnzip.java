/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
