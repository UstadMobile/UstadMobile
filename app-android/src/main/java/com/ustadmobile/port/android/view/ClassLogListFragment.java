package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ClassLogListFragment Android fragment extends UstadBaseFragment
 */
public class ClassLogListFragment extends UstadBaseFragment implements ClassLogListView,
        View.OnClickListener, View.OnLongClickListener {

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzLogListPresenter mPresenter;


    protected class ClazzLogListRecyclerAdapter extends
            PagedListAdapter<ClazzLog, ClazzLogListRecyclerAdapter.ClazzLogViewHolder>{

        protected class ClazzLogViewHolder extends RecyclerView.ViewHolder{
            protected ClazzLogViewHolder(View itemView){
                super(itemView);
            }
        }

        protected ClazzLogListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzLog> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            View clazzLogListItem =
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.item_clazzlog_log, parent, false);
            return new ClazzLogViewHolder(clazzLogListItem);

        }

        @Override
        public void onBindViewHolder(@NonNull ClazzLogViewHolder holder, int position){
            ClazzLog clazzLog = getItem(position);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(clazzLog.getLogDate());
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd/MMMM/yyyy");
            SimpleDateFormat formatShortDay = new SimpleDateFormat("EEE");
            String prettyDate = format.format(calendar.getTime());
            String prettyShortDay = formatShortDay.format(calendar.getTime());
            int presentCount = clazzLog.getNumPresent();
            int absentCount = clazzLog.getNumAbsent();
            String clazzLogAttendanceStatus = presentCount + " " +
                    getText(R.string.present) + ", " + absentCount + " " +
                    getText(R.string.absent);

            ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_date))
                    .setText(prettyDate);
            ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_day))
                    .setText(prettyShortDay);
            ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_status_text))
                    .setText(clazzLogAttendanceStatus);

            holder.itemView.setOnClickListener(v -> mPresenter.goToClazzLogDetailActivity(clazzLog));
        }
    }

    public static final DiffUtil.ItemCallback<ClazzLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzLog>(){

                @Override
                public boolean areItemsTheSame(ClazzLog oldItem, ClazzLog newItem) {
                    return oldItem.getClazzLogUid() == newItem.getClazzLogUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzLog oldItem, ClazzLog newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Generates a new Fragment for a page fragment
     * TODO: Add any args if needed
     *
     * @return A new instance of fragment ClassLogListFragment.
     */
    public static ClassLogListFragment newInstance(long clazzUid) {
        ClassLogListFragment fragment = new ClassLogListFragment();
        Bundle args = new Bundle();
        args.putLong("clazzUid", clazzUid);
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

        rootContainer =
                inflater.inflate(R.layout.fragment_class_log_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_log_list_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL);

        //Create the presenter and call its onCreate
        mPresenter = new ClazzLogListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //return container
        return rootContainer;
    }

    @Override
    public void setClazzLogListProvider(UmProvider<ClazzLog> clazzLogListProvider) {

        //Create a recycler adapter to set on the Recycler View.
        ClazzLogListRecyclerAdapter recyclerAdapter =
                new ClazzLogListRecyclerAdapter(DIFF_CALLBACK);

        DataSource.Factory<Integer, ClazzLog> factory =
                (DataSource.Factory<Integer, ClazzLog>) clazzLogListProvider.getProvider();

        LiveData<PagedList<ClazzLog>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
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
