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
import com.ustadmobile.core.view.ReportSalesPerformanceDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
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
    public void setReportData(Map<Object, Object> dataSet) {
        //TODO: Redo with correct data
        chartLL.removeAllViews();
        BarChart barChart = createBarChart();
        chartLL.addView(barChart);
    }

    @Override
    public void setReportType(int reportType) {
        runOnUiThread(() -> toolbar.setTitle(R.string.sales_performance_report));
    }


    private BarChart createBarChart(){

        BarChart barChart = new BarChart(this);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        barChart.setLayoutParams(params);

        //barChart = setUpCharts(barChart);
        barChart = hideEverythingInBarChart(barChart);

        ArrayList<BarEntry> heratEntries = new ArrayList<>();
        ArrayList<BarEntry> kabulEntries = new ArrayList<>();
        ArrayList<BarEntry> khostEntries = new ArrayList<>();
        ArrayList<BarEntry> kunduzEntries = new ArrayList<>();
        ArrayList<BarEntry> paktikaEntries = new ArrayList<>();

        heratEntries.add(new BarEntry(1,18000));
        heratEntries.add(new BarEntry(2,17000));
        heratEntries.add(new BarEntry(3,17000));
        heratEntries.add(new BarEntry(4,16000));


        kabulEntries.add(new BarEntry(1,95000));
        kabulEntries.add(new BarEntry(2,120000));
        kabulEntries.add(new BarEntry(3,130000));
        kabulEntries.add(new BarEntry(4,122000));


        khostEntries.add(new BarEntry(1,50500));
        khostEntries.add(new BarEntry(2,60000));
        khostEntries.add(new BarEntry(3,59000));
        khostEntries.add(new BarEntry(4,6000));


        kunduzEntries.add(new BarEntry(1,100000));
        kunduzEntries.add(new BarEntry(2,130000));
        kunduzEntries.add(new BarEntry(3,70000));
        kunduzEntries.add(new BarEntry(4,90000));


        paktikaEntries.add(new BarEntry(1,40000));
        paktikaEntries.add(new BarEntry(2,25000));
        paktikaEntries.add(new BarEntry(3,30000));
        paktikaEntries.add(new BarEntry(4,20000));


        BarDataSet barDataSet = new BarDataSet(heratEntries,"Herat");
        barDataSet.setColor(Color.parseColor("#FF9800"));
        BarDataSet barDataSet1 = new BarDataSet(kabulEntries,"Kabul");
        barDataSet1.setColors(Color.parseColor("#FF6D00"));
        BarDataSet barDataSet2 = new BarDataSet(khostEntries,"Khost");
        barDataSet2.setColors(Color.parseColor("#FF5722"));
        BarDataSet barDataSet3 = new BarDataSet(kunduzEntries,"Kunduz");
        barDataSet3.setColors(Color.parseColor("#918F8F"));
        BarDataSet barDataSet4 = new BarDataSet(paktikaEntries,"Paktika");
        barDataSet4.setColors(Color.parseColor("#666666"));

        String[] months = new String[] {"5-May", "12-May", "19-May", "26-May"};
        BarData data = new BarData(barDataSet,barDataSet1,barDataSet2,barDataSet3, barDataSet4);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        barChart.getAxisLeft().setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);

        float barSpace = 0.02f;
        float groupSpace = 0.3f;
        int groupCount = 4;

        //IMPORTANT *****
        data.setBarWidth(0.15f);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        barChart.groupBars(0, groupSpace, barSpace); // perform the "explicit" grouping
        //***** IMPORTANT


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
