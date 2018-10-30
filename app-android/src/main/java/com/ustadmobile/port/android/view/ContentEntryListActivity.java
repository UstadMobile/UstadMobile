package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;

public class ContentEntryListActivity extends UstadBaseActivity implements ContentEntryListFragment.ContentEntryListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        setUMToolbar(R.id.entry_toolbar);
        setDirectionFromSystem();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ContentEntryListFragment currentFrag = ContentEntryListFragment.newInstance(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit();
        }

    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
