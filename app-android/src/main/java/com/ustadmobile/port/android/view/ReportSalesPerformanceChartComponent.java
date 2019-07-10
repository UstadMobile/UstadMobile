package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportBarChartComponentView;
import com.ustadmobile.lib.db.entities.ReportSalesPerformance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportSalesPerformanceChartComponent extends LinearLayout implements
        ReportBarChartComponentView {

    BarChart barChart;
    Context mContext;

    public ReportSalesPerformanceChartComponent(Context context) {
        super(context);
        mContext = context;
    }

    public ReportSalesPerformanceChartComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReportSalesPerformanceChartComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChartData(List<Object> dataSet){
        runOnUiThread(() -> {
            removeAllViews();
            barChart = createSalesBarChart(dataSet);
            addView(barChart);
        });

    }

    private BarChart createSalesBarChart(List<Object> dataSet){

        BarChart barChart = new BarChart(getContext());
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


        //Map of Location Uid (Group 1) and BarEntry list
        Map<Long, ArrayList<BarEntry>> locationToBarEntriesMap = new HashMap<>();
        List<String> allDateOccurences = new ArrayList<>();

        //Map of LocationUid -> Name for Label searching
        Map<Long, String> locationUidToName = new HashMap<>();

        //Get all date occurrences from the data. (This will be x axis - 2nd group)
        for(Object data:dataSet){
            ReportSalesPerformance entry = (ReportSalesPerformance) data;
            if(!allDateOccurences.contains(entry.getFirstDateOccurence())){
                allDateOccurences.add(entry.getFirstDateOccurence());
            }
        }

        //Build every location's value plotting data.
        //Loop over every data output.
        for(int i=0; i<dataSet.size();i++){

            //Get Report data:
            ReportSalesPerformance entry = (ReportSalesPerformance) dataSet.get(i);
            //Sale amount
            long saleAmount = entry.getSaleAmount();
            //Get date of occurrence
            String saleOccurrence = entry.getFirstDateOccurence();
            //Get location
            long locationUid = entry.getLocationUid();

            //Build the Location Uid, Location Name for future lookup.
            String locationName = entry.getLocationName();
            if(!locationUidToName.containsKey(locationUid)){
                locationUidToName.put(locationUid, locationName);
            }

            //Get the Group 1's Bar Entry list. Its either a new one or one already made.
            ArrayList<BarEntry> locationBarEntries;
            if(locationToBarEntriesMap.containsKey(locationUid)){
                locationBarEntries = locationToBarEntriesMap.get(locationUid);
            }else{
                locationBarEntries = new ArrayList<>();
            }

            if(!allDateOccurences.contains(saleOccurrence)){
                //Add it
                allDateOccurences.add(saleOccurrence);
            }

            //Get index of where in the date occurrence this data belongs
            int index = allDateOccurences.indexOf(saleOccurrence) + 1;

            //Add entry in this index
            locationBarEntries.add(new BarEntry(index, saleAmount));

            //END: update bar entries.
            locationToBarEntriesMap.put(locationUid, locationBarEntries);

        }

        //Buld the bar Data set for every Location
        BarData data = new BarData();
        int colorPos = 0;
        for (Long barEntry : locationToBarEntriesMap.keySet()) {
            //Get location name
            String locationName = locationUidToName.get(barEntry);

            //Get entries (values plotted)
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

    @Override
    public void runOnUiThread(Runnable r) {
        ((Activity)mContext).runOnUiThread(r);
    }
}
