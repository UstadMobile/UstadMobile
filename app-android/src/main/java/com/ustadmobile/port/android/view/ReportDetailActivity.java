package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportChartViewComponentPresenter;
import com.ustadmobile.core.controller.ReportDetailPresenter;
import com.ustadmobile.core.controller.ReportSalesLogComponentPresenter;
import com.ustadmobile.core.controller.ReportTopLEsComponentPresenter;
import com.ustadmobile.core.view.ReportDetailView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;

;

public class ReportDetailActivity extends UstadBaseActivity implements ReportDetailView {

    private Toolbar toolbar;
    private ReportDetailPresenter mPresenter;
    private FloatingTextButton fab;
    Menu menu;
    private boolean fabVisibility=true;

    private TextView xLabel, yLabel;
    private LinearLayout chartLL;

    private String reportOptionsString;

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
        setContentView(R.layout.activity_report_detail);

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

        if(getIntent().getExtras().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionsString = getIntent().getExtras().get(ARG_REPORT_OPTIONS).toString();
        }else{
            reportOptionsString = "";
        }

        //Call the Presenter
        mPresenter = new ReportDetailPresenter(this,
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
    public void showSalesPerformanceReport(){
        //Common Layout params for chart views to match parent.
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        //Creating the args to be sent to the Chart/Report view presenter
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_OPTIONS, reportOptionsString);

        //Clear before adding.
        chartLL.removeAllViews();
        xLabel.setVisibility(View.VISIBLE);
        yLabel.setVisibility(View.VISIBLE);

        //Create the chart component, sets its layout and call the presenter on the view.
        ReportSalesPerformanceChartComponent chartComponent =
                new ReportSalesPerformanceChartComponent(this);
        chartComponent.setLayoutParams(params);
        ReportChartViewComponentPresenter cPresenter =
                new ReportChartViewComponentPresenter(this,args, chartComponent);
        cPresenter.onCreate(args);

        chartLL.addView(chartComponent);

    }

    @Override
    public void showSalesLogReport() {

        //Common Layout params for chart views to match parent.
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        //Creating the args to be sent to the Chart/Report view presenter
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_OPTIONS, reportOptionsString);

        //Clear before adding.
        chartLL.removeAllViews();

        xLabel.setVisibility(View.INVISIBLE);
        yLabel.setVisibility(View.INVISIBLE);

        //Create View, presenter and add it to the view
        ReportTableListComponent salesLogComponent =
                new ReportTableListComponent(this);
        salesLogComponent.setLayoutParams(params);
        ReportSalesLogComponentPresenter lPresenter =
                new ReportSalesLogComponentPresenter(this, args, salesLogComponent);
        lPresenter.onCreate(args);

        chartLL.addView(salesLogComponent);

    }

    @Override
    public void showTopLEsReport() {

        //Common Layout params for chart views to match parent.
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        //Creating the args to be sent to the Chart/Report view presenter
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_OPTIONS, reportOptionsString);

        //Clear before adding.
        chartLL.removeAllViews();

        xLabel.setVisibility(View.INVISIBLE);
        yLabel.setVisibility(View.INVISIBLE);

        //Create View, presenter and add it to the view
        ReportTableListComponent topLEsComponent =
                new ReportTableListComponent(this);
        topLEsComponent.setLayoutParams(params);
        ReportTopLEsComponentPresenter tPresenter =
                new ReportTopLEsComponentPresenter(this, args, topLEsComponent);
        tPresenter.onCreate(args);

        chartLL.addView(topLEsComponent);

    }

    @Override
    public void setReportType(int reportType) {
        String title = "";
        switch (reportType){
            case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:
                title = getText(R.string.sales_performance_report).toString();
                break;
            case DashboardEntry.REPORT_TYPE_SALES_LOG:
                title = getText(R.string.sales_log_report).toString();
                break;
            case DashboardEntry.REPORT_TYPE_TOP_LES:
                title = getText(R.string.top_les_report).toString();
                break;

        }
        toolbar.setTitle(title);
    }


}
