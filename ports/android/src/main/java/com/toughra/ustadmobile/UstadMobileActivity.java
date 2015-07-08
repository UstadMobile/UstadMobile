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

package com.toughra.ustadmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.ustadmobile.impl.UMTransferJob;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.opf.UstadJSOPFItem;
import com.ustadmobile.view.LoginActivity;

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

        /*
        if(impl.getActiveUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
        */

    }
}
