package com.ustadmobile.port.android.view;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao.AttendanceResultGroupedByAgeAndThreshold;
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ReportNumberOfDaysClassesOpen activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportNumberOfDaysClassesOpenView
 */
public class ReportAttendanceGroupedByThresholdsActivity extends UstadBaseActivity
        implements ReportAttendanceGroupedByThresholdsView, PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;
    private LinearLayout reportLinearLayout;
    private ReportAttendanceGroupedByThresholdsPresenter mPresenter;

    /**
     * Used to construct the export report (has line by line information)
     */
    List<String[]> tableTextData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_attendance_grouped_by_thresholds);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_attendance_grouped_by_thresholds_toolbar);
        toolbar.setTitle(R.string.attendance_grouped_by_threshold);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        reportLinearLayout = findViewById(R.id.actvity_report_attendance_grouped_by_thresholds_ll);

        //Call the Presenter
        mPresenter = new ReportAttendanceGroupedByThresholdsPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        //eg:
        FloatingTextButton fab = findViewById(R.id.activity_report_attendance_grouped_by_thresholds_fab);
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
            mPresenter.dataToCSV();
            return true;
        }
        if(i== R.id.menu_export_xls){
            startXLSXReportGeneration();
            return true;
        }

        else {
            return false;
        }
    }

    @Override
    public void generateXLSXReport(String xlsxReportPath) {
        String applicationId = getPackageName();
        Uri sharedUri = FileProvider.getUriForFile(this,
                applicationId+".fileprovider",
                new File(xlsxReportPath));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    /**
     * Starts the xlsx report process. Here it crates hte xlsx file.
     */
    private void startXLSXReportGeneration(){

        File dir = getFilesDir();
        String xlsxReportPath;

        String title = "report_attendance_grouped_by_threshold_" + System.currentTimeMillis();

        File output = new File(dir, title + ".xlsx");
        xlsxReportPath = output.getAbsolutePath();

        File testDir = new File(dir, title);
        testDir.mkdir();
        String workingDir = testDir.getAbsolutePath();

        mPresenter.dataToXLSX(title, xlsxReportPath, workingDir, tableTextData);

    }


    @Override
    public void updateTables(LinkedHashMap<String,
            List<AttendanceResultGroupedByAgeAndThreshold>> dataMaps) {

        //Build a string array of the data
        tableTextData = new ArrayList<>();

        for (String locationName : dataMaps.keySet()) {
            List<AttendanceResultGroupedByAgeAndThreshold> dataMapList = dataMaps.get(locationName);
            assert dataMapList != null;
            if (!dataMapList.isEmpty()) {

                //Add title to tableTextData
                String[] titleItems = new String[]{locationName};
                tableTextData.add(titleItems);

                List<View> addThese = generateAllViewRowsForTable(dataMapList);

                //heading
                TextView heading = new TextView(getApplicationContext());
                heading.setTextColor(Color.BLACK);
                heading.setTypeface(null, Typeface.BOLD);
                heading.setText(locationName);

                TableLayout tableLayout = new TableLayout(getApplicationContext());

                runOnUiThread(() -> {

                    for (View everyRow : addThese) {
                        tableLayout.addView(everyRow);

                    }

                    reportLinearLayout.addView(heading);
                    reportLinearLayout.addView(tableLayout);

                });

            }
        }

    }

    @Override
    public void generateCSVReport() {

        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "report_attendance_grouped_by_threshold_" +
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

    public List<View> generateAllViewRowsForTable(List<AttendanceResultGroupedByAgeAndThreshold> dataMapList ){

        List<View> addThese = new ArrayList<>();

        //LAYOUT
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        everyItemParam.gravity = Gravity.CENTER_VERTICAL;


        //HEADING
        TableRow superHeadingRow = new TableRow(getApplicationContext());
        superHeadingRow.setLayoutParams(rowParams);

        superHeadingRow.addView(new TextView(getApplicationContext()));

        TextView attendanceLowHeading = new TextView(getApplicationContext());
        TableRow.LayoutParams params = (TableRow.LayoutParams)superHeadingRow.getLayoutParams();
        params.span = 2;
        attendanceLowHeading.setLayoutParams(params);
        attendanceLowHeading.setTextSize(10);
        attendanceLowHeading.setTextColor(Color.BLACK);
        attendanceLowHeading.setText(getText(R.string.attendance) + " >" + mPresenter.getThresholdValues().low + "%");


        TextView attendanceMidHeading = new TextView(getApplicationContext());
         params = (TableRow.LayoutParams)superHeadingRow.getLayoutParams();
        params.span = 2;
        attendanceMidHeading.setLayoutParams(params);
        attendanceMidHeading.setTextSize(10);
        attendanceMidHeading.setTextColor(Color.BLACK);
        attendanceMidHeading.setText(getText(R.string.attendance) + " >" + mPresenter.getThresholdValues().med + "%");

        TextView attendanceHighHeading = new TextView(getApplicationContext());
        params = (TableRow.LayoutParams)superHeadingRow.getLayoutParams();
        params.span = 2;
        attendanceHighHeading.setLayoutParams(params);
        attendanceHighHeading.setTextSize(10);
        attendanceHighHeading.setTextColor(Color.BLACK);
        attendanceHighHeading.setText(getText(R.string.attendance) + " >" + mPresenter.getThresholdValues().high + "%");


        superHeadingRow.addView(attendanceLowHeading);
        superHeadingRow.addView(attendanceMidHeading);
        superHeadingRow.addView(attendanceHighHeading);

        addThese.add(superHeadingRow);

        //Super heading row to tableTextData
        String[] superHeadingItems = new String[superHeadingRow.getChildCount() + 3];
        int j = 0;
        for(int i = 0; i < superHeadingRow.getChildCount(); i++){
            String addThis = ((TextView) superHeadingRow.getChildAt(i)).getText().toString();
            superHeadingItems[j] = addThis;

            if(addThis.startsWith(getText(R.string.attendance).toString())){
                j++;
                superHeadingItems[j] = "";
            }
            j++;
        }
        tableTextData.add(superHeadingItems);

        TableRow headingRow = new TableRow(getApplicationContext());
        headingRow.setLayoutParams(rowParams);

        TextView ageHeading = new TextView(getApplicationContext());
        ageHeading.setTextColor(Color.BLACK);
        ageHeading.setLayoutParams(everyItemParam);
        ageHeading.setText(R.string.age);

        TextView maleLowHeading = new TextView(getApplicationContext());
        maleLowHeading.setTextColor(Color.BLACK);
        maleLowHeading.setLayoutParams(everyItemParam);
        maleLowHeading.setText(R.string.male);

        TextView femaleLowHeading = new TextView(getApplicationContext());
        femaleLowHeading.setTextColor(Color.BLACK);
        femaleLowHeading.setLayoutParams(everyItemParam);
        femaleLowHeading.setText(R.string.female);

        TextView maleMidHeading = new TextView(getApplicationContext());
        maleMidHeading.setTextColor(Color.BLACK);
        maleMidHeading.setLayoutParams(everyItemParam);
        maleMidHeading.setText(R.string.male);

        TextView femaleMidHeading = new TextView(getApplicationContext());
        femaleMidHeading.setTextColor(Color.BLACK);
        femaleMidHeading.setLayoutParams(everyItemParam);
        femaleMidHeading.setText(R.string.female);

        TextView maleHighHeading = new TextView(getApplicationContext());
        maleHighHeading.setTextColor(Color.BLACK);
        maleHighHeading.setLayoutParams(everyItemParam);
        maleHighHeading.setText(R.string.male);

        TextView femaleHighHeading = new TextView(getApplicationContext());
        femaleHighHeading.setTextColor(Color.BLACK);
        femaleHighHeading.setLayoutParams(everyItemParam);
        femaleHighHeading.setText(R.string.female);


        if(!mPresenter.isGenderDisaggregate()){
            maleLowHeading.setText(R.string.average);
            maleMidHeading.setText(R.string.average);
            maleHighHeading.setText(R.string.average);

            femaleLowHeading.setText("");
            femaleMidHeading.setText("");
            femaleHighHeading.setText("");
        }

        //Add all individual headings to the heading row.
        headingRow.addView(ageHeading);
        headingRow.addView(maleLowHeading);
        headingRow.addView(femaleLowHeading);
        headingRow.addView(maleMidHeading);
        headingRow.addView(femaleMidHeading);
        headingRow.addView(maleHighHeading);
        headingRow.addView(femaleHighHeading);

        //ADD HEADING ROW to the View to return
        addThese.add(headingRow);

        //heading row to tableTextData
        String[] headingItems = new String[headingRow.getChildCount()];
        for(int i = 0; i < headingRow.getChildCount(); i++){
            headingItems[i] = ((TextView) headingRow.getChildAt(i)).getText().toString();
        }
        tableTextData.add(headingItems);

        //Horizontal line
        View v = new View(this);
        TableRow.LayoutParams hlineParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 2);
        v.setBackgroundColor(Color.GRAY);
        v.setLayoutParams(hlineParams);
        addThese.add(v);



        List<Integer> ages = new ArrayList<>();

        HashMap<Integer, Integer>  maleLowMap = new HashMap<>();
        HashMap<Integer, Integer>  femaleLowMap = new HashMap<>();
        HashMap<Integer, Integer>  maleMidMap = new HashMap<>();
        HashMap<Integer, Integer>  femaleMidMap = new HashMap<>();
        HashMap<Integer, Integer>  maleHighMap = new HashMap<>();
        HashMap<Integer, Integer>  femaleHighMap = new HashMap<>();

        for(AttendanceResultGroupedByAgeAndThreshold every_result: dataMapList){
            //Get ages
            ages.add(every_result.getAge());

            if(every_result.getThresholdGroup().toLowerCase().equals("high")){
                if(every_result.getGender() == Person.GENDER_MALE) {
                    maleHighMap.put(every_result.getAge(), every_result.getTotal());
                }else if(every_result.getGender() == Person.GENDER_FEMALE){
                    femaleHighMap.put(every_result.getAge(), every_result.getTotal());
                }
            }
            if(every_result.getThresholdGroup().toLowerCase().equals("medium")){
                if(every_result.getGender() == Person.GENDER_MALE) {
                    maleMidMap.put(every_result.getAge(), every_result.getTotal());
                }else if(every_result.getGender() == Person.GENDER_FEMALE){
                    femaleMidMap.put(every_result.getAge(), every_result.getTotal());
                }
            }if(every_result.getThresholdGroup().toLowerCase().equals("low")){
                if(every_result.getGender() == Person.GENDER_MALE) {
                    maleLowMap.put(every_result.getAge(), every_result.getTotal());
                }else if(every_result.getGender() == Person.GENDER_FEMALE){
                    femaleLowMap.put(every_result.getAge(), every_result.getTotal());
                }
            }

        }

        //remove duplicates on ages
        Set<Integer> hs = new HashSet<>();
        hs.addAll(ages);
        ages.clear();
        ages.addAll(hs);
        //Sort ages
        Collections.sort(ages);


        for(int age: ages){
            int totalMaleStudentsAtThisAge = 0;
            int totalFemaleStudentsAtThisAge = 0;
            int totalStudentsAtThisAge = 0;
            TableRow everyAgeRow = new TableRow(getApplicationContext());
            everyAgeRow.setLayoutParams(rowParams);

            TextView ageView = new TextView(getApplicationContext());
            ageView.setTextColor(Color.BLACK);
            ageView.setLayoutParams(everyItemParam);
            ageView.setText(String.valueOf(age));

            TextView maleLowView, femaleLowView, maleMidView, femaleMidView, maleHighView,
                    femaleHighView;

            float maleLow =0, maleMid=0, maleHigh=0, femaleLow=0, femaleMid=0, femaleHigh=0;

            maleLowView = new TextView(getApplicationContext());
            maleLowView.setTextColor(Color.BLACK);
            maleLowView.setLayoutParams(everyItemParam);
            if(maleLowMap.containsKey(age)) {
                maleLowView.setText(String.valueOf(maleLowMap.get(age)));
                totalMaleStudentsAtThisAge += maleLowMap.get(age);
                maleLow = maleLowMap.get(age);
            } else
                maleLowView.setText("0");

            femaleLowView = new TextView(getApplicationContext());
            femaleLowView.setTextColor(Color.BLACK);
            femaleLowView.setLayoutParams(everyItemParam);
            if(femaleLowMap.containsKey(age)) {
                femaleLowView.setText(String.valueOf(femaleLowMap.get(age)));
                totalFemaleStudentsAtThisAge += femaleLowMap.get(age);
                femaleLow = femaleLowMap.get(age);
            } else
                femaleLowView.setText("0");

            maleMidView = new TextView(getApplicationContext());
            maleMidView.setTextColor(Color.BLACK);
            maleMidView.setLayoutParams(everyItemParam);
            if(maleMidMap.containsKey(age)) {
                maleMidView.setText(String.valueOf(maleMidMap.get(age)));
                totalMaleStudentsAtThisAge += maleMidMap.get(age);
                maleMid = maleMidMap.get(age);
            } else maleMidView.setText("0");

            femaleMidView = new TextView(getApplicationContext());
            femaleMidView.setTextColor(Color.BLACK);
            femaleMidView.setLayoutParams(everyItemParam);
            if(femaleMidMap.containsKey(age)) {
                femaleMidView.setText(String.valueOf(femaleMidMap.get(age)));
                totalFemaleStudentsAtThisAge += femaleMidMap.get(age);
                femaleMid = femaleMidMap.get(age);
            } else femaleMidView.setText("0");

            maleHighView = new TextView(getApplicationContext());
            maleHighView.setTextColor(Color.BLACK);
            maleHighView.setLayoutParams(everyItemParam);
            if(maleHighMap.containsKey(age)) {
                maleHighView.setText(String.valueOf(maleHighMap.get(age)));
                totalMaleStudentsAtThisAge += maleHighMap.get(age);
                maleHigh = maleHighMap.get(age);
            } else maleHighView.setText("0");

            femaleHighView = new TextView(getApplicationContext());
            femaleHighView.setTextColor(Color.BLACK);
            femaleHighView.setLayoutParams(everyItemParam);
            if(femaleHighMap.containsKey(age)) {
                femaleHighView.setText(String.valueOf(femaleHighMap.get(age)));
                totalFemaleStudentsAtThisAge += femaleHighMap.get(age);
                femaleHigh = femaleHighMap.get(age);
            } else femaleHighView.setText("0");


            String identifier = "";



            totalStudentsAtThisAge = totalMaleStudentsAtThisAge + totalFemaleStudentsAtThisAge;


            if(mPresenter.getShowPercentages()){
                identifier = "%";
                if(mPresenter.isGenderDisaggregate()){
                    maleLow = (float)((maleLow / totalMaleStudentsAtThisAge) * 100);
                    maleMid = (float)((maleMid / totalMaleStudentsAtThisAge) * 100);
                    maleHigh = (float)((maleHigh / totalMaleStudentsAtThisAge) * 100);
                    femaleLow = (float)((femaleLow / totalFemaleStudentsAtThisAge) * 100);
                    femaleMid = (float)((femaleMid / totalFemaleStudentsAtThisAge) * 100);
                    femaleHigh = (float)((femaleHigh / totalFemaleStudentsAtThisAge) * 100);
                }else{
                    maleLow = (float)((maleLow + femaleLow) / totalStudentsAtThisAge) *100;
                    maleMid = (((maleMid + femaleMid) / totalStudentsAtThisAge) *100);
                    maleHigh = (((maleHigh + femaleHigh) / totalStudentsAtThisAge) *100);

                    maleLowView.setText(String.valueOf(maleLow)+identifier);
                    maleMidView.setText(String.valueOf(maleMid)+identifier);
                    maleHighView.setText(String.valueOf(maleHigh)+identifier);

                    femaleLowView.setText("");
                    femaleMidView.setText("");
                    femaleHighView.setText("");
                }
            }else {

                maleLowView.setText(String.valueOf(maleLow) + identifier);
                maleMidView.setText(String.valueOf(maleMid) + identifier);
                maleHighView.setText(String.valueOf(maleHigh) + identifier);

                femaleLowView.setText(String.valueOf(femaleLow) + identifier);
                femaleMidView.setText(String.valueOf(femaleMid) + identifier);
                femaleHighView.setText(String.valueOf(femaleHigh) + identifier);
            }

            if(!mPresenter.getShowPercentages() && !mPresenter.isGenderDisaggregate()){


                if(maleLowMap.containsKey(age)){
                    maleLow = maleLowMap.get(age);
                }
                if(maleMidMap.containsKey(age)){
                    maleMid = maleMidMap.get(age);
                }
                if(maleHighMap.containsKey(age)){
                    maleHigh = maleHighMap.get(age);
                }

                if(femaleLowMap.containsKey(age)){
                    femaleLow = femaleLowMap.get(age);
                }
                if(femaleMidMap.containsKey(age)){
                    femaleMid = femaleMidMap.get(age);
                }
                if(femaleHighMap.containsKey(age)){
                    femaleHigh = femaleHighMap.get(age);
                }

                float currentAverageLowValue = (maleLow + femaleLow) / 2;
                maleLowView.setText(String.valueOf(currentAverageLowValue));
                float currentAverageMidValue = (maleMid + femaleMid) / 2;
                maleMidView.setText(String.valueOf(currentAverageMidValue));
                float currentAverageHighValue = (maleHigh + femaleHigh) / 2;
                maleHighView.setText(String.valueOf(currentAverageHighValue));

                femaleLowView.setText("");
                femaleMidView.setText("");
                femaleHighView.setText("");


            }

            everyAgeRow.addView(ageView);
            everyAgeRow.addView(maleLowView);
            everyAgeRow.addView(femaleLowView);
            everyAgeRow.addView(maleMidView);
            everyAgeRow.addView(femaleMidView);
            everyAgeRow.addView(maleHighView);
            everyAgeRow.addView(femaleHighView);


            addThese.add(everyAgeRow);

            //heading row to tableTextData
            String[] everyAgeRowSA = new String[everyAgeRow.getChildCount()];
            for(int i = 0; i < everyAgeRow.getChildCount(); i++){
                everyAgeRowSA[i] = ((TextView) everyAgeRow.getChildAt(i)).getText().toString();
            }
            tableTextData.add(everyAgeRowSA);

        }


        return addThese;

    }


}
