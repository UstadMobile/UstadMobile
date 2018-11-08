package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;

/**
 * The ClassLogDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClassLogDetailView
 */
public class ClazzLogDetailActivity extends UstadBaseActivity
        implements ClassLogDetailView {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzLogDetailPresenter mPresenter;

    public long clazzLogUid;
    public long clazzUid;
    public long logDate;
    private TextView dateHeading;
    private ImageButton goOneDayBackImageView;
    private ImageButton goOneDayForwardImageView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onCreate of the Activity does the following:
     *
     * 1. Gets arguments for clazz log uid, clazz uid, logdate
     * 2. sets the recycler view
     * 3. adds handlers to all buttons on the view
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_clazz_log_detail);

        //Toolbar
        toolbar = findViewById(R.id.class_log_detail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        //Get arguments
        if (getIntent().hasExtra(ClazzListView.ARGS_CLAZZLOG_UID)){
            clazzLogUid = getIntent().getLongExtra(ClazzListView.ARGS_CLAZZLOG_UID, -1L);
        }

        if(getIntent().hasExtra(ARG_CLAZZ_UID)){
            clazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, -1L);
        }

        if(getIntent().hasExtra(ARG_LOGDATE)){
            logDate = getIntent().getLongExtra(ARG_LOGDATE, -1L);
        }


        mRecyclerView = findViewById(R.id.class_log_detail_container_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);


        mPresenter = new ClazzLogDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        if(mPresenter.currentClazz != null ){
            String clazzName = mPresenter.currentClazz.getClazzName();
            if (clazzName != null && clazzName.length() > 0) {
                toolbar.setTitle(clazzName);
            }
        }

        //FAB
        FloatingTextButton fab = findViewById(R.id.class_log_detail__done_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

        //Mark all present
        findViewById(R.id.activity_class_log_detail_mark_all_present_text)
            .setOnClickListener((view) ->
                    mPresenter.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED));

        //Mark all absent
        findViewById(R.id.activity_class_log_detail_mark_all_absent_text)
            .setOnClickListener((view) ->
                    mPresenter.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT));

        //Date heading
        dateHeading = findViewById(R.id.activity_class_log_detail_date_heading);

        goOneDayBackImageView = findViewById(R.id.activity_class_log_detail_date_go_back);
        goOneDayBackImageView.setOnClickListener(v -> mPresenter.handleClickGoBackDate());

        goOneDayForwardImageView = findViewById(R.id.activity_class_log_detail_date_go_forward);
        goOneDayForwardImageView.setOnClickListener(v -> mPresenter.handleClickGoForwardDate());

    }



    // Diff callback.
    public static final DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>() {
                @Override
                public boolean areItemsTheSame(ClazzLogAttendanceRecordWithPerson oldItem,
                                               ClazzLogAttendanceRecordWithPerson newItem) {
                    return oldItem.getClazzLogAttendanceRecordUid() ==
                            newItem.getClazzLogAttendanceRecordUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzLogAttendanceRecordWithPerson oldItem,
                                                  ClazzLogAttendanceRecordWithPerson newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setClazzLogAttendanceRecordProvider(
            UmProvider<ClazzLogAttendanceRecordWithPerson> clazzLogAttendanceRecordProvider) {

        ClazzLogDetailRecyclerAdapter recyclerAdapter =
                new ClazzLogDetailRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);

        DataSource.Factory<Integer, ClazzLogAttendanceRecordWithPerson> factory =
                (DataSource.Factory<Integer, ClazzLogAttendanceRecordWithPerson>)
                        clazzLogAttendanceRecordProvider.getProvider();
        LiveData<PagedList<ClazzLogAttendanceRecordWithPerson>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateToolbarTitle(String title) {
        runOnUiThread(() -> {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        });

    }

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    @Override
    public void updateDateHeading(String dateString) {
        //Since its called from the presenter, need to run on ui thread.

        runOnUiThread(() -> dateHeading.setText(dateString));
    }


}
