package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzDetailPresenter;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.WeakHashMap;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;

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
    private boolean attendanceVisibility, activityVisibility, selVisibility;

    /**
     * Separated out view pager setup for clarity.
     */
    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.class_detail_view_pager_container);
        mPagerAdapter = new ClassDetailViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragments(0, ClazzStudentListFragment.newInstance(this.clazzUid));
        if(attendanceVisibility)
        mPagerAdapter.addFragments(1, ClazzLogListFragment.newInstance(this.clazzUid));
        if(activityVisibility)
        mPagerAdapter.addFragments(2, ClazzActivityListFragment.newInstance(this.clazzUid));
        if(selVisibility)
        mPagerAdapter.addFragments(3, SELAnswerListFragment.newInstance(this.clazzUid));
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

        mTabLayout= findViewById(R.id.activity_class_detail_tablayout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setupWithViewPager(mPager);
    }

    @Override
    public void onResume(){
        super.onResume();

        //Update title
        mPresenter.updateToolbarTitle();
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clazzdetail, menu);
        return true;
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        //If this activity started from other activity
        if (i == R.id.menu_clazzdetail_gear) {
            mPresenter.handleClickClazzEdit();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
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
        runOnUiThread(() -> {
            toolbar.setTitle(toolbarTitle);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        });

    }

    @Override
    public void setAttendanceVisibility(boolean visible) {
        attendanceVisibility = visible;
    }

    @Override
    public void setActivityVisibility(boolean visible) {
        activityVisibility = visible;
    }

    @Override
    public void setSELVisibility(boolean visible) {
        selVisibility = visible;
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
                    case 2:
                        return ClazzActivityListFragment.newInstance(clazzUid);
                    case 3:
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
                case 2:
                    return ((String) getText(R.string.activity)).toUpperCase();
                case 3:
                    return ((String) getText(R.string.sel)).toUpperCase();
                default:
                    return "";
            }
        }
    }

}
