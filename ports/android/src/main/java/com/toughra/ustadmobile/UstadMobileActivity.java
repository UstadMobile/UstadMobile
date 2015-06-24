package com.toughra.ustadmobile;

import android.app.Activity;
import android.os.Bundle;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.opf.UstadJSOPFItem;

public class UstadMobileActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String contentDirURI = impl.getSharedContentDir();
        String localeStr = impl.getSystemLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
