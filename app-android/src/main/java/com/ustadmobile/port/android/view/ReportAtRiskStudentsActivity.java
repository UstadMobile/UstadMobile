package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportAtRiskStudentsPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ReportAtRiskStudentsView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ReportAtRiskStudentsActivity extends UstadBaseActivity
        implements ReportAtRiskStudentsView, PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;
    private LinearLayout reportLinearLayout;
    private ReportAtRiskStudentsPresenter mPresenter;
    private RecyclerView mRecyclerView;

    /**
     * Data for export report. Used to construct the export report (has line by line information)
     */
    List<String[]> tableTextData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout:
        setContentView(R.layout.activity_report_at_risk_students);

        //Toolbar
        toolbar = findViewById(R.id.activity_report_at_risk_students_toolbar);
        toolbar.setTitle(R.string.at_risk_students);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.activity_report_at_risk_students_rv);

        //reportLinearLayout = findViewById(R.id.activity_report_at_risk_students_ll);

        tableTextData = new ArrayList<>();

        //Recycler View for Report
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Presenter
        mPresenter = new ReportAtRiskStudentsPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_report_at_risk_students_fab);
        fab.setOnClickListener(this::showPopup);
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
        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "report_at_risk_students_" +
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

    public List<View> generateViewForDataSet(List<PersonWithEnrollment> classDataSet){

        List<View> classRiskStudentViews = new ArrayList<>();

        for(PersonWithEnrollment everyStudent:classDataSet){
            LinearLayout hl = new LinearLayout(getApplicationContext());
            hl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            TextView studentNameTV = new TextView(getApplicationContext());
            studentNameTV.setTextSize(10);
            studentNameTV.setTextColor(Color.BLACK);
            studentNameTV.setText(everyStudent.getFirstNames() +
                    " " + everyStudent.getLastName() +
                    " (" + String.valueOf((float)everyStudent.getAttendancePercentage()*100)
                    + "% " + getText(R.string.attendance).toString() + ")" );

            classRiskStudentViews.add(studentNameTV);
        }



        return classRiskStudentViews;
    }

    @Override
    public void updateTables(LinkedHashMap<String, List<PersonWithEnrollment>> dataMaps) {

        tableTextData = new ArrayList<>();

        Iterator<String> iterator = dataMaps.keySet().iterator();
        while(iterator.hasNext()){
            String className = iterator.next();
            List<PersonWithEnrollment> classDataSet = dataMaps.get(className);

            if(!classDataSet.isEmpty()){
                //Add title to tableTextData
                String[] titleItems = new String[]{className};
                tableTextData.add(titleItems);

                int numRiskStudents = classDataSet.size();

                List<View> addThese = generateViewForDataSet(classDataSet);

                //Heading
                TextView heading = new TextView(getApplicationContext());
                heading.setTextColor(Color.BLACK);
                heading.setTypeface(null, Typeface.BOLD);
                heading.setText(className + "( " + numRiskStudents + " " +
                        getText(R.string.students_literal) + ")");

                LinearLayout everyClassLL = new LinearLayout(getApplicationContext());
                everyClassLL.setOrientation(LinearLayout.VERTICAL);

                runOnUiThread(() -> {

                    for(View everyStudentRow: addThese){
                        everyClassLL.addView(everyStudentRow);
                    }

                    reportLinearLayout.addView(heading);
                    reportLinearLayout.addView(everyClassLL);
                });
            }
        }

    }

    public static final DiffUtil.ItemCallback<PersonWithEnrollment> DIFF_CALLBACK2 =
        new DiffUtil.ItemCallback<PersonWithEnrollment>() {
            @Override
            public boolean areItemsTheSame(PersonWithEnrollment oldItem,
                                           PersonWithEnrollment newItem) {
                return oldItem.getPersonUid() == newItem.getPersonUid();
            }

            @Override
            public boolean areContentsTheSame(PersonWithEnrollment oldItem,
                                              PersonWithEnrollment newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setReportProvider(UmProvider<PersonWithEnrollment> provider) {
        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, getApplicationContext(),
                        this, mPresenter, true, false, true);

        recyclerAdapter.setShowAddStudent(false);
        recyclerAdapter.setShowAddTeacher(false);

        //A warning is expected.
        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)
                        provider.getProvider();

        LiveData<PagedList<PersonWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }
}
