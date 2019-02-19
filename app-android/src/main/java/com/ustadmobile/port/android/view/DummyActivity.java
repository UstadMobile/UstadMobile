package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DummyView;

import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;
import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_DOWNLOADED_CONTENT;

public class DummyActivity extends UstadBaseActivity implements DummyView {


    public static final long MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        Toolbar toolbar = findViewById(R.id.entry_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        ViewPager viewPager = findViewById(R.id.library_viewpager);
        viewPager.setAdapter(new LibraryPagerAdapter(getSupportFragmentManager(), (Context) getContext()));
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    public static class LibraryPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;
        private final Context context;
        private final UstadMobileSystemImpl impl;

        LibraryPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            impl = UstadMobileSystemImpl.getInstance();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();

            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    bundle.putString(ARG_CONTENT_ENTRY_UID, String.valueOf(MASTER_SERVER_ROOT_ENTRY_UID));
                    return ContentEntryListFragment.newInstance(bundle);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    bundle.putString(ARG_DOWNLOADED_CONTENT, String.valueOf(""));
                    return ContentEntryListFragment.newInstance(bundle);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return impl.getString(MessageID.libraries, context);
                case 1:
                    return impl.getString(MessageID.downloaded, context);

            }
            return null;

        }

    }


}
