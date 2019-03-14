package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.HolidayCalendarDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.HolidayCalendarDetailView;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.entities.UMCalendar;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class HolidayCalendarDetailActivity extends UstadBaseActivity implements HolidayCalendarDetailView {

    private Toolbar toolbar;
    private HolidayCalendarDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private ConstraintLayout addRangeCL;
    private EditText title;


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_holiday_calendar_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_holiday_calendar_detail_toolbar);
        toolbar.setTitle(getText(R.string.new_holiday));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_holiday_calendar_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        addRangeCL = findViewById(R.id.activity_holiday_calendar_detail_add_cl);

        title = findViewById(R.id.activity_holiday_calendar_detail_name);

        //Call the Presenter
        mPresenter = new HolidayCalendarDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        addRangeCL.setOnClickListener(v -> mPresenter.handleAddDateRange());

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_holiday_calendar_detail_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<DateRange> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DateRange>() {
                @Override
                public boolean areItemsTheSame(DateRange oldItem,
                                               DateRange newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(DateRange oldItem,
                                                  DateRange newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<DateRange> listProvider) {
        HolidayCalendarDetailRecyclerAdapter recyclerAdapter =
                new HolidayCalendarDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, DateRange> factory =
                (DataSource.Factory<Integer, DateRange>)
                        listProvider.getProvider();
        LiveData<PagedList<DateRange>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateCalendarOnView(UMCalendar calendar) {
        title.setText(calendar.getUmCalendarName());

    }
}
