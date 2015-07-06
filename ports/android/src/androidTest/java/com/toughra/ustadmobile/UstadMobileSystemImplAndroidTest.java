package com.toughra.ustadmobile;
import android.os.Environment;

import com.ustadmobile.impl.HTTPResult;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;

/**
 *
 * TODO: Add instrumentation
 * https://developer.android.com/training/testing/unit-testing/instrumented-unit-tests.html
 *
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroidTest extends TestCase{

    private UstadMobileSystemImpl impl;

    protected void setUp() {
        impl = UstadMobileSystemImpl.getInstance();
    }
    
    public void testCanCreateImpl() {
        assertNotNull("Can create impl", impl);
    }

    public void testSharedContentDir() {
        File sharedDir = new File(impl.getSharedContentDir());
        assertTrue(sharedDir.exists() && sharedDir.isDirectory());
    }


    /**
     * Test that the implementation can write a String to a file, and then read the same string
     * back
     *
     * @throws IOException
     */
    public void testReadWriteStringToFile() throws IOException {
        File baseDir = Environment.getExternalStorageDirectory();
        File outFile = new File(baseDir, "umtestfile.txt");

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String contents = "The answer is 42";
        impl.writeStringToFile(contents, outFile.getAbsolutePath(), "UTF-8");

        String contentsRead = impl.readFileAsText(outFile.getAbsolutePath(), "UTF-8");

        assertEquals("String read from file equal to what was written", contents, contentsRead);
    }

    public void testMakeRemoveDirectory() throws IOException{
        File baseDir = Environment.getExternalStorageDirectory();
        File outDir = new File(baseDir, "umtestdir/umtestsubdir");
        impl.makeDirectory(outDir.getAbsolutePath());
        assertTrue("New directory made", outDir.isDirectory());

        String filePath = outDir.getAbsolutePath()+"/content.txt";
        impl.writeStringToFile("The answer is 42", filePath, "UTF-8");
        assertTrue("Newly written file exists", impl.fileExists(filePath));
        File delDir = new File(baseDir, "umtestdir");

        impl.removeRecursively(delDir.getAbsolutePath());
        assertFalse("Directory deleted", impl.dirExists(delDir.getAbsolutePath()));
        assertFalse("Deleted file does not exist", impl.fileExists(filePath));
    }

    public void testFileSize() throws IOException {
        File baseDir = Environment.getExternalStorageDirectory();
        File outFile = new File(baseDir, "umtestfile.txt");
        String message = "This file is written here";
        impl.writeStringToFile(message, outFile.getAbsolutePath(), "UTF-8");
    }

    public void testSystemInfo() {
        Hashtable systemInfo = UstadMobileSystemImpl.getInstance().getSystemInfo();
        assertEquals("Got hashtable: os = Android", "Android", (String)systemInfo.get("os"));
    }

    public void testSystemLocale() {
        String systemLocale = UstadMobileSystemImpl.getInstance().getSystemLocale();
        String[] localeComponents = systemLocale.split("_");
        Locale newLocale = new Locale(localeComponents[0], localeComponents[1]);
        assertNotNull("Can create a locale object from given locale string", newLocale);
    }

    public void testModTimeDiff() throws IOException{
        File baseDir = Environment.getExternalStorageDirectory();
        File file1 = new File(baseDir, "umtestfile1.txt");
        File file2 = new File(baseDir, "umtestfile2.txt");


        UstadMobileSystemImplAndroid impl = (UstadMobileSystemImplAndroid) UstadMobileSystemImpl.getInstance();
        impl.writeStringToFile("hello world", file1.getAbsolutePath(), "UTF-8");
        try {
            Thread.sleep(1000);
        }catch(InterruptedException e) {}
        impl.writeStringToFile("hello world", file2.getAbsolutePath(), "UTF-8");
        long timeDiff = impl.modTimeDifference(file1.getAbsolutePath(), file2.getAbsolutePath());
        assertTrue("Time difference is at least 1 second, less than five: is " + timeDiff ,
                timeDiff >= 1000 && timeDiff < 5000);
        file1.delete();
        file2.delete();
    }

    public void testHttpRequest() throws IOException {
        String httpServer = "http://www.ustadmobile.com";
        HTTPResult result = impl.makeRequest(httpServer, new Hashtable(), new Hashtable(), "GET");
        assertEquals("Get 200 response OK from live server", 200, result.getStatus());
    }



}
