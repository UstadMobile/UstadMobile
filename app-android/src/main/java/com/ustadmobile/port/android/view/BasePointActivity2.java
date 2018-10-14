package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.BasePointView2;

import java.util.WeakHashMap;


/**
 * The new Base Point screen. This Activity has a bottom Navigation bar with buttons. We are using
 * an external component to achieve this vs Google's own (some differences when you start adding
 * more than 3).
 *
 * This Activity extends UstadBaseActivity and implements BasePointView
 */
public class BasePointActivity2 extends UstadBaseActivity implements BasePointView2 {

    private ViewPager mPager;
    private BasePointViewPagerAdapter mPagerAdapter;

    private CollapsingToolbarLayout mCollapsingToolbar;
    private Toolbar toolbar;

    /**
     *
     * @param menuItems String menuItems
     */
    @Override
    public void setBottomNavigationItems(String[] menuItems) {

    }

    /**
     * Get color
     * @param color
     * @return
     */
    public int fetchColor(int color){
        return ContextCompat.getColor(this, color);
    }

    /**
     * ViewPager set up in its own method for clarity.
     */
    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.container_feedlist);
        mPagerAdapter = new BasePointViewPagerAdapter(getSupportFragmentManager());
        Fragment selectedFragment = mPagerAdapter.getItem(0);
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * The overriden onCreate method does the following:
     *
     * 1. Creates, names, styles and sets the Bottom Navigation
     * 2. Sets the default location (Feed)
     * 3. Sets the toolbar title upon navigation
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_base_point2);

        //set up view pager
        setupViewPager();

        //Toolbar
        toolbar = findViewById(R.id.base_point_2_toolbar);
        toolbar.setTitle("Ustad Mobile");
        setSupportActionBar(toolbar);

        //Get the bottom navigation component.
        AHBottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);

        //Style
        bottomNavigation.setDefaultBackgroundColor(fetchColor(R.color.default_background_color_2));
        bottomNavigation.setAccentColor(fetchColor(R.color.bottom_nav_yourAccentColor));
        bottomNavigation.setInactiveColor(fetchColor(R.color.bottom_nav_yourInactiveColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setUseElevation(true, 2L);


        //Create the items to be added
        AHBottomNavigationItem feed_item =
                new AHBottomNavigationItem(R.string.feed,
                        R.drawable.ic_today_black_48dp, R.color.default_back_color);
        AHBottomNavigationItem classes_item =
                new AHBottomNavigationItem(R.string.classes,
                        R.drawable.ic_people_black_48dp, R.color.default_back_color);

        AHBottomNavigationItem people_item =
                new AHBottomNavigationItem(R.string.people,
                        R.drawable.ic_person_black_24dp, R.color.default_back_color);
//
//        AHBottomNavigationItem statistics_item =
//                new AHBottomNavigationItem(R.string.statistcs,
//                        R.drawable.ic_chart_areaspline_black_24dp, R.color.default_back_color);
//        AHBottomNavigationItem lessons_item =
//                new AHBottomNavigationItem(R.string.lessons,
//                        R.drawable.ic_book_multiple_black_24dp, R.color.default_back_color);

        //Add the items
        bottomNavigation.addItem(feed_item);
        bottomNavigation.addItem(classes_item);
        bottomNavigation.addItem(people_item);
//        bottomNavigation.addItem(lessons_item);

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(0);

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (!wasSelected) {
                    mPagerAdapter.notifyDataSetChanged();
                    Fragment selectedFragment = mPagerAdapter.getItem(position);
                    mPagerAdapter.notifyDataSetChanged();
                    mPager.setCurrentItem(position);
                }

                //Update title
                switch(position){
                    case 0:
                        updateTitle(getText(R.string.feed).toString());
                        break;
                    case 1:
                        updateTitle(getText(R.string.my_classes).toString());
                        break;
                    case 2:
                        updateTitle(getText(R.string.people).toString());
                        break;
//                    case 3:
//                        updateTitle(getText(R.string.lessons).toString());
//                        break;
                }
                return true;
            }
        });
    }

    /**
     *  Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    public void updateTitle(String title){
        toolbar.setTitle(title);
    }

    /**
     * Feed view pager's Adapter
     */
    private static class BasePointViewPagerAdapter extends FragmentStatePagerAdapter{

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        public BasePointViewPagerAdapter(FragmentManager fm) {
            super(fm);
            positionMap = new WeakHashMap<>();
        }

        public void addFragments(int pos, Fragment fragment) {
            positionMap.put(pos, (UstadBaseFragment)fragment);
        }

        /**
         * Generate fragment for that page/position
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            UstadBaseFragment thisFragment = positionMap.get(new Integer(position));
            if(thisFragment != null){
                return thisFragment;
            }else{
                switch(position){
                    case 0:
                        UstadBaseFragment newFrag =
                                FeedListFragment.newInstance();
                        this.positionMap.put(position, newFrag);
                        return newFrag;
                    case 1:
                        UstadBaseFragment classesFragment =
                                ClazzListFragment.newInstance();
                        this.positionMap.put(position, classesFragment);
                        return classesFragment;
//
//                        UstadBaseFragment peopleFragment =
//                                BasePointPeopleFragment.newInstance();
//                        this.positionMap.put(position, peopleFragment);
//                        return peopleFragment;
                    case 2:
                        UstadBaseFragment peopleListFragment =
                                PeopleListFragment.newInstance();
                        this.positionMap.put(position, peopleListFragment);
                        return peopleListFragment;

//                        UstadBaseFragment comingSoonFragment =
//                                ComingSoonFragment.newInstance();
//                        this.positionMap.put(position, comingSoonFragment);
//                        return comingSoonFragment;
//                    case 3:
//                        UstadBaseFragment csf =
//                                ComingSoonFragment.newInstance();
//                        this.positionMap.put(position, csf);
//                        return csf;
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
