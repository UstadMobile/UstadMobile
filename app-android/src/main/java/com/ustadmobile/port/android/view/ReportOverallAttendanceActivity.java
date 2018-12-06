package com.ustadmobile.port.android.view;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ustadmobile.core.controller.ReportOverallAttendancePresenter;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.toughra.ustadmobile.R;


import com.ustadmobile.core.view.ReportOverallAttendanceView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ReportOverallAttendance activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportOverallAttendanceView
 */
public class ReportOverallAttendanceActivity extends UstadBaseActivity
        implements ReportOverallAttendanceView, PopupMenu.OnMenuItemClickListener  {

    private Toolbar toolbar;

    //RecyclerView
    private ReportOverallAttendancePresenter mPresenter;
    LineChart lineChart;
    TableLayout dataTable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_overall_attendance);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_overall_attendance_toolbar);
        toolbar.setTitle(R.string.overall_attendance_report);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataTable = findViewById(R.id.activity_report_overall_attendance_table);

        //Call the Presenter
        mPresenter = new ReportOverallAttendancePresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        //eg:
        FloatingTextButton fab = findViewById(R.id.activity_report_overall_attendance_fab);
        fab.setOnClickListener(v -> showPopup(v));


    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_export, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_export_csv) {
            mPresenter.dataToCSV();
            return true;
        } else if (i == R.id.menu_export_xls) {
            mPresenter.dataToXLS();
            return true;
        } else if (i == R.id.menu_export_json) {
            mPresenter.dataToJSON();
            return true;
        } else {
            return false;
        }
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
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
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
     * Converts dp to pixels (used in MPAndroid charts)
     *
     * @param dp    dp number
     * @return      pixels number
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void setUpCharts() {
        //Get the chart view
        lineChart = findViewById(R.id.activity_report_overall_attendance_line_chart);
        lineChart.setMinimumHeight(dpToPx(ATTENDANCE_LINE_CHART_HEIGHT));
        //lineChart = hideEverythingInLineChart(lineChart);
        lineChart.getAxisLeft().setValueFormatter((value, axis) -> (int) value + "%");
        lineChart.getXAxis().setValueFormatter((value, axis) -> (int) value + "");
        lineChart.setTouchEnabled(false);
        lineChart.getXAxis().setLabelCount(4, true);
    }

    @Override
    public void updateAttendanceMultiLineChart(LinkedHashMap<String,
            LinkedHashMap<Float, Float>> dataMaps) {


        LineData lineData = new LineData();
        Boolean hasSomething = false;
        Boolean headingCreated = false;

        View hline = new View(this);
        hline.setLayoutParams(new LinearLayout.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                2
        ));
        hline.setBackgroundColor(Color.parseColor("#DEDEDE"));

        //Update the table
        if(!headingCreated){
            TableRow headingRow = new TableRow(this);

            TextView dateHeading = new TextView(this);
            dateHeading.setText("Date");


            TextView averageHeading = new TextView(this);
            averageHeading.setText("Average");


            headingRow.addView(dateHeading);
            headingRow.addView(averageHeading);


            if(mPresenter.isGenderDisaggregate()){

                TextView maleHeading = new TextView(this);
                maleHeading.setText("Male");

                TextView femaleHeading = new TextView(this);
                femaleHeading.setText("Female");

                headingRow.addView(maleHeading);
                headingRow.addView(femaleHeading);
            }

            headingCreated = true;
            dataTable.addView(headingRow);
        }

        for(Map.Entry<String, LinkedHashMap<Float, Float>> everyLineDataMap : dataMaps.entrySet()){
            LinkedHashMap<Float, Float> dataMap = everyLineDataMap.getValue();
            String dataSetType = everyLineDataMap.getKey();


            String labelDesc, labelColor;
            switch (dataSetType){
                case ATTENDANCE_LINE_MALE_LABEL_DESC:
                    labelDesc = ATTENDANCE_LINE_MALE_LABEL_DESC;
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_MALE;
                    break;
                case ATTENDANCE_LINE_FEMALE_LABEL_DESC:
                    labelDesc = ATTENDANCE_LINE_FEMALE_LABEL_DESC;
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_FEMALE;
                    break;
                case ATTENDANCE_LINE_AVERAGE_LABEL_DESC:
                    labelDesc = ATTENDANCE_LINE_AVERAGE_LABEL_DESC;
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_AVERAGE;
                    break;
                default:
                    labelDesc = "-";
                    labelColor = "#000000";

            }
            List<Entry> lineDataEntries = new ArrayList<>();
            for (Map.Entry<Float, Float> floatFloatEntry : dataMap.entrySet()) {
                hasSomething = true;
                Entry anEntry = new Entry();
                anEntry.setX(floatFloatEntry.getKey());
                anEntry.setY(floatFloatEntry.getValue() * 100);
                lineDataEntries.add(anEntry);
            }

            //Create a line data set (one line)
            LineDataSet dataSetLine1 = new LineDataSet(lineDataEntries, labelDesc);
            dataSetLine1.setColor(Color.parseColor(labelColor));
            dataSetLine1.setValueTextColor(Color.BLACK);
            //Don't want to see the values on the data points.
            dataSetLine1.setDrawValues(false);
            //Don't want to see the circles
            dataSetLine1.setDrawCircles(false);

            lineData.addDataSet(dataSetLine1);




        }

        //Update the lineChart on the UI thread (since this method is called via the Presenter)
        Boolean finalHasSomething = hasSomething;
        runOnUiThread(() -> {
            setUpCharts();
            if(finalHasSomething){
                lineChart.setData(lineData);
                lineChart.invalidate();
            }else{
                lineChart.setData(null);
                lineChart.invalidate();
            }

        });


    }
}
