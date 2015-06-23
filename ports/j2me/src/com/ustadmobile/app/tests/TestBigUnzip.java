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
        ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
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
