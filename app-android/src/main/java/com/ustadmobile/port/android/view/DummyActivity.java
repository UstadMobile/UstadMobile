package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;

public class DummyActivity extends UstadBaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        setUMToolbar(R.id.entry_toolbar);
        setDirectionFromSystem();

        ContentLibraryViewPagerFragment currentFrag = new ContentLibraryViewPagerFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit();
        }

    }
}
