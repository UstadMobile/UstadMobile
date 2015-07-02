package com.toughra.ustadmobile;
import android.os.Environment;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;

/**
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
        assertTrue("Time difference is at least 1 second, less than two: is " + timeDiff ,
                timeDiff >= 1000 && timeDiff < 2000);
        file1.delete();
        file2.delete();
    }



}
