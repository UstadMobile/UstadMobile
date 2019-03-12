package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.FeedListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.FeedListView;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

/**
 * FeedListFragment Android fragment extends UstadBaseFragment - fragment responsible for displaying
 * the feed page and actions on them depending on the feed.
 */
public class FeedListFragment extends UstadBaseFragment implements FeedListView,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener  {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private TextView numClassesView, numStudentsView, attendancePercentageView;
    private FeedListPresenter mPresenter;
    private Button reportButton;
    private ImageView reportImageView;
    private CardView summaryCard;

    /**
     * The Diff callback
     */
    public static final DiffUtil.ItemCallback<FeedEntry> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<FeedEntry>() {
            @Override
            public boolean areItemsTheSame(FeedEntry oldItem,
                                           FeedEntry newItem) {
                return oldItem.getFeedEntryHash() == newItem.getFeedEntryHash();
            }

            @Override
            public boolean areContentsTheSame(FeedEntry oldItem,
                                              @NonNull FeedEntry newItem) {
                return oldItem.equals(newItem);
            }
        };

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
    public static FeedListFragment newInstance() {


//        // ACRA exception testing: Remove me.
//        String a = null;
//        if(a.equals("null")){
//            a=null;
//        }

        FeedListFragment fragment = new FeedListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_feed_list, container, false);
        setHasOptionsMenu(true);

        //Set Recycler view
        mRecyclerView = rootContainer.findViewById(R.id.fragment_feed_list_recyclerview);

        //Use Linear Layout Manager : Set layout Manager
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Swipe trial:
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        //end of Swipe

        numClassesView =
                rootContainer.findViewById(R.id.fragment_feed_list_report_card_num_classes);
        numStudentsView =
                rootContainer.findViewById(R.id.fragment_feed_list_report_card_num_students);
        attendancePercentageView =
                rootContainer.findViewById(R.id.fragment_feed_list_report_card_attendance_percentage);
        reportButton =
                rootContainer.findViewById(R.id.fragment_feed_list_report_card_view_report);
        reportImageView =
                rootContainer.findViewById(R.id.fragment_feed_list_report_card_report_icon);

        summaryCard = rootContainer.findViewById(R.id.fragment_feed_list_report_card);

        //Create presenter and call its onCreate()
        mPresenter = new FeedListPresenter(getContext(), UMAndroidUtil.bundleToHashtable(
                getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        reportButton.setOnClickListener(v -> mPresenter.handleClickViewReports());

        return rootContainer;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        System.out.println("hi");
        FeedListRecyclerAdapter adapter = (FeedListRecyclerAdapter) mRecyclerView.getAdapter();
        long feedUid = adapter.positionToFeedUid.get(position);
        if(feedUid != 0){
            mPresenter.markFeedAsDone(feedUid);
        }
        adapter.notifyItemRemoved(position);

    }

    @Override
    public void setFeedEntryProvider(UmProvider<FeedEntry> feedEntryUmProvider) {
        FeedListRecyclerAdapter recyclerAdapter =
                new FeedListRecyclerAdapter(this, DIFF_CALLBACK, getContext(),
                        mPresenter);

        // A warning is expected
        DataSource.Factory<Integer, FeedEntry> factory =
                (DataSource.Factory<Integer, FeedEntry>) feedEntryUmProvider.getProvider();

        LiveData<PagedList<FeedEntry>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * Updates the number of classes in the Summary card. View is updated on UI Thread since it is
     * being called from the presenter (probably on another async thread)
     *
     * @param num   The total number of classes
     */
    @Override
    public void updateNumClasses(int num) {
        runOnUiThread(() -> {
            String numClassesText = Integer.toString(num);
            numClassesView.setText(numClassesText);
        });
    }

    /**
     * Update the number of students on the Summary card. View is updated on UI Thread since it is
     * being called from the presenter (probably on another async thread)
     *
     * @param num   The total number of students
     */
    @Override
    public void updateNumStudents(int num) {
        runOnUiThread(() -> {
            String numStudentsText = Integer.toString(num);
            numStudentsView.setText(numStudentsText);
        });
    }

    /**
     * Update the number of attendance percentage of the Summary card. View is updated on UI Thread
     * since it is bring called from the presenter (probably on another async thread)
     *
     * @param per   The percentage value in double - triple digits (ie: 42 or 100)
     */
    @Override
    public void updateAttendancePercentage(int per) {
        String concatString = Integer.toString(per) + "%";
        runOnUiThread(() -> attendancePercentageView.setText(concatString));
    }

    @Override
    public void updateAttendanceTrend(int trend, int per) {
        switch(trend){
            case FEED_LIST_ATTENDANCE_TREND_UP:
                break;
            case FEED_LIST_ATTENDANCE_TREND_DOWN:
                break;
            case FEED_LIST_ATTENDANCE_TREND_FLAT:
                break;
            default:
                break;
        }
    }

    @Override
    public void showReportOptionsOnSummaryCard(boolean visible) {
        runOnUiThread(() -> {
            reportImageView.setVisibility(visible?View.VISIBLE:View.GONE);
            reportButton.setVisibility(visible?View.VISIBLE:View.GONE);
        });

    }

    @Override
    public void showSummaryCard(boolean visible) {
        runOnUiThread(() -> {
            summaryCard.setVisibility(visible?View.VISIBLE:View.GONE);
            summaryCard.setEnabled(visible);
        });
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

    /**
     * Updates the title of the toolbar.
     *
     * @param title     The title of the toolbar that needs updating
     */
    public void updateTitle(String title){
        //Update the parent header toolbar
        Toolbar toolbar =
                Objects.requireNonNull(getActivity()).findViewById(R.id.base_point_2_toolbar);
        if(toolbar != null) {
            toolbar.setTitle(title);
        }
    }
}
