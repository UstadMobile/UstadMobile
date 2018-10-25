package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ContentEntryView;
import com.ustadmobile.lib.db.entities.ContentEntry;

public class ContentEntryListActivity extends UstadBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, ContentEntryListFragment.newInstance(getIntent().getExtras()))
                    .commit();
        }

    }
}
