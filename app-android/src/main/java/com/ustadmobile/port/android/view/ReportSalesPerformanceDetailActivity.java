package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportSalesPerformanceDetailPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportSalesPerformanceDetailView;
import com.ustadmobile.lib.db.entities.ReportSalesPerformance;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ReportSalesPerformanceDetailActivity extends UstadBaseActivity
        implements ReportSalesPerformanceDetailView {

    private Toolbar toolbar;
    private ReportSalesPerformanceDetailPresenter mPresenter;
    private FloatingTextButton fab;
    Menu menu;
    private boolean fabVisibility=true;

    private TextView xLabel, yLabel;
    private LinearLayout chartLL;


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param thisMenu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu thisMenu) {
        menu = thisMenu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_report_detail, menu);

        menu.findItem(R.id.menu_report_detail_download).setVisible(true);
        menu.findItem(R.id.menu_report_detail_edit).setVisible(true);

        return true;
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.menu_report_detail_download) {
            mPresenter.handleClickDownloadReport();
            return true;
        } else if (i == R.id.menu_report_detail_edit) {
            mPresenter.handleClickEditReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_sales_performance_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_sales_performance_detail_toolbar);
        toolbar.setTitle(getText(R.string.sales_performance_report));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        xLabel = findViewById(R.id.activity_report_sales_perforance_detail_x_label);
        yLabel = findViewById(R.id.activity_report_sales_perforance_detail_y_label);
        chartLL = findViewById(R.id.activity_report_sales_perforance_detail_report_ll);

        xLabel.setVisibility(View.VISIBLE);
        yLabel.setVisibility(View.VISIBLE);

        //Call the Presenter
        mPresenter = new ReportSalesPerformanceDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        fab = findViewById(R.id.activity_report_sales_performance_detail_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickAddToDashboard());
        fab.setVisibility(fabVisibility?View.VISIBLE:View.INVISIBLE);
    }


    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void showDownloadButton(boolean show) {
        if(menu!=null){
            menu.getItem(R.id.menu_report_detail_download).setVisible(show);
        }
    }

    @Override
    public void showAddToDashboardButton(boolean show) {
        fabVisibility = show;
        runOnUiThread(() -> {
            if(fab!= null){
                fab.setVisibility(show?View.VISIBLE:View.INVISIBLE);
            }
        });
    }

    @Override
    public void setReportData(List<Object> dataSet) {

        chartLL.removeAllViews();
        BarChart barChart = createBarChart(dataSet);
        chartLL.addView(barChart);
    }

    @Override
    public void setReportType(int reportType) {
        runOnUiThread(() -> toolbar.setTitle(R.string.sales_performance_report));
    }


    public BarChart createBarChart(List<Object> dataSet){

        BarChart barChart = new BarChart(this);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        barChart.setLayoutParams(params);

        //barChart = setUpCharts(barChart);
        barChart = hideEverythingInBarChart(barChart);

        String[] yAxisValues;

        List<String> yAxisValueList = new ArrayList<>();

        //TODO: Have unlimited variations
        List<String> barColorsList = new ArrayList<>();
        barColorsList.add("#FF9800");
        barColorsList.add("#FF6D00");
        barColorsList.add("#FF5722");
        barColorsList.add("#918F8F");
        barColorsList.add("#666666");
        String[] barColors = barColorsList.toArray(new String[0]);


        List<String> allDateOccurences = new ArrayList<>();

        Map<Long, ArrayList<BarEntry>> locationToBarEntriesMap = new HashMap<>();

        Map<Long, String> locationUidToName = new HashMap<>();


        List<String> allDatesInOrder = new ArrayList<>();

        for(Object data:dataSet){
            ReportSalesPerformance entry = (ReportSalesPerformance) data;
            if(!allDateOccurences.contains(entry.getFirstDateOccurence())){
                allDateOccurences.add(entry.getFirstDateOccurence());
            }
        }


        for(int i=0; i<dataSet.size();i++){

            int index;
            //Get Report data:
            Object everyEntryObject = dataSet.get(i);
            ReportSalesPerformance entry = (ReportSalesPerformance) everyEntryObject;

            long locationUid = entry.getLocationUid();
            String locationName = entry.getLocationName();
            if(!locationUidToName.containsKey(locationUid)){
                locationUidToName.put(locationUid, locationName);
            }
            ArrayList<BarEntry> locationBarEntries;
            if(locationToBarEntriesMap.containsKey(locationUid)){
                locationBarEntries = locationToBarEntriesMap.get(locationUid);
            }else{
                locationBarEntries = new ArrayList<>();
                int j=0;
                for(String ignored :allDateOccurences){
                    j++;
                    //locationBarEntries.add(new BarEntry(j,0));
                }
            }

            //Sale amount
            long saleAmount = entry.getSaleAmount();
            //Get date of occurrence
            String saleOccurrence = entry.getFirstDateOccurence();

            if(!allDateOccurences.contains(saleOccurrence)){
                //Add it
                allDateOccurences.add(saleOccurrence);
                index = 1;
            }else{
                index = locationBarEntries.size()+1;
            }
            index = allDateOccurences.indexOf(saleOccurrence) + 1;
            //Add entry in this index
            locationBarEntries.add(new BarEntry(index, saleAmount));


            //END: update bar entries.
            locationToBarEntriesMap.put(locationUid, locationBarEntries);


        }

        //Get data for chart
        BarData data = new BarData();
        int colorPos = 0;
        for (Long barEntry : locationToBarEntriesMap.keySet()) {
            String locationName = locationUidToName.get(barEntry);

            //Get entries
            ArrayList<BarEntry> locationEntry = locationToBarEntriesMap.get(barEntry);

            //Create BarDataSet
            assert locationEntry != null;
            BarDataSet barDataSet = new BarDataSet(locationEntry, locationName);

            //Color the bar
            String barColor;
            if(barColors.length >= colorPos){
                barColor = barColors[colorPos];
            }else{
                barColor = barColors[1];
            }
            barDataSet.setColor(Color.parseColor(barColor));
            colorPos++;

            //Add to data :
            data.addDataSet(barDataSet);

        }



        //Get yAxis for chart of date occurrences
        for(String everyDateOccurrence:allDateOccurences){
            String prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                    UMCalendarUtil.convertYYYYMMddToLong(everyDateOccurrence), null);
            yAxisValueList.add(prettyDate);
        }

        yAxisValues = yAxisValueList.toArray(new String[0]);

        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(yAxisValues));
        barChart.getAxisLeft().setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);

        float barSpace = 0.02f;
        float groupSpace = 0.3f;
        int groupCount = yAxisValueList.size();

        data.setBarWidth(0.15f);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 +
                barChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);

        if(colorPos>1){
            barChart.groupBars(0, groupSpace, barSpace);
        }

        //Hide values on top of every bar
        barChart.getBarData().setDrawValues(false);

        return barChart;
    }

    private BarChart hideEverythingInBarChart(BarChart barChart){

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.getXAxis().setEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(true);

        //Left Values
        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisLeft().setDrawTopYLabelEntry(true);

        //Right Values:
        barChart.getAxisRight().setEnabled(false);

        //Legend:
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        //Label Description
        barChart.getDescription().setEnabled(false);

        barChart.setTouchEnabled(false);



        return barChart;
    }
}
