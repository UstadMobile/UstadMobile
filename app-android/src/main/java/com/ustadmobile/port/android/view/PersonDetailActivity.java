package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonDetailPresenter;
import com.ustadmobile.core.view.PersonDetailView;

import java.util.WeakHashMap;

/**
 * The PersonDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements PersonDetailView
 */
public class PersonDetailActivity extends UstadBaseActivity implements PersonDetailView {

    private ViewPager mPager;
    private PersonDetailViewPagerAdapter mPagerAdapter;
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
        setContentView(R.layout.activity_person_detail);

        //Toolbar
        toolbar = findViewById(R.id.activity_person_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set up view pager
        setupViewPager();


    }

    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.activity_person_detail_fields);
        mPagerAdapter = new PersonDetailViewPagerAdapter(getSupportFragmentManager());
        Fragment selectedFragment = mPagerAdapter.getItem(0);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void setField(int index, PersonDetailPresenter.PersonDetailViewField field, Object value) {

    }

    /**
     * Class : feed view pager adapter
     */
    private class PersonDetailViewPagerAdapter extends FragmentStatePagerAdapter {

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        public PersonDetailViewPagerAdapter(FragmentManager fm) {
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
