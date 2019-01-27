package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportSELPresenter;
import com.ustadmobile.core.view.ReportSELView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ReportSELActivity extends UstadBaseActivity implements
        ReportSELView, PopupMenu.OnMenuItemClickListener{

    private Toolbar toolbar;
    private LinearLayout reportLinearLayout;
    private ReportSELPresenter mPresenter;

    //For export line by line data.
    List<String[]> tableTextData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_report_sel);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_sel_toolbar);
        toolbar.setTitle(R.string.sel_report);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        reportLinearLayout =
                findViewById(R.id.activity_report_sel_ll);

        //Call the Presenter
        mPresenter = new ReportSELPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        //eg:
        FloatingTextButton fab =
                findViewById(R.id.activity_report_sel_fab);
        fab.setOnClickListener(v -> showPopup(v));
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
            generateCSVReport();
            return true;

        }
        //TODO: Sprint 5
//        else if (i == R.id.menu_export_xls) {
//            mPresenter.dataToXLS();
//            return true;
//        } else if (i == R.id.menu_export_json) {
//            mPresenter.dataToJSON();
//            return true;
//        }
        else {
            return false;
        }
    }

    @Override
    public void generateCSVReport() {

        //Build a string array of the data
        tableTextData = new ArrayList<>();


    }

    @Override
    public void generateXLSReport() {

    }

    @Override
    public void updateTables(List items) {

        //TODO:
    }
}
