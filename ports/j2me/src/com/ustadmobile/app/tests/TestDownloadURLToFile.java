/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.DeviceRoots;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;

/**
 *
 * @author varuna
 */
public class TestDownloadURLToFile extends TestCase {
    public TestDownloadURLToFile(){
        setName("Test Download URL to file");
    }
    
    public void runTest() throws Throwable{
        DeviceRoots mainRoot = FileUtils.getBestRoot();
        
        String url = "http://www.ustadmobile.com/hello.txt";
        HTTPUtils.downloadURLToFile(url, mainRoot.path, "");
        String fileContents = FileUtils.getFileContents(
                FileUtils.joinPath(mainRoot.path, "hello.txt"));
        if (fileContents!=null){
            if(fileContents.startsWith("Hello")){
                assertTrue(true);
            }else{
                assertTrue(false);
            }
        }else{
            assertTrue(false);
        }
        
            
    }
}
