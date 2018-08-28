package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.WeakHashMap;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * The ClassLogDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ClassLogDetailView
 */
public class ClassLogDetailActivity extends UstadBaseActivity
        implements ClassLogDetailView, View.OnClickListener, View.OnLongClickListener{

    //Toolbar
    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzLogDetailPresenter mPresenter;

    public long clazzLogUid;
    public long clazzUid;
    public long logDate;

    protected class ClazzLogDetailRecyclerAdapter
            extends PagedListAdapter<ClazzLogAttendanceRecord,
                ClazzLogDetailRecyclerAdapter.ClazzLogDetailViewHolder>{

        protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
            protected ClazzLogDetailViewHolder(View itemView){
                super(itemView);
            }
        }

        protected ClazzLogDetailRecyclerAdapter(
                @NonNull DiffUtil.ItemCallback<ClazzLogAttendanceRecord> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

            View clazzLogDetailListItem =
                    LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.item_clazzlog_detail_student, parent, false);
            return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
        }

        public void voidAllRecordIcons(@NonNull ClazzLogDetailViewHolder holder){
            ((ImageView)holder.itemView
                    .findViewById(R.id.item_clazzlog_detail_student_present_icon))
                    .setColorFilter(Color.GRAY);
            ((ImageView)holder.itemView
                    .findViewById(R.id.item_clazzlog_detail_student_absent_icon))
                    .setColorFilter(Color.GRAY);
            ((ImageView)holder.itemView
                    .findViewById(R.id.item_clazzlog_detail_student_delay_icon))
                    .setColorFilter(Color.GRAY);
        }


        @Override
        public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
            ClazzLogAttendanceRecord attendanceRecord = getItem(position);
            String studentName =
                    "Student Name " + attendanceRecord.getClazzLogAttendanceRecordClazzMemberUid();
            int studentAttendance = attendanceRecord.getAttendanceStatus();

            ((TextView)holder.itemView
                    .findViewById(R.id.item_clazzlog_detail_student_name)).setText(studentName);
            ((ImageView)holder.itemView
                    .findViewById(R.id.item_clazzlog_detail_student_present_icon)).setColorFilter(Color.BLACK);


            voidAllRecordIcons(holder);

            switch(studentAttendance){
                case ClazzLogAttendanceRecord.STATUS_ATTENDED:
                    ((ImageView)holder.itemView
                            .findViewById(R.id.item_clazzlog_detail_student_present_icon))
                            .setColorFilter(Color.BLACK);
                    break;
                case ClazzLogAttendanceRecord.STATUS_ABSENT:
                    ((ImageView)holder.itemView
                            .findViewById(R.id.item_clazzlog_detail_student_absent_icon))
                            .setColorFilter(Color.BLACK);
                    break;
                case ClazzLogAttendanceRecord.STATUS_PARTIAL:
                    ((ImageView)holder.itemView
                            .findViewById(R.id.item_clazzlog_detail_student_delay_icon))
                            .setColorFilter(Color.BLACK);
                    break;
                case 0:
                    break;
                default:
                    break;
            }
        }
    }

    public static final DiffUtil.ItemCallback<ClazzLogAttendanceRecord> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzLogAttendanceRecord>() {
                @Override
                public boolean areItemsTheSame(ClazzLogAttendanceRecord oldItem,
                                               ClazzLogAttendanceRecord newItem) {
                    return oldItem.getClazzLogAttendanceRecordUid() ==
                            newItem.getClazzLogAttendanceRecordUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzLogAttendanceRecord oldItem,
                                                  ClazzLogAttendanceRecord newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * onCreate of the Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_class_log_detail);

        //Toolbar
        toolbar = findViewById(R.id.class_log_detail_toolbar);
        toolbar.setTitle("Class");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        //arguments
        if (getIntent().hasExtra("clazzloguid")){
            clazzLogUid = getIntent().getLongExtra("clazzloguid", -1L);
        }

        if(getIntent().hasExtra("clazzuid")){
            clazzUid = getIntent().getLongExtra("clazzuid", -1L);
        }

        if(getIntent().hasExtra("logdate")){
            logDate = getIntent().getLongExtra("logdate", -1L);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.class_log_detail_container_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(),
                        LinearLayoutManager.VERTICAL);

        mPresenter = new ClazzLogDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        FloatingTextButton fab = findViewById(R.id.class_log_detail__done_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleClickDone();
            }
        });

        Button allPresentButton = findViewById(R.id.activity_class_log_detail_mark_all_present_text);
        allPresentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleMarkAllPresent();
            }
        });

        Button allAbsentButton = findViewById(R.id.activity_class_log_detail_mark_all_absent_text);
        allAbsentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleMarkAllAbsent();
            }
        });


    }

    @Override
    public void setClazzLogAttendanceRecordProvider(
            UmProvider<ClazzLogAttendanceRecord> clazzLogAttendanceRecordProvider) {
        ClazzLogDetailRecyclerAdapter recyclerAdapter =
                new ClazzLogDetailRecyclerAdapter(DIFF_CALLBACK);
        DataSource.Factory<Integer, ClazzLogAttendanceRecord> factory =
                (DataSource.Factory<Integer, ClazzLogAttendanceRecord>)
                        clazzLogAttendanceRecordProvider.getProvider();
        LiveData<PagedList<ClazzLogAttendanceRecord>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }


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
    public void onClick(View v) {
        int x;
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


}
