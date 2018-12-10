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

package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.internal.platform.Platform;

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */
/* $endif$ */

/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
        import android.content.Intent;
        import android.app.Activity;
        import android.app.Instrumentation;
   $endif$ */


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestFileImpl extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public abstract class TestFileImpl extends TestCase {
/* $endif */
    
    public TestFileImpl() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    public void testFileImpl() throws IOException{
        UstadMobileSystemImpl.l(UMLog.DEBUG, 600, "testFileImpl Asking for context");

        Object context = PlatformTestUtil.getTargetContext();
        TestUtils utils = new TestUtils();
        
        UstadMobileSystemImpl.l(UMLog.DEBUG, 600, "testFileImpl got context");
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        String sharedContentDir = impl.getSharedContentDir(PlatformTestUtil.getTargetContext());
        
        assertTrue("Shared content dir exists", impl.dirExists(sharedContentDir));
        
        impl.setActiveUser(utils.getTestProperty(TestUtils.PROP_TESTUSER), context);
        
        assertTrue("User directory exists when active user is set",
                impl.dirExists(impl.getUserContentDirectory(PlatformTestUtil.getTargetContext(),
                        impl.getActiveUser(context))));
        
        String testMkDirPath = UMFileUtil.joinPaths(
                new String[]{impl.getSharedContentDir(PlatformTestUtil.getTargetContext()),
                        "tmpFileDirTest"});
        impl.makeDirectory(testMkDirPath);
        assertTrue("Newly created dir exists: " + testMkDirPath,
            impl.dirExists(testMkDirPath));
        
        String testRecursivePath = UMFileUtil.joinPaths(new String[]{ 
            testMkDirPath, "some", "sub", "dir"});
        impl.makeDirectoryRecursive(testRecursivePath);
        assertTrue("New recursively created dir exists", 
            impl.dirExists(testRecursivePath));
        
        impl.removeRecursively(testMkDirPath);
        assertTrue("Newly created dir removed", !impl.dirExists(testMkDirPath));
        
        String contentToWrite = "The answer to the meaning of life, the universe and everything is 42";
        String fileWritePath = UMFileUtil.joinPaths(new String[]{sharedContentDir, 
            "fileimpltest.txt"});
        
        if(impl.fileExists(fileWritePath)) {
            impl.removeFile(fileWritePath);
        }
        
        impl.writeStringToFile(contentToWrite, fileWritePath, "UTF-8");
        long fileSize= impl.fileSize(fileWritePath);
        
        String fileContents = impl.readFileAsText(fileWritePath);
        
        assertEquals("Can read same content back from file",
            contentToWrite, fileContents);
        
        OutputStream appendOut = impl.openFileOutputStream(fileWritePath, 
            UstadMobileSystemImpl.FILE_APPEND);
        appendOut.write(contentToWrite.getBytes("UTF-8"));
        appendOut.flush();
        appendOut.close();
        
        assertEquals("File on appending the content second time is double size",
            fileSize * 2, impl.fileSize(fileWritePath));
        
        impl.removeFile(fileWritePath);
        assertTrue("File once removed is gone", !impl.fileExists(fileWritePath));
    }
    
    public void runTest() throws IOException {
        testFileImpl();
    }
    
}
