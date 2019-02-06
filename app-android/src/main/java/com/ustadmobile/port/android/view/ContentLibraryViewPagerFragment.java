package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;

public class ContentLibraryViewPagerFragment extends UstadBaseFragment {

    public static ContentLibraryViewPagerFragment newInstance() {
        ContentLibraryViewPagerFragment fragment = new ContentLibraryViewPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }




}
