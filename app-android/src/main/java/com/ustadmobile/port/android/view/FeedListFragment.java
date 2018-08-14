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
import com.ustadmobile.core.view.FeedListView;

import java.util.ArrayList;

/**
 * FeedListFragment Android fragment extends UstadBaseFragment
 */
public class FeedListFragment extends UstadBaseFragment implements FeedListView, View.OnClickListener,
        View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;

    //Swipe-refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Generates a new Fragment for a page fragment
     *
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedListFragment newInstance() {
        FeedListFragment fragment = new FeedListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create of the fragment.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create of the View fragment . Part of Android's Fragment Override
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return the root container
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_feed_list, container, false);
        setHasOptionsMenu(true);

        //Set Recycler view
        mRecyclerView = rootContainer.findViewById(R.id.fragment_feed_list_recyclerview);

        //Use Linear Layout Manager : Set layout Manager
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // data to populate the RecyclerView with
        ArrayList<String> animalNames = new ArrayList<>();
        for(int i=0;i<10;i++){
            animalNames.add("Horse");
            animalNames.add("Cow");
            animalNames.add("Camel");
            animalNames.add("Sheep");
            animalNames.add("Goat");
        }


        //Specify the adapter
        mAdapter = new MyRecyclerViewAdapter(getContext(), animalNames);
        mRecyclerView.setAdapter(mAdapter);

        //Swipe-refresh
        mSwipeRefreshLayout = rootContainer.findViewById(R.id.fragment_feed_swiperefreshview);
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
        //TODO: Check this
        mRecyclerView.refreshDrawableState();
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
