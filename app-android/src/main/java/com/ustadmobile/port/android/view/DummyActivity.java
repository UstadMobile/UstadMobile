package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;

public class DummyActivity extends UstadBaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new ContentLibraryViewPagerFragment())
                    .commit();
        }

    }
}
