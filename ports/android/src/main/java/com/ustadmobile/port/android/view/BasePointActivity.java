package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class BasePointActivity extends UstadBaseActivity implements BasePointView {

    protected BasePointController mBasePointController;

    protected BasePointPagerAdapter mPagerAdapter;

    private int[] tabIconsIds = new int[]{R.drawable.ic_book_black_24dp,
            R.drawable.ic_group_black_24dp};


    protected boolean classListVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_point);
        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        //make OPDS fragments and set them here
        mBasePointController = BasePointController.makeControllerForView(this, args);
        setBaseController(mBasePointController);
        setUMToolbar();

        mPagerAdapter = new BasePointPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.basepoint_pager);
        viewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.basepoint_tabs);
        tabLayout.setupWithViewPager(viewPager);
        for(int i = 0; i < mPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setIcon(tabIconsIds[i]);
        }
    }


    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_basepoint_addfeed:
                mBasePointController.handleClickAddFeed();
                return true;
            case R.id.action_basepoint_removefeed:
                CatalogOPDSFragment opdsFragment = (CatalogOPDSFragment)mPagerAdapter.getItem(
                        BasePointController.INDEX_BROWSEFEEDS);
                mBasePointController.handleRemoveItemsFromUserFeed(
                        opdsFragment.getSelectedEntries());
                opdsFragment.setSelectedEntries(new UstadJSOPDSEntry[0]);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */




    @Override
    public void refreshCatalog(int column) {
        ((CatalogOPDSFragment) mPagerAdapter.getItem(column)).loadCatalog();
    }


    @Override
    public void setClassListVisible(boolean visible) {
        this.classListVisible= visible;
    }





    public class BasePointPagerAdapter extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{MessageIDConstants.my_resources, MessageIDConstants.classes};

        private WeakHashMap<Integer, Fragment> fragmentMap;

        public BasePointPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentMap = new WeakHashMap<>();
        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragmentMap.get(position);
            if(fragment == null) {
                Bundle bundle = null;
                switch(position) {
                    case BasePointController.INDEX_DOWNLOADEDENTRIES:
                        Hashtable posArgs =
                                BasePointActivity.this.mBasePointController.getCatalogOPDSArguments(position);
                        bundle = UMAndroidUtil.hashtableToBundle(posArgs);
                        fragment = CatalogOPDSFragment.newInstance(bundle);
                        break;
                    case BasePointController.INDEX_CLASSES:
                        bundle = new Bundle();
                        fragment = ClassListFragment.newInstance(bundle);
                        break;
                }
                fragmentMap.put(position, fragment);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return BasePointActivity.this.classListVisible ? 2 : 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position]);
        }


    }
}
