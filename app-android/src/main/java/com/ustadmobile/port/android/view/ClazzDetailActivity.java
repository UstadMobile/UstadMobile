package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzDetailPresenter;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.WeakHashMap;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

/**
 * The ClassDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClassDetailView
 */
public class ClazzDetailActivity extends UstadBaseActivity implements
        ClassDetailView, TabLayout.OnTabSelectedListener {

    private ViewPager mPager;
    private ClassDetailViewPagerAdapter mPagerAdapter;
    private Toolbar toolbar;
    private TabLayout mTabLayout;
    private ClazzDetailPresenter mPresenter;
    Long clazzUid;

    /**
     * Separated out view pager setup for clarity.
     */
    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.class_detail_view_pager_container);
        mPagerAdapter = new ClassDetailViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragments(0, ClazzStudentListFragment.newInstance(this.clazzUid));
        mPagerAdapter.addFragments(1, ClazzLogListFragment.newInstance(this.clazzUid));
        //mPagerAdapter.addFragments(2, ComingSoonFragment.newInstance());
        mPagerAdapter.addFragments(2, SELAnswerListFragment.newInstance(this.clazzUid));
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * The ClazzDetailActivity's onCreate get the Clazz UID from arguments given to it
     * and sets up TabLayout.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_clazz_detail);

        clazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, 0L);

        toolbar = findViewById(R.id.class_detail_toolbar);
        //Set title as Class name
        toolbar.setTitle("Class");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Presenter
        mPresenter = new ClazzDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //set up view pager
        setupViewPager();

        mTabLayout= (TabLayout)findViewById(R.id.activity_class_detail_tablayout);
        mTabLayout.setupWithViewPager(mPager);
    }

    //Tab layout's on Tab selected
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Fragment selectedFragment = mPagerAdapter.getItem(tab.getPosition());
        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void setToolbarTitle(String toolbarTitle) {
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * ClassDetailView's view pager adapter
     */
    private class ClassDetailViewPagerAdapter extends FragmentPagerAdapter {

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        public ClassDetailViewPagerAdapter(FragmentManager fm) {
            super(fm);
            positionMap = new WeakHashMap<>();
        }

        public void addFragments(int pos, Fragment fragment) {
            positionMap.put(pos, (UstadBaseFragment) fragment);
        }

        /**
         * Generates fragment for that page/position
         *
         * @param position The position of the fragment to generate
         * @return void
         */
        @Override
        public Fragment getItem(int position) {
            UstadBaseFragment thisFragment = positionMap.get(new Integer(position));
            if (thisFragment != null) {
                return thisFragment;
            } else {
                switch (position) {
                    case 0:
                        return ClazzStudentListFragment.newInstance(clazzUid);
                    case 1:
                        return ClazzLogListFragment.newInstance(clazzUid);
//                    case 2:
//                        return ComingSoonFragment.newInstance();
                    case 2:
                        return SELAnswerListFragment.newInstance(clazzUid);
                    default:
                        return null;
                }
            }
        }

        /**
         * Gets count of tabs
         * @return void
         */
        @Override
        public int getCount() {
            return positionMap.size();
        }

        /**
         * Gets the title of the tab position
         *
         * @param position the position of the tab
         * @return void
         */
        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return ((String) getText(R.string.students_literal)).toUpperCase();
                case 1:
                    return ((String) getText(R.string.attendance)).toUpperCase();
//                case 2:
//                    return ((String) getText(R.string.schedule)).toUpperCase();
                case 2:
                    return ((String) getText(R.string.sel)).toUpperCase();
                default:
                    return "";
            }
        }
    }

}
