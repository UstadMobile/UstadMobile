package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;

/**
 * ClazzLogListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzLogListFragment extends UstadBaseFragment implements ClassLogListView{

    View rootContainer;
    private RecyclerView mRecyclerView;

    private ClazzLogListPresenter mPresenter;
    LineChart lineChart;
    HorizontalBarChart barChart;

    private Button lastWeekButton;
    private Button lastMonthButton;
    private Button lastYearButton;

    FloatingTextButton fab;

    /**
     * Hides elements of MPAndroid Chart that we do not need as part of the Bar Chart in the
     * Attendance Log list fragment. Hides things as per UI intended (axis, labels, etc)
     *
     * @param barChart  The horizontal bar chart
     * @return  The horizontal bar chart with elements hidden
     */
    public HorizontalBarChart hideEverythingInBarChart(HorizontalBarChart barChart){

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.getXAxis().setEnabled(false);
        //Left Values
        barChart.getAxisLeft().setEnabled(false);
        //Right Values:
        barChart.getAxisRight().setEnabled(false);

        //Legend:
        barChart.getLegend().setEnabled(false);

        //Label Description
        barChart.getDescription().setEnabled(false);

        return barChart;
    }

    /**
     * Hides elements of MpAndroid Chart that we do not need as part of the Line Chart in the
     * Attendance Log list fragment. Hides things as per UI intended (axis, labels, etc)
     *
     * @param lineChart The line chart
     * @return  The line chart with elements hidden.
     */
    public LineChart hideEverythingInLineChart(LineChart lineChart){

        //We want the Left Axis grid (vertical lines)
        //lineChart.getAxisLeft().setDrawGridLines(false);

        //We don't want horizontal grid lines:
        lineChart.getXAxis().setDrawGridLines(false);

        //No need for legend:
        lineChart.getLegend().setEnabled(false);

        //We don't want the right label
        lineChart.getAxisRight().setDrawLabels(false);

        //We don't want the description label here
        lineChart.getDescription().setEnabled(false);

        return lineChart;
    }

    /**
     * Custom Bar Data Set. The idea is any color logic can be applied here.
     */
    class AttendanceBarDataSet extends BarDataSet {

        AttendanceBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            return mColors.get(index);
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


    /**
     * Updates the line chart with attendance with data specified to it. This is directly drawing
     * on the MPAndroid Chart View in the Fragment.
     *
     * @param dataMap The hash map containing the values
     */
    @Override
    public void updateAttendanceLineChart(LinkedHashMap<Float, Float> dataMap){
        List<Entry> lineDataEntries = new ArrayList<>();
        List<Entry> lineDataEntriesZero = new ArrayList<>();
        for (Map.Entry<Float, Float> floatFloatEntry : dataMap.entrySet()) {
            Entry anEntry = new Entry();
            float x = floatFloatEntry.getKey();
            float y = floatFloatEntry.getValue();
            anEntry.setX(x);
            anEntry.setY(y*100);

            if(y > -1){ //If the value is meant to be 0
                lineDataEntries.add(anEntry);
            }else{
                //Not meant to be 0 - join adjacent lines (ie: not a school day/no ClazzLog)
                lineDataEntriesZero.add(new Entry(x,0));
            }



        }

        //0 data set
        LineDataSet dataSetLine0 = new LineDataSet(lineDataEntriesZero, "Line 0 label");
        dataSetLine0.setColor(android.R.color.transparent);
        dataSetLine0.setValueTextColor(android.R.color.transparent);
        dataSetLine0.setDrawValues(false);
        dataSetLine0.setDrawCircles(false);

        //Create a line data set (one line)
        LineDataSet dataSetLine1 = new LineDataSet(lineDataEntries, ATTENDANCE_LINE_LABEL_DESC);
        dataSetLine1.setColor(Color.parseColor(ATTENDANCE_LINE_CHART_COLOR));
        dataSetLine1.setValueTextColor(Color.BLACK);
        dataSetLine1.setLineWidth(2.0f);
        //Don't want to see the values on the data points.
        dataSetLine1.setDrawValues(false);
        //Don't want to see the circles
        dataSetLine1.setDrawCircles(false);

        //dataSetLine1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //Add LineDataSet to LineData
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetLine1);
        //Add 0 line :
        lineData.addDataSet(dataSetLine0);

        //Update the lineChart on the UI thread (since this method is called via the Presenter)
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

    /**
     * Update the bar chart with attendance with data specified to it. This is directly drawing
     * on the MPAndroid Chart View in the Fragment.
     * @param dataMap the Hashmap containing the values.
     */
    @Override
    public void updateAttendanceBarChart(LinkedHashMap<Float, Float> dataMap){
        List<BarEntry> barDataEntries = new ArrayList<>();
        for (Map.Entry<Float, Float> nextEntry : dataMap.entrySet()) {
            BarEntry anEntry = new BarEntry(nextEntry.getKey(),
                    nextEntry.getValue() * 100);
            barDataEntries.add(anEntry);
        }

        //Create Bar color
        AttendanceBarDataSet dataSetBar1 = new AttendanceBarDataSet(barDataEntries,
                ATTENDANCE_BAR_LABEL_DESC);
        dataSetBar1.setValueTextColor(Color.GRAY);
        dataSetBar1.setDrawValues(true);
        dataSetBar1.setValueFormatter(
                (value, entry, dataSetIndex, viewPortHandler) -> "" + ((int) value) + "%");

        int[] colors = new int[]{ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.traffic_green),
                ContextCompat.getColor(getContext(), R.color.traffic_orange),
                ContextCompat.getColor(getContext(), R.color.traffic_red)};
        dataSetBar1.setColors(colors);


        BarData barData = new BarData(dataSetBar1);

        runOnUiThread(() -> {
            setUpCharts();
            barChart.setData(barData);
            barChart.setFitBars(true);
            barChart.invalidate();
        });

    }

    /**
     * Converts dp to pixels (used in MPAndroid charts)
     *
     * @param dp    dp number
     * @return      pixels number
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Separated out method to set the charts up. Called on onCreateView
     */
    public void setUpCharts(){
        //Get the chart view
        lineChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart);
        lineChart.setMinimumHeight(dpToPx(ATTENDANCE_LINE_CHART_HEIGHT));
        lineChart = hideEverythingInLineChart(lineChart);
        lineChart.getAxisLeft().setValueFormatter((value, axis) -> (int)value + "%");
        lineChart.getXAxis().setValueFormatter((value, axis) -> (int)value + "");
        lineChart.setTouchEnabled(false);
        lineChart.getXAxis().setLabelCount(4,true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisLeft().setLabelCount(4, true);
        //lineChart.setExtraOffsets(0,-150,0,0);
        lineChart.getXAxis().setAxisMinimum(0);
        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getAxisLeft().setAxisMaximum(100);

        barChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_bar_chart);
        barChart.setMinimumHeight(dpToPx(ATTENDANCE_BAR_CHART_HEIGHT));
        barChart.setMinimumWidth(dpToPx(ATTENDANCE_BAR_CHART_HEIGHT));
        barChart = hideEverythingInBarChart(barChart);
        barChart.getAxisLeft().setAxisMaximum(ATTENDANCE_BAR_CHART_AXIS_MAXIMUM);
        barChart.getAxisLeft().setAxisMinimum(ATTENDANCE_BAR_CHART_AXIS_MINIMUM);
        barChart.setTouchEnabled(false);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootContainer =
                inflater.inflate(R.layout.fragment_clazz_log_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_log_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Record attendance FAB
        fab = rootContainer.findViewById(R.id.fragment_class_log_record_attendance_fab);

        //Separated out Chart initialisation
        setUpCharts();

        //Create the presenter and call its onCreate
        mPresenter = new ClazzLogListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        lastWeekButton = rootContainer.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_thisweek);
        lastMonthButton = rootContainer.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_thismonth);
        lastYearButton =rootContainer.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_lastyear);

        lastWeekButton.setOnClickListener(
                v -> {
                    mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_WEEK);
                    getTintedDrawable(lastWeekButton.getBackground(), R.color.primary);
                    //lastWeekButton.getBackground().setTint(getResources().getColor(R.color.primary));
                });
        lastMonthButton.setOnClickListener(
                v -> {
                    mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_MONTH);
                    getTintedDrawable(lastMonthButton.getBackground(), R.color.primary);
                    //lastMonthButton.getBackground().setTint(getResources().getColor(R.color.primary));
                });
        lastYearButton.setOnClickListener(
                v ->{
                    mPresenter.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_YEAR);
                    getTintedDrawable(lastYearButton.getBackground(), R.color.primary);
                    //lastYearButton.getBackground().setTint(getResources().getColor(R.color.primary));
                });

        //Default start to Last Week's data:
        lastWeekButton.callOnClick();

        //Take attendance fab click listener
        fab.setOnClickListener(v -> mPresenter.goToNewClazzLogDetailActivity());

        return rootContainer;
    }

    /**
     * Tints the drawable to the color. This method supports the Context compat tinting on drawables.
     *
     * @param drawable  The drawable to be tinted
     * @param color     The color of the tint
     */
    public void getTintedDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        int tintColor = ContextCompat.getColor(Objects.requireNonNull(getContext()), color);
        DrawableCompat.setTint(drawable, tintColor);
    }

    /**
     * Removes background color from View's report button
     */
    @Override
    public void resetReportButtons() {
        runOnUiThread(() -> {

            getTintedDrawable(lastWeekButton.getBackground(), R.color.color_gray);
            getTintedDrawable(lastMonthButton.getBackground(), R.color.color_gray);
            getTintedDrawable(lastYearButton.getBackground(), R.color.color_gray);

        });

    }

    @Override
    public void setFABVisibility(boolean visible) {
        if(visible){
            fab.setVisibility(View.VISIBLE);
        }else{
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showMessage(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String message = impl.getString(messageId, getContext());

        runOnUiThread(() -> Toast.makeText(
                getContext(),
                message,
                Toast.LENGTH_SHORT
        ).show());

    }

    // ClassLogList's DIFF callback
    public static final DiffUtil.ItemCallback<ClazzLogWithScheduleStartEndTimes> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ClazzLogWithScheduleStartEndTimes>(){

            @Override
            public boolean areItemsTheSame(ClazzLogWithScheduleStartEndTimes oldItem,
                                           ClazzLogWithScheduleStartEndTimes newItem) {
                return oldItem.getClazzLogUid() == newItem.getClazzLogUid();
            }

            @Override
            public boolean areContentsTheSame(ClazzLogWithScheduleStartEndTimes oldItem,
                                              ClazzLogWithScheduleStartEndTimes newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setClazzLogListProvider(UmProvider<ClazzLogWithScheduleStartEndTimes> clazzLogListProvider) {

        //Create a recycler adapter to set on the Recycler View.
        ClazzLogListRecyclerAdapter recyclerAdapter =
            new ClazzLogListRecyclerAdapter(DIFF_CALLBACK, getContext(), this, mPresenter, false);

        //warning is expected
        DataSource.Factory<Integer, ClazzLogWithScheduleStartEndTimes> factory =
                (DataSource.Factory<Integer, ClazzLogWithScheduleStartEndTimes>) clazzLogListProvider.getProvider();

        LiveData<PagedList<ClazzLogWithScheduleStartEndTimes>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

}
