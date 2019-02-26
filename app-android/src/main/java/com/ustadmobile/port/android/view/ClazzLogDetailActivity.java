package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Locale;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * The ClassLogDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClassLogDetailView
 */
public class ClazzLogDetailActivity extends UstadBaseActivity
        implements ClassLogDetailView {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;

    private ClazzLogDetailPresenter mPresenter;

    private TextView dateHeading;

    private Button markAllPresent, markAllAbsent;

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
     * @param savedInstanceState    The bundle saved state
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

        mRecyclerView = findViewById(R.id.class_log_detail_container_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        AppCompatImageButton backDate = findViewById(R.id.activity_class_log_detail_date_go_back);
        AppCompatImageButton forwardDate = findViewById(R.id.activity_class_log_detail_date_go_forward);

        markAllPresent = findViewById(R.id.activity_class_log_detail_mark_all_present_text);
        markAllAbsent = findViewById(R.id.activity_class_log_detail_mark_all_absent_text);

        FloatingTextButton fab = findViewById(R.id.class_log_detail__done_fab);

        //Date heading
        dateHeading = findViewById(R.id.activity_class_log_detail_date_heading);

        mPresenter = new ClazzLogDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        //Change icon based on rtl in current language (eg: arabic)
        int isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault());
        switch (isLeftToRight){
            case ViewCompat.LAYOUT_DIRECTION_RTL:
                backDate.setImageDrawable(AppCompatResources.getDrawable(
                        getApplicationContext(), R.drawable.ic_chevron_right_black_24dp));
                forwardDate.setImageDrawable(AppCompatResources.getDrawable(getApplication(),
                        R.drawable.ic_chevron_left_black_24dp));
        }

        //FAB
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

        //Mark all present
        markAllPresent.setOnClickListener((view) ->
                    mPresenter.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED));

        //Mark all absent
        markAllAbsent.setOnClickListener((view) ->
                    mPresenter.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT));

        backDate.setOnClickListener(v -> mPresenter.handleClickGoBackDate());

        forwardDate.setOnClickListener(v -> mPresenter.handleClickGoForwardDate());

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
                                              @NonNull ClazzLogAttendanceRecordWithPerson newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setClazzLogAttendanceRecordProvider(
            UmProvider<ClazzLogAttendanceRecordWithPerson> clazzLogAttendanceRecordProvider) {

        ClazzLogDetailRecyclerAdapter recyclerAdapter =
                new ClazzLogDetailRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);

        //A warning is expected
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

    @Override
    public void showMarkAllButtons(boolean show) {
        if(show) {
            markAllAbsent.setVisibility(View.VISIBLE);
            markAllPresent.setVisibility(View.VISIBLE);
        }else{
            markAllAbsent.setVisibility(View.INVISIBLE);
            markAllPresent.setVisibility(View.INVISIBLE);
        }
    }


}
