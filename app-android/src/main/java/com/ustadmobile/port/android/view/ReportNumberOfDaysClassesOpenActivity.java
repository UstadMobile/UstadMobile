package com.ustadmobile.port.android.view;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportNumberOfDaysClassesOpenPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ReportNumberOfDaysClassesOpen activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportNumberOfDaysClassesOpenView
 */
public class ReportNumberOfDaysClassesOpenActivity extends UstadBaseActivity
        implements ReportNumberOfDaysClassesOpenView, PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;

    private ReportNumberOfDaysClassesOpenPresenter mPresenter;
    BarChart barChart;
    TableLayout tableLayout;

    //Used for exporting
    List<String[]> tableTextData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_number_of_days_classes_open);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_number_of_days_classes_open_toolbar);
        toolbar.setTitle(R.string.number_of_days_classes_open);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.number_of_days_classes_open);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tableLayout = findViewById(R.id.activity_report_number_of_days_classes_open_table);

        //Call the Presenter
        mPresenter = new ReportNumberOfDaysClassesOpenPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        //eg:
        FloatingTextButton fab = findViewById(R.id.activity_report_number_of_days_classes_open_fab);
        fab.setOnClickListener(v -> showPopup(v));


    }

    public void setUpChart(){

        Locale currentLocale = getResources().getConfiguration().locale;

        toolbar.setTitle(R.string.number_of_days_classes_open);

        barChart = findViewById(R.id.activity_report_number_of_days_classes_open_bar_chart);

        barChart.setMinimumHeight(dpToPx(BAR_CHART_HEIGHT));

        Description barChartDes = new Description();
        barChartDes.setText("");
        barChart.setDescription(barChartDes);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);

        barChart.getXAxis().setValueFormatter((value, axis) -> {
            String prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                    mPresenter.barChartTimestamps.get((int) value), currentLocale
            );
            return prettyDate;
        });

        barChart.setTouchEnabled(false);
    }


    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_export, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_export_csv) {
            mPresenter.dataToCSV();
            return true;
        }
        return false;
    }

    @Override
    public void updateBarChart(LinkedHashMap<Float, Float> dataMap) {

        Boolean hasSomething = false;
        mPresenter.barChartTimestamps = new ArrayList<>();
        int index = 0;
        //RENDER HERE

        List<BarEntry> barDataEntries = new ArrayList<>();
        for (Map.Entry<Float, Float> nextEntry : dataMap.entrySet()) {
            hasSomething = true;
            mPresenter.barChartTimestamps.add((long) (nextEntry.getKey() * 1000));
            BarEntry anEntry = new BarEntry(index,
                    nextEntry.getValue());
            barDataEntries.add(anEntry);
            index ++;
        }

        //Create Bar color
        BarDataSet dataSetBar1 = new BarDataSet(barDataEntries, BAR_LABEL);
        dataSetBar1.setValueTextColor(Color.BLACK);
        dataSetBar1.setDrawValues(true);
        dataSetBar1.setColor(Color.parseColor(BAR_CHART_BAR_COLOR));

        BarData barData = new BarData(dataSetBar1);

        List<View> addThese = generateAllViewRowsForTable(dataMap);

        Boolean finalHasSomething = hasSomething;
        runOnUiThread(() -> {
            setUpChart();
            if(finalHasSomething){
                barChart.setData(barData);
                barChart.invalidate();
            }else{
                barChart.setData(null);
                barChart.invalidate();
            }

            for(View everyRow: addThese){
                tableLayout.addView(everyRow);
            }

        });
    }

    @Override
    public void generateCSVReport() {
        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "number_of_days_classes_open_report_" +
                System.currentTimeMillis() + ".csv");
        csvReportFilePath = output.getAbsolutePath();

        try {
            FileWriter fileWriter = new FileWriter(csvReportFilePath);
            Iterator<String[]> tableTextdataIterator = tableTextData.iterator();

            while(tableTextdataIterator.hasNext()){
                boolean firstDone = false;
                String[] lineArray = tableTextdataIterator.next();
                for(int i=0;i<lineArray.length;i++){
                    if(firstDone){
                        fileWriter.append(",");
                    }
                    firstDone = true;
                    fileWriter.append(lineArray[i]);
                }
                fileWriter.append("\n");
            }
            fileWriter.close();



        } catch (IOException e) {
            e.printStackTrace();
        }

        String applicationId = getPackageName();
        Uri sharedUri = FileProvider.getUriForFile(this,
                applicationId+".fileprovider",
                new File(csvReportFilePath));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
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

    public List<View> generateAllViewRowsForTable(LinkedHashMap<Float, Float> dataTableMaps ){

        Locale currentLocale = getResources().getConfiguration().locale;

        List<View> addThese = new ArrayList<>();

        //Build a string array of the data
        tableTextData = new ArrayList<>();

        //LAYOUT
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));


        //HEADING
        TableRow headingRow = new TableRow(getApplicationContext());
        headingRow.setLayoutParams(rowParams);

        TextView dateHeading = new TextView(getApplicationContext());
        dateHeading.setTextColor(Color.BLACK);
        dateHeading.setLayoutParams(everyItemParam);
        dateHeading.setText(R.string.date);

        TextView valueHeading = new TextView(getApplicationContext());
        valueHeading.setTextColor(Color.BLACK);
        valueHeading.setLayoutParams(everyItemParam);
        valueHeading.setText(R.string.number_of_classes);

        headingRow.addView(dateHeading);
        headingRow.addView(valueHeading);

        //ADD HEADING
        addThese.add(headingRow);

        //MAKE TABLE TEXT DATA:
        String[] headingItems = new String[headingRow.getChildCount()];
        for(int i = 0; i < headingRow.getChildCount(); i++){
            headingItems[i] = ((TextView) headingRow.getChildAt(i)).getText().toString();
        }
        tableTextData.add(headingItems);


        List<Float> dates = new ArrayList<>();
        dates.addAll(dataTableMaps.keySet());

        for(float everyDate: dates){
            String everyDateString =
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                            (long)everyDate * 1000, currentLocale);

            TableRow everyDateRow = new TableRow(getApplicationContext());
            everyDateRow.setLayoutParams(rowParams);

            TextView dateView = new TextView(getApplicationContext());
            dateView.setTextColor(Color.BLACK);
            dateView.setLayoutParams(everyItemParam);
            dateView.setText(everyDateString);

            TextView valueView = new TextView(getApplicationContext());
            valueView.setTextColor(Color.BLACK);
            valueView.setLayoutParams(everyItemParam);
            valueView.setText(String.valueOf(Math.round(dataTableMaps.get(everyDate))));

            everyDateRow.addView(dateView);
            everyDateRow.addView(valueView);

            addThese.add(everyDateRow);

            //BUILD TABLE TEXT DATA
            String[] rowItems = new String[everyDateRow.getChildCount()];
            for(int i = 0; i < everyDateRow.getChildCount(); i++){
                rowItems[i] = ((TextView)everyDateRow.getChildAt(i)).getText().toString();
            }
            tableTextData.add(rowItems);
        }

        return addThese;

    }

}
