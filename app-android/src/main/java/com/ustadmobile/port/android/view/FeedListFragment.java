package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.FeedListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.FeedListView;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * FeedListFragment Android fragment extends UstadBaseFragment
 */
public class FeedListFragment extends UstadBaseFragment implements FeedListView,
        View.OnClickListener, View.OnLongClickListener {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private Toolbar toolbar;

    private FeedListPresenter mPresenter;


    public long getTodayDateInLong(){
        Calendar attendanceDate = Calendar.getInstance();
        attendanceDate.setTimeInMillis(System.currentTimeMillis());
        attendanceDate.set(Calendar.HOUR_OF_DAY, 0);
        attendanceDate.set(Calendar.MINUTE, 0);
        attendanceDate.set(Calendar.SECOND, 0);
        attendanceDate.set(Calendar.MILLISECOND, 0);

        return attendanceDate.getTimeInMillis();
    }
    /**
     * The Recycler Adapter for Feed Entries.
     */
    //FeedViewHolder
    protected class FeedListRecyclerAdapter extends
            PagedListAdapter<FeedEntry,
                    FeedListRecyclerAdapter.FeedViewHolder> {

        protected class FeedViewHolder extends RecyclerView.ViewHolder {
            protected FeedViewHolder(View itemView) {
                super(itemView);
            }
        }

        protected FeedListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<FeedEntry> diffCallback) {
            super(diffCallback);
        }

        /**
         * This method inflates the card layout (to parent view given) and returns it.
         * @param parent View given.
         * @param viewType View Type not used here.
         * @return New ViewHolder for the ClazzStudent type
         */
        @NonNull
        @Override
        public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View feedEntryListItem =
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.item_feedlist_feed, parent, false);
            return new FeedViewHolder(feedEntryListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         * @param holder    The holder
         * @param position  The position in the recycler view.
         */
        @Override
        public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {

            FeedEntry feedEntry = getItem(position);

            TextView feedText = ((TextView)holder.itemView
                .findViewById(R.id.item_feedlist_feed_title));
            ImageView feedIcon = ((ImageView)holder.itemView
                .findViewById(R.id.item_feedlist_feed_icon));
            feedText.setText(feedEntry.getTitle());
            if (getTodayDateInLong() > feedEntry.getDeadline()){
                //TODO: Apply more complex deadline with scheduling.
                feedIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.accent));
                feedText.setText(feedEntry.getTitle() + " (" + getText(R.string.overdue)+ ")");
            }

            holder.itemView.setOnClickListener(v -> mPresenter.handleClickFeedEntry(feedEntry));


        }
    }

    public static final DiffUtil.ItemCallback<FeedEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FeedEntry>() {
                @Override
                public boolean areItemsTheSame(FeedEntry oldItem,
                                               FeedEntry newItem) {
                    return oldItem.getFeedEntryHash() == newItem.getFeedEntryHash();
                }

                @Override
                public boolean areContentsTheSame(FeedEntry oldItem,
                                                  FeedEntry newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Generates a new Fragment for a page fragment
     *
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
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
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        //Create presetner and call its onCreate()
        mPresenter = new FeedListPresenter(this, UMAndroidUtil.bundleToHashtable(
                getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //return container
        return rootContainer;
    }

    @Override
    public void setFeedEntryProvider(UmProvider<FeedEntry> feedEntryUmProvider) {
        FeedListRecyclerAdapter recyclerAdapter =
                new FeedListRecyclerAdapter(DIFF_CALLBACK);

        DataSource.Factory<Integer, FeedEntry> factory =
                (DataSource.Factory<Integer, FeedEntry>) feedEntryUmProvider.getProvider();

        LiveData<PagedList<FeedEntry>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }


    @Override
    public void onResume(){
        super.onResume();
        updateTitle(getText(R.string.feed).toString());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        updateTitle(getText(R.string.feed).toString());

    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        updateTitle(getText(R.string.feed).toString());

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    /**
     * Updates the title of the toolbar.
     * TODO: check why its null sometimes
     * @param title
     */
    public void updateTitle(String title){
        //Update the parent header toolbar
        toolbar = getActivity().findViewById(R.id.base_point_2_toolbar);
        if(toolbar != null) {
            toolbar.setTitle(title);
        }
    }

}
