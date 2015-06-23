/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplFactory;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.FileUtils;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class TestRename extends TestCase {
    private UstadMobileSystemImpl ustadMobileSystemImpl;
    public TestRename(){
        setName("TestRename Test");
    }
    
    public void runTest() throws Throwable{
        ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        String getSharedContentDir = ustadMobileSystemImpl.getSharedContentDir();
        String fileURIFrom = FileUtils.joinPath(getSharedContentDir, "from.txt");
        FileConnection fc = null;
        fc = (FileConnection) Connector.open(fileURIFrom,
                Connector.READ_WRITE);
        if (!fc.exists()){
            fc.create();
        }
        if (fc!=null){
            fc.close();
        }
        String fileURITo = FileUtils.joinPath(getSharedContentDir, "to.txt");
        ustadMobileSystemImpl.renameFile(fileURIFrom, fileURITo);
        if (FileUtils.checkFile(fileURITo)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }
}
