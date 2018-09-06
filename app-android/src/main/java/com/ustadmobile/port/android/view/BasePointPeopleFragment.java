package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.BasePointPeopleView;

/**
 * BasePointPeopleFragment Android fragment extends UstadBaseFragment
 */
public class BasePointPeopleFragment extends UstadBaseFragment implements BasePointPeopleView,
        View.OnClickListener, View.OnLongClickListener{

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ViewPager mPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    //Swipe-refresh
    //private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment BasePointPeopleFragment.
     */
    public static BasePointPeopleFragment newInstance() {
        BasePointPeopleFragment fragment = new BasePointPeopleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create of the fragment.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create of the View fragment . Part of Android's Fragment Override
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return the root container
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_base_point_people,container, false);
        setHasOptionsMenu(true);

        // Specify the mAdapter
        mPager = (ViewPager) rootContainer.findViewById(R.id.fragment_base_point_people_container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mSectionsPagerAdapter);

        //return container
        return rootContainer;
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

    /**
     * View Listeners
     */

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ClazzListFragment.newInstance();
                default:
                    return PeopleListFragment.newInstance();
            }
            //return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    String classes_title =
                            (String) getContext().getResources().getText(R.string.classes);
                    return classes_title.toUpperCase();
                default:
                    String people_title =
                            (String) getContext().getResources().getText(R.string.people);
                    return people_title.toUpperCase();
            }


        }
    }
}
