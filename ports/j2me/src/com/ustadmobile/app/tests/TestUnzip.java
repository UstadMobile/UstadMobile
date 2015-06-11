/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.ZipUtils;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplFactory;
import java.util.Vector;
import javax.microedition.io.Connector;

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
            //continue..
        }else{
            assertTrue(false);
        }
        String unzipURI = FileUtils.joinPath(getSharedContentDir, "unzippedfolder");
        
        //Delete if folder exists..
        if (FileUtils.checkDir(unzipURI)){
            FileUtils.deleteRecursively(unzipURI, true);
        }
        
        ZipUtils.unZipFile(zipURI, unzipURI);
        if (FileUtils.checkDir(unzipURI)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        String unZipDirList[] = FileUtils.listFilesInDirectory(unzipURI);
        Vector unZipDirListVector = FileUtils.listFilesRecursivelyInDirectory(unzipURI, unzipURI);
        String[] unZipDirListStr = FileUtils.vectorToStringArray(unZipDirListVector);
        boolean found = false;
        for (int i=0; i<unZipDirListStr.length; i++){
            if(unZipDirListStr[i].toLowerCase().indexOf("fileinafolder.txt") >=0 ){
                found = true;
                break;
            }
        }
        
        if (found){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
    }
}
