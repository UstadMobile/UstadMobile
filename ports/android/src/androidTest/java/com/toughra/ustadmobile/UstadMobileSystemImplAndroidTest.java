package com.toughra.ustadmobile;
import android.os.Environment;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.impl.UstadMobileSystemImplFactory;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroidTest extends TestCase{

    /*
    public void testCanCreateImpl() {
        UstadMobileSystemImpl impl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        assertNotNull("Can create impl", impl);
    }*/

    /*
    public void testReadWriteStringToFile() throws IOException {
        File baseDir = Environment.getExternalStorageDirectory();
        File outFile = new File(baseDir, "umtestfile.txt");

        UstadMobileSystemImplAndroid impl = new UstadMobileSystemImplAndroid();
        String contents = "The answer is 42";
        impl.writeStringToFile(contents, outFile.getAbsolutePath(), "UTF-8");
        assertEquals(true, "Managed to write file without throwing exception");
    }*/

}
