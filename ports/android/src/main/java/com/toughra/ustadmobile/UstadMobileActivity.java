package com.toughra.ustadmobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import com.ustadmobile.impl.UMTransferJob;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.opf.UstadJSOPFItem;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class UstadMobileActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            ((UstadMobileSystemImplAndroid)impl).setCurrentContext(getApplicationContext());


        String contentDirURI = impl.getSharedContentDir();
        String localeStr = impl.getSystemLocale();

        File baseDir = Environment.getExternalStorageDirectory();
        File file1 = new File(baseDir, "umtestfile1.txt");
        File file2 = new File(baseDir, "umtestfile2.txt");
        File file3 = new File(baseDir, "wpdownload.zip");
        try {
            impl.writeStringToFile("hello world", file1.getAbsolutePath(), "UTF-8");
            System.out.println("WRite file OK");
        }catch(IOException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
