package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.PeopleListView;

/**
 * PeopleListFragment Android fragment extends UstadBaseFragment
 */
public class PeopleListFragment extends UstadBaseFragment implements PeopleListView,
        View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;

    //Swipe-refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Generates a new Fragment for a page fragment
     * TODO: Add any args if needed
     *
     * @return A new instance of fragment PeopleListFragment.
     */
    public static PeopleListFragment newInstance() {
        PeopleListFragment fragment = new PeopleListFragment();
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

        // TODO: Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_people_list, container, false);
        setHasOptionsMenu(true);

        // TODO: Set mRecyclerView..
        mRecyclerView = rootContainer.findViewById(R.id.fragment_people_list_recyclerview);

        /*
        // TODO: Use Layout: set layout manager. Change defaults
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // TODO: Specify the mAdapter
        ////mAdapter = new CustomAdapter(getContext(), some_data);
        mRecyclerView.setAdapter(mAdapter);
        */

        //Swipe-refresh
        mSwipeRefreshLayout = rootContainer.findViewById(R.id.fragment_people_list_swiperefreshview);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //return container
        return rootContainer;
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

    /**
     * View and SwipeRefreshLayout Listeners
     */
    @Override
    public void onRefresh() {
        //Update refreshing animation, etc
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
