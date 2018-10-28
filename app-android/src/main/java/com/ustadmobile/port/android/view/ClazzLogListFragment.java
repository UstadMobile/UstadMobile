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
import android.widget.Button;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

/**
 * ClazzLogListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzLogListFragment extends UstadBaseFragment implements ClassLogListView{

    View rootContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzLogListPresenter mPresenter;
    LineChart lineChart;
    HorizontalBarChart barChart;

    public HorizontalBarChart hideEverythingInBarChart(HorizontalBarChart barChart){

        //Hide only the grid lines and not axis:
        //barChart.getAxisLeft().setDrawGridLines(false);
        //barChart.getXAxis().setDrawGridLines(false);
        //barChart.getAxisRight().setDrawGridLines(false);

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.getXAxis().setEnabled(false);
        //Left Values
        barChart.getAxisLeft().setEnabled(false);
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

    public LineChart hideEverythingInLineChart(LineChart lineChart){

        //We want the Left Axis grid (vertical lines)
        //lineChart.getAxisLeft().setDrawGridLines(false);

        //We don't want horizontal grid lines:
        lineChart.getXAxis().setDrawGridLines(false);

        //We need the x axis and values on the left and right
        //lineChart.getXAxis().setEnabled(false);
        //lineChart.getAxisLeft().setEnabled(false);
        //lineChart.getAxisRight().setEnabled(false);

        //lineChart.getAxisRight().setDrawGridLines(false);

        //No need for legend:
        lineChart.getLegend().setEnabled(false);

        //We want the left label
        //lineChart.getAxisLeft().setDrawLabels(false);

        //We don't want the right label
        lineChart.getAxisRight().setDrawLabels(false);

        //We want the top labels.
        //lineChart.getXAxis().setDrawLabels(false);

        //We don't want the description label here
        lineChart.getDescription().setEnabled(false);

        return lineChart;
    }

    class AttendanceBarDataSet extends BarDataSet {

        public AttendanceBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() > 79)
                return mColors.get(0);
            else if(getEntryForIndex(index).getY() > 59)
                return mColors.get(1);
            else
                return mColors.get(2);
        }

    }

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment ClazzLogListFragment.
     */
    public static ClazzLogListFragment newInstance(long clazzUid) {
        ClazzLogListFragment fragment = new ClazzLogListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CLAZZ_UID, clazzUid);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void updateAttendanceLineChart(LinkedHashMap<Float, Float> dataMap){
        List<Entry> lineDataEntries = new ArrayList<Entry>();
        Iterator<Map.Entry<Float, Float>> dataMapIterator = dataMap.entrySet().iterator();
        while(dataMapIterator.hasNext()){
            Entry anEntry  = new Entry();
            Map.Entry<Float, Float> nextEntry = dataMapIterator.next();
            anEntry.setX(nextEntry.getKey());
            anEntry.setY(nextEntry.getValue()*100);
            lineDataEntries.add(anEntry);
        }

        //Create a line data set (one line)
        LineDataSet dataSetLine1 = new LineDataSet(lineDataEntries, ATTENDANCE_LINE_LABEL_DESC);
        dataSetLine1.setColor(Color.parseColor(ATTENDANCE_LINE_CHART_COLOR));
        dataSetLine1.setValueTextColor(Color.BLACK);
        //Don't want to see the values on the data points.
        dataSetLine1.setDrawValues(false);
        //Don't want to see the circles
        dataSetLine1.setDrawCircles(false);

        //Add LineDataSet to LineData
        LineData lineData = new LineData(dataSetLine1);


        runOnUiThread(() -> {
            setUpCharts();
            if(lineDataEntries.size() > 0){
                lineChart.setData(lineData);
                lineChart.invalidate();
            }else{
                lineChart.setData(null);
                lineChart.invalidate();
            }

        });
    }

    @Override
    public void updateAttendanceBarChart(LinkedHashMap<Float, Float> dataMap){
        List<BarEntry> barDataEntries = new ArrayList<BarEntry>();
        Iterator<Map.Entry<Float, Float>> dataMapIterator = dataMap.entrySet().iterator();
        while(dataMapIterator.hasNext()){
            Map.Entry<Float, Float> nextEntry = dataMapIterator.next();
            BarEntry anEntry  = new BarEntry(nextEntry.getKey(),
                    nextEntry.getValue()*100);
            barDataEntries.add(anEntry);
        }


        //Create Bar color
        AttendanceBarDataSet dataSetBar1 = new AttendanceBarDataSet(barDataEntries,
                ATTENDANCE_BAR_LABEL_DESC);
        dataSetBar1.setValueTextColor(Color.WHITE);
        dataSetBar1.setDrawValues(true);
        dataSetBar1.setValueFormatter(
                (value, entry, dataSetIndex, viewPortHandler) -> "" + ((int) value) + "%");

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

    /**
     * Separated out method to set the charts up. Called on onCreateView
     */
    public void setUpCharts(){
        //Get the chart view
        lineChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart);
        lineChart.setMinimumHeight(ATTENDANCE_LINE_CHART_HEIGHT);
        lineChart = hideEverythingInLineChart(lineChart);
        lineChart.getAxisLeft().setValueFormatter((value, axis) -> (int)value + "%");
        lineChart.getXAxis().setValueFormatter((value, axis) -> (int)value + "");
        lineChart.setTouchEnabled(false);
        lineChart.getXAxis().setLabelCount(4,true);

        barChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_bar_chart);
        barChart.setMinimumHeight(ATTENDANCE_BAR_CHART_HEIGHT);
        barChart = hideEverythingInBarChart(barChart);
        barChart.getAxisLeft().setAxisMaximum(ATTENDANCE_BAR_CHART_AXIS_MAXIMUM);
        barChart.getAxisLeft().setAxisMinimum(ATTENDANCE_BAR_CHART_AXIS_MINIMUM);
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
                inflater.inflate(R.layout.fragment_clazz_log_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_log_list_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Separated out Chart initialisation
        setUpCharts();

        //Create the presenter and call its onCreate
        mPresenter = new ClazzLogListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Record attendance FAB
        FloatingTextButton fab = rootContainer.findViewById(R.id.fragment_class_log_record_attendance_fab);
        fab.setOnClickListener(v -> mPresenter.goToNewClazzLogDetailActivity());

        //Buttons
        Button lastWeekButton =
                rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart_selector_button_thisweek);
        Button lastMonthButton =
                rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart_selector_button_thismonth);
        Button lastYearButton =
                rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart_selector_button_lastyear);


        lastWeekButton.setOnClickListener(
                v -> mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_WEEK));
        lastMonthButton.setOnClickListener(
                v -> mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_MONTH));
        lastYearButton.setOnClickListener(
                v -> mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_YEAR));

        //return container
        return rootContainer;
    }

    // ClassLogList's DIFF callback
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

    @Override
    public void setClazzLogListProvider(UmProvider<ClazzLog> clazzLogListProvider) {

        //Create a recycler adapter to set on the Recycler View.
        ClazzLogListRecyclerAdapter recyclerAdapter =
            new ClazzLogListRecyclerAdapter(DIFF_CALLBACK, getContext(), this, mPresenter);

        DataSource.Factory<Integer, ClazzLog> factory =
                (DataSource.Factory<Integer, ClazzLog>) clazzLogListProvider.getProvider();

        LiveData<PagedList<ClazzLog>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

}
