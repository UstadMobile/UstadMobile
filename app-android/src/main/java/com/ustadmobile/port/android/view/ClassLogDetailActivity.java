package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ClassLogDetailView;

import java.util.WeakHashMap;

/**
 * The ClassLogDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ClassLogDetailView
 */
public class ClassLogDetailActivity extends UstadBaseActivity implements ClassLogDetailView {

    private ViewPager mPager;
    private ClassLogDetailViewPagerAdapter mPagerAdapter;
    //Toolbar
    private Toolbar toolbar;

    /**
     * Get color
     *
     * @param color
     * @return
     */
    public int fetchColor(int color) {
        return ContextCompat.getColor(this, color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_class_log_detail);

        //set up view pager
        setupViewPager();

        //Toolbar
        toolbar = findViewById(R.id.class_log_detail_toolbar);
        toolbar.setTitle("Ustad Mobile");
        setSupportActionBar(toolbar);


    }

    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.container_feedlist);
        mPagerAdapter = new ClassLogDetailViewPagerAdapter(getSupportFragmentManager());
        Fragment selectedFragment = mPagerAdapter.getItem(0);
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * Class : feed view pager adapter
     */
    private class ClassLogDetailViewPagerAdapter extends FragmentStatePagerAdapter {

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        public ClassLogDetailViewPagerAdapter(FragmentManager fm) {
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
                    default:
                        return null;
                }

            }
        }

        @Override
        public int getCount() {
            return positionMap.size();
        }
    }


}
