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
import android.widget.TableRow;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportTopLEsDetailPresenter;
import com.ustadmobile.core.view.ReportTopLEsDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Map;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ReportTopLEsDetailActivity extends UstadBaseActivity
        implements ReportTopLEsDetailView {

    private Toolbar toolbar;
    private ReportTopLEsDetailPresenter mPresenter;
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

        xLabel.setVisibility(View.INVISIBLE);
        yLabel.setVisibility(View.INVISIBLE);

        //Call the Presenter
        mPresenter = new ReportTopLEsDetailPresenter(this,
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
        LinearLayout topLEs = createTopLEs();
        chartLL.addView(topLEs);
    }

    @Override
    public void setReportType(int reportType) {
        runOnUiThread(() -> toolbar.setTitle(R.string.sales_performance_report));
    }


    private LinearLayout createTopLEs(){
        LinearLayout topLL = new LinearLayout(this);
        topLL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        topLL.setLayoutParams(params);

        String[] names = new String[]{"Roya Rahimi","Laila Gulzar","Meena Hotaki", "Nargis Yousafzai"};
        String[] values = new String[]{"81,756 Afs","70,865 Afs","51,162 Afs", "48,900 Afs"};
        for(int i=0; i<4;i++){

            TextView t1 = new TextView(this);
            t1.setText(names[i]);
            t1.setPadding(0,8,0,0);
            topLL.addView(t1);
            TextView v1 = new TextView(this);
            v1.setTextSize(18);
            v1.setTextColor(Color.parseColor("#F57C00"));
            v1.setText(values[i]);
            topLL.addView(v1);
            v1.setPadding(0,0,0,8);
            topLL.addView(getHorizontalLine());
        }

        return topLL;

    }

    /**
     * Creates a new Horizontal line for a table's row.
     * @return  The horizontal line view.
     */
    public View getHorizontalLine(){
        //Horizontal line
        ViewGroup.LayoutParams hlineParams = new ViewGroup.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1);
        View hl = new View(this);
        hl.setBackgroundColor(Color.GRAY);
        hl.setLayoutParams(hlineParams);
        return hl;
    }

}
