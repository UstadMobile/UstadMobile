package com.ustadmobile.port.android.view;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzDetailPresenter;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.HashMap;
import java.util.Map;
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
    Long currentClazzUid;
    private boolean attendanceVisibility, activityVisibility, selVisibility, settingsVisibility;
    Menu menu;

    private ClazzStudentListFragment clazzStudentListFragment;

    private Map<Integer, Class> fragPosMap = new HashMap<>();

    /**
     * Separated out view pager setup for clarity.
     */
    @Override
    public void setupViewPager() {

        runOnUiThread(() -> {
            mPager = findViewById(R.id.class_detail_view_pager_container);
            mPagerAdapter = new ClassDetailViewPagerAdapter(getSupportFragmentManager());
            int fragCount = 0;
            mPagerAdapter.addFragments(fragCount, ClazzStudentListFragment.newInstance(currentClazzUid));
            fragPosMap.put(fragCount++, ClazzStudentListFragment.class);

            if(attendanceVisibility) {
                mPagerAdapter.addFragments(fragCount, ClazzLogListFragment.newInstance(currentClazzUid));
                fragPosMap.put(fragCount++, ClazzLogListFragment.class);
            }

            if(activityVisibility) {
                mPagerAdapter.addFragments(fragCount, ClazzActivityListFragment.newInstance(currentClazzUid));
                fragPosMap.put(fragCount++, ClazzActivityListFragment.class);
            }

            if(selVisibility) {
                mPagerAdapter.addFragments(fragCount, SELAnswerListFragment.newInstance(currentClazzUid));
                fragPosMap.put(fragCount++, SELAnswerListFragment.class);
            }

            mPager.setAdapter(mPagerAdapter);

            mTabLayout= findViewById(R.id.activity_class_detail_tablayout);
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            mTabLayout.setupWithViewPager(mPager);

        });

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

        currentClazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, 0L);

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
        //setupViewPager();
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
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clazzdetail, menu);

        MenuItem settingsGearMenuItem = menu.findItem(R.id.menu_clazzdetail_gear);
        Drawable gearIcon =
                AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_settings_white_24dp);
        gearIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);
        settingsGearMenuItem.setIcon(gearIcon);

        if(menu != null) {
            if(settingsGearMenuItem != null)
                settingsGearMenuItem.setVisible(settingsVisibility);
        }

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
        }
        else if(i == R.id.menu_clazzdetail_search){
            mPresenter.handleClickSearch();
            return super.onOptionsItemSelected(item);
        }
        else if(i == android.R.id.home) {
            onBackPressed();
            return true;
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

    @Override
    public void setSettingsVisibility(boolean visible) {
        settingsVisibility = visible;
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
                Class fragClass = fragPosMap.get(position);
                if(fragClass.equals(ClazzStudentListFragment.class)) {
                    clazzStudentListFragment = ClazzStudentListFragment.newInstance(currentClazzUid);
                    return clazzStudentListFragment;
                }else if(fragClass.equals(ClazzLogListFragment.class)) {
                    return ClazzLogListFragment.newInstance(currentClazzUid);
                }else if(fragClass.equals(ClazzActivityListFragment.class)) {
                    return ClazzActivityListFragment.newInstance(currentClazzUid);
                }else if(fragClass.equals(SELAnswerListFragment.class)) {
                    return SELAnswerListFragment.newInstance(currentClazzUid);
                }else{
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
            Class fragClass = fragPosMap.get(position);
            if(fragClass.equals(ClazzStudentListFragment.class)) {
                return ((String) getText(R.string.students_literal)).toUpperCase();
            }else if(fragClass.equals(ClazzLogListFragment.class)) {
                return ((String) getText(R.string.attendance)).toUpperCase();
            }else if(fragClass.equals(ClazzActivityListFragment.class)) {
                return ((String) getText(R.string.activity)).toUpperCase();
            }else if(fragClass.equals(SELAnswerListFragment.class)) {
                return ((String) getText(R.string.sel)).toUpperCase();
            }else {
                return "";
            }
        }
    }

}
