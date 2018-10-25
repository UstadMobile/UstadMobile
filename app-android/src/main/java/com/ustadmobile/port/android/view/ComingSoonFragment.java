package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;

/**
 * ComingSoonFragment Android fragment extends UstadBaseFragment
 */
public class ComingSoonFragment extends UstadBaseFragment implements View.OnClickListener,
        View.OnLongClickListener {

    View rootContainer;

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment ComingSoonFragment.
     */
    public static ComingSoonFragment newInstance() {
        ComingSoonFragment fragment = new ComingSoonFragment();
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

        //Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_coming_soon,container, false);
        setHasOptionsMenu(true);

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
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
