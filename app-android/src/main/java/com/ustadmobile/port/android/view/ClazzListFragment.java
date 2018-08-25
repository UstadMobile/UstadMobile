package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ClazzListView;

import java.util.ArrayList;

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzListFragment extends UstadBaseFragment implements ClazzListView,
        View.OnClickListener, View.OnLongClickListener{

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private Toolbar toolbar;

    //Swipe-refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Generates a new Fragment for a page fragment
     * TODO: Add any args if needed
     *
     * @return A new instance of fragment ClazzListFragment.
     */
    public static ClazzListFragment newInstance() {
        ClazzListFragment fragment = new ClazzListFragment();
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

        rootContainer = inflater.inflate(R.layout.fragment_class_list, container, false);
        setHasOptionsMenu(true);


        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_list_recyclerview);

        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        //Get test data
        // data to populate the RecyclerView with
        ArrayList<String> classNames = new ArrayList<>();
        for(int i=0;i<16;i++){
            classNames.add("Class " + i + 1);
        }

        mAdapter = new CardRecyclerViewAdapter(getContext(), classNames);
        mRecyclerView.setAdapter(mAdapter);

        //Update the parent header toolbar
        toolbar = getActivity().findViewById(R.id.base_point_2_toolbar);
        toolbar.setTitle(getText(R.string.my_classes));

        //return container
        return rootContainer;
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
