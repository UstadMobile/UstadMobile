package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.DummyView;

public class DummyActivity extends UstadBaseActivity implements DummyView {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

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
