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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.HolidayCalendarListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.HolidayCalendarListView;
import com.ustadmobile.lib.db.entities.Holiday;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class HolidayCalendarListActivity extends UstadBaseActivity implements HolidayCalendarListView {

    private Toolbar toolbar;
    private HolidayCalendarListPresenter mPresenter;
    private RecyclerView mRecyclerView;


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
        setContentView(R.layout.activity_holiday_calendar_list);

        //Toolbar:
        toolbar = findViewById(R.id.activity_holiday_calendar_list_toolbar);
        toolbar.setTitle(getText(R.string.holiday_calendars));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_holiday_calendar_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new HolidayCalendarListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_holiday_calendar_list_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<Holiday> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Holiday>() {
                @Override
                public boolean areItemsTheSame(Holiday oldItem,
                                               Holiday newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(Holiday oldItem,
                                                  Holiday newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<Holiday> listProvider) {
        HolidayCalendarListRecyclerAdapter recyclerAdapter =
                new HolidayCalendarListRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, Holiday> factory =
                (DataSource.Factory<Integer, Holiday>)
                        listProvider.getProvider();
        LiveData<PagedList<Holiday>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }
}
