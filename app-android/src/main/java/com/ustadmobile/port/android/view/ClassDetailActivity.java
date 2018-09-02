package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.lib.db.entities.Clazz;

import java.util.WeakHashMap;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

/**
 * The ClassDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ClassDetailView
 */
public class ClassDetailActivity extends UstadBaseActivity implements
        ClassDetailView, TabLayout.OnTabSelectedListener {

    private ViewPager mPager;
    private ClassDetailViewPagerAdapter mPagerAdapter;
    //Toolbar
    private Toolbar toolbar;
    private TabLayout mTabLayout;


    Long clazzUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_class_detail);

        clazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, 0L);

        //Toolbar
        toolbar = findViewById(R.id.class_detail_toolbar);
        //Set title as Class name
        toolbar.setTitle("Class");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set up view pager
        setupViewPager();

        mTabLayout= (TabLayout)findViewById(R.id.activity_class_detail_tablayout);
        mTabLayout.setupWithViewPager(mPager);



    }

    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.class_detail_view_pager_container);
        mPagerAdapter = new ClassDetailViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragments(0, ClazzStudentListFragment.newInstance(this.clazzUid));
        mPagerAdapter.addFragments(1, ClassLogListFragment.newInstance(this.clazzUid));
        mPagerAdapter.addFragments(2, ComingSoonFragment.newInstance());
        //Fragment selectedFragment = mPagerAdapter.getItem(0);
        mPager.setAdapter(mPagerAdapter);


    }

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

    /**
     * Class : feed view pager adapter
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
         * Generate fragment for that page/position
         *
         * @param position
         * @return
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
                        return ClassLogListFragment.newInstance(clazzUid);
                    case 2:
                        return ComingSoonFragment.newInstance();
                    default:
                        return null;
                }

            }
        }

        @Override
        public int getCount() {
            return positionMap.size();
        }


        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    String students_title = (String)
                            getText(R.string.students_literal);
                    return students_title.toUpperCase();
                case 1:
                    String log_title = (String)
                            getText(R.string.log);
                    return log_title.toUpperCase();
                case 2:
                    String schedule_title = (String)
                            getText(R.string.schedule);
                    return schedule_title.toUpperCase();
                default:
                    return "";
            }
        }
    }


}
