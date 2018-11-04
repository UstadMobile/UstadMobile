package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzActivityListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzActivityListView;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;



public class ClazzActivityListFragment extends UstadBaseFragment implements ClazzActivityListView {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzActivityListPresenter mPresenter;
    BarChart barChart;

    String[] changesPresets;
    Spinner activityChangesSpinner;
    HashMap<Float, Boolean> negativeValue;

    public BarChart hideEverythingInBarChart(BarChart barChart){

        //Hide only the grid lines and not axis:
        //barChart.getAxisLeft().setDrawGridLines(false);
        //barChart.getXAxis().setDrawGridLines(false);
        //barChart.getAxisRight().setDrawGridLines(false);

        //Hide all lines from x, left and right
        //Top values on X Axis
        //barChart.getXAxis().setEnabled(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getXAxis().setDrawGridLines(false);

        //Left Values
        //barChart.getAxisLeft().setEnabled(false);
        //Right Values:
        barChart.getAxisRight().setEnabled(false);


        //Legend:
        barChart.getLegend().setEnabled(false);

        //barChart.getAxisLeft().setDrawLabels(false);
        //barChart.getAxisRight().setDrawLabels(false);
        //barChart.getXAxis().setDrawLabels(false);

        //Label Description
        barChart.getDescription().setEnabled(false);

        return barChart;
    }

    class AttendanceBarDataSet extends BarDataSet {

        public AttendanceBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            //TODO: clean up
            if(negativeValue != null) {
                if (negativeValue.containsKey(getEntryForIndex(index).getX())) {
                    if(negativeValue.get(getEntryForIndex(index).getX())){
                        return mColors.get(2);
                    }
                }
            }

            return mColors.get(0);
        }

    }

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment ClazzLogListFragment.
     */
    public static ClazzActivityListFragment newInstance(long clazzUid) {
        ClazzActivityListFragment fragment = new ClazzActivityListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CLAZZ_UID, clazzUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateActivityBarChart(LinkedHashMap<Float, Float> dataMap){
        List<BarEntry> barDataEntries = new ArrayList<BarEntry>();
        Iterator<Map.Entry<Float, Float>> dataMapIterator = dataMap.entrySet().iterator();
        negativeValue = new HashMap<>();
        while(dataMapIterator.hasNext()){
            Map.Entry<Float, Float> nextEntry = dataMapIterator.next();
            Float thisBarKey = nextEntry.getKey();
            Float thisBarValue = nextEntry.getValue();

            BarEntry anEntry  = new BarEntry(thisBarKey, thisBarValue);
            if(thisBarValue < 0){
                anEntry.setIcon(getResources().getDrawable(R.drawable.ic_thumb_down_black_12dp));
            }else {
                anEntry.setIcon(getResources().getDrawable(R.drawable.ic_thumb_up_black_12dp));
            }
            if(thisBarValue <0){
                thisBarValue = (-1)*thisBarValue;
                anEntry.setY(thisBarValue);
                negativeValue.put(thisBarKey, true);

            }

            barDataEntries.add(anEntry);
        }

        //Create Bar color
        AttendanceBarDataSet dataSetBar1 = new AttendanceBarDataSet(barDataEntries,
                ACTIVITY_BAR_LABEL_DESC);
        dataSetBar1.setValueTextColor(Color.WHITE);
        dataSetBar1.setDrawValues(true);
        dataSetBar1.setValueFormatter(
                (value, entry, dataSetIndex, viewPortHandler) -> "" );

        dataSetBar1.setColors(new int[]{ContextCompat.getColor(getContext(), R.color.traffic_green),
                ContextCompat.getColor(getContext(), R.color.traffic_orange),
                ContextCompat.getColor(getContext(), R.color.traffic_red)});


        BarData barData = new BarData(dataSetBar1);

        runOnUiThread(() -> {
            setUpCharts();
            barChart.setData(barData);
            barChart.setFitBars(true);
            barChart.invalidate();
            barChart.setDrawValueAboveBar(false);
        });

    }

    @Override
    public void setClazzActivityChangesDropdownPresets(String[] presets) {
        this.changesPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, changesPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityChangesSpinner.setAdapter(adapter);



    }

    /**
     * Separated out method to set the charts up. Called on onCreateView
     */
    public void setUpCharts(){
        //Get the chart view
        barChart = rootContainer.findViewById(R.id.fragment_clazz_activity_list_bar_chart);
        barChart.setMinimumHeight(ACTIVITY_BAR_CHART_HEIGHT);
        barChart = hideEverythingInBarChart(barChart);
        //barChart.getAxisLeft().setAxisMaximum(ACTIVITY_BAR_CHART_AXIS_MAXIMUM);
        barChart.getAxisLeft().setAxisMinimum(ACTIVITY_BAR_CHART_AXIS_MINIMUM);
        barChart.setTouchEnabled(false);
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
                inflater.inflate(R.layout.fragment_clazz_activity_list, container,false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_clazz_activity_list_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Separated out Chart initialisation
        setUpCharts();


        //Record attendance FAB
        FloatingTextButton fab =
                rootContainer.findViewById(R.id.fragment_clazz_log_record_attendance_fab);
        fab.setOnClickListener(v -> mPresenter.goToNewClazzActivityEditActivity());

        //Buttons
        Button lastWeekButton = rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_thisweek);
        Button lastMonthButton = rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_thismonth);
        Button lastYearButton =rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_lastyear);

        lastWeekButton.setOnClickListener(
                v -> mPresenter.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_WEEK));
        lastMonthButton.setOnClickListener(
                v -> mPresenter.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_MONTH));
        lastYearButton.setOnClickListener(
                v -> mPresenter.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_YEAR));

        activityChangesSpinner =
                rootContainer.findViewById(R.id.fragment_clazz_activity_list_bar_chart_spinner);

        //Create the presenter and call its onCreate
        mPresenter = new ClazzActivityListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        activityChangesSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mPresenter.setClazzActivityChangeUid(id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        //return container
        return rootContainer;
    }

    //TODO: Maybe change from ClazzActivity to CumulativeClazzActivity
    // ClassLogList's DIFF callback
    public static final DiffUtil.ItemCallback<ClazzActivity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzActivity>(){

                @Override
                public boolean areItemsTheSame(ClazzActivity oldItem, ClazzActivity newItem) {
                    return oldItem.getClazzActivityUid() == newItem.getClazzActivityUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzActivity oldItem, ClazzActivity newItem) {
                    return oldItem.equals(newItem);
                }
            };

    //TODO: check this
    @Override
    public void setListProvider(UmProvider<ClazzActivity> clazzLogListProvider) {

        //Create a recycler adapter to set on the Recycler View.
        ClazzActivityListRecyclerAdapter recyclerAdapter = new ClazzActivityListRecyclerAdapter(
                DIFF_CALLBACK, getContext(), this, mPresenter, true);

        DataSource.Factory<Integer, ClazzActivity> factory =
                (DataSource.Factory<Integer, ClazzActivity>) clazzLogListProvider.getProvider();

        LiveData<PagedList<ClazzActivity>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void finish() {

    }

}