package com.ustadmobile.port.android.view;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportMasterPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportMasterView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.ReportMasterItem;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ReportMasterActivity extends UstadBaseActivity implements
        ReportMasterView, PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;
    private ReportMasterPresenter mPresenter;
    private TableLayout tableLayout;

    //For export line by line data.
    List<String[]> tableTextData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_report_master);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_master_toolbar);
        toolbar.setTitle(R.string.irc_master_list_report);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tableLayout =
                findViewById(R.id.activity_report_master_table);

        //Call the Presenter
        mPresenter = new ReportMasterPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab =
                findViewById(R.id.activity_report_master_fab);
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

        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "report_irc_master_list_" +
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

    @Override
    public void updateTables(List<ReportMasterItem> items) {
        System.out.println("Updating tables with : " + items.size() + " items.");
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

        TextView classNameHTV, firstNameHTV, lastNameHTV, studentIDHTV, daysPresentHTV,
                daysAbsentHTV, daysPartialHTV, totalClazzDaysHTV, dateLeftHTV, activeStatusHTV,
                genderHTV, dobHTV;

        classNameHTV = new TextView(getApplicationContext());
        classNameHTV.setLayoutParams(everyItemParam);
        classNameHTV.setTextColor(Color.BLACK);
        classNameHTV.setTypeface(null, Typeface.BOLD);
        classNameHTV.setText(R.string.class_id);

        firstNameHTV = new TextView(getApplicationContext());
        firstNameHTV.setLayoutParams(everyItemParam);
        firstNameHTV.setTextColor(Color.BLACK);
        firstNameHTV.setTypeface(null, Typeface.BOLD);
        firstNameHTV.setText(R.string.first_name);

        lastNameHTV = new TextView(getApplicationContext());
        lastNameHTV.setLayoutParams(everyItemParam);
        lastNameHTV.setTextColor(Color.BLACK);
        lastNameHTV.setTypeface(null, Typeface.BOLD);
        lastNameHTV.setText(R.string.last_name);

        studentIDHTV = new TextView(getApplicationContext());
        studentIDHTV.setLayoutParams(everyItemParam);
        studentIDHTV.setTextColor(Color.BLACK);
        studentIDHTV.setTypeface(null, Typeface.BOLD);
        studentIDHTV.setText(R.string.student_id);

        daysPresentHTV = new TextView(getApplicationContext());
        daysPresentHTV.setLayoutParams(everyItemParam);
        daysPresentHTV.setTextColor(Color.BLACK);
        daysPresentHTV.setTypeface(null, Typeface.BOLD);
        daysPresentHTV.setText(R.string.count_present_days);

        daysAbsentHTV = new TextView(getApplicationContext());
        daysAbsentHTV.setLayoutParams(everyItemParam);
        daysAbsentHTV.setTextColor(Color.BLACK);
        daysAbsentHTV.setTypeface(null, Typeface.BOLD);
        daysAbsentHTV.setText(R.string.count_absent_days);

        daysPartialHTV = new TextView(getApplicationContext());
        daysPartialHTV.setLayoutParams(everyItemParam);
        daysPartialHTV.setTextColor(Color.BLACK);
        daysPartialHTV.setTypeface(null, Typeface.BOLD);
        daysPartialHTV.setText(R.string.count_partial_days);

        totalClazzDaysHTV = new TextView(getApplicationContext());
        totalClazzDaysHTV.setLayoutParams(everyItemParam);
        totalClazzDaysHTV.setTextColor(Color.BLACK);
        totalClazzDaysHTV.setTypeface(null, Typeface.BOLD);
        totalClazzDaysHTV.setText(R.string.class_days);

        dateLeftHTV = new TextView(getApplicationContext());
        dateLeftHTV.setLayoutParams(everyItemParam);
        dateLeftHTV.setTextColor(Color.BLACK);
        dateLeftHTV.setTypeface(null, Typeface.BOLD);
        dateLeftHTV.setText(R.string.date_left);

        activeStatusHTV = new TextView(getApplicationContext());
        activeStatusHTV.setLayoutParams(everyItemParam);
        activeStatusHTV.setTextColor(Color.BLACK);
        activeStatusHTV.setTypeface(null, Typeface.BOLD);
        activeStatusHTV.setText(R.string.active);

        genderHTV = new TextView(getApplicationContext());
        genderHTV.setLayoutParams(everyItemParam);
        genderHTV.setTextColor(Color.BLACK);
        genderHTV.setTypeface(null, Typeface.BOLD);
        genderHTV.setText(R.string.gender_literal);

        dobHTV = new TextView(getApplicationContext());
        dobHTV.setLayoutParams(everyItemParam);
        dobHTV.setTextColor(Color.BLACK);
        dobHTV.setTypeface(null, Typeface.BOLD);
        dobHTV.setText(R.string.birthday);

        headingRow.addView(classNameHTV);
        headingRow.addView(firstNameHTV);
        headingRow.addView(lastNameHTV);
        headingRow.addView(studentIDHTV);
        headingRow.addView(daysPresentHTV);
        headingRow.addView(daysAbsentHTV);
        headingRow.addView(daysPartialHTV);
        headingRow.addView(totalClazzDaysHTV);
        headingRow.addView(dateLeftHTV);
        headingRow.addView(activeStatusHTV);
        headingRow.addView(genderHTV);
        headingRow.addView(dobHTV);


        tableLayout.addView(headingRow);

        //MAKE TABLE TEXT DATA:
        String[] headingItems = new String[headingRow.getChildCount()];
        for(int i = 0; i < headingRow.getChildCount(); i++){
            headingItems[i] = ((TextView) headingRow.getChildAt(i)).getText().toString();
        }
        tableTextData.add(headingItems);


        if(!items.isEmpty()){
            TextView classNameTV, firstNameTV, lastNameTV, studentIDTV, daysPresentTV,daysAbsentTV,
                    daysPartialTV, totalClazzDaysTV, dateLeftTV, activeStatusTV, genderTV, dobTV;

            for(ReportMasterItem everyItem: items){
                TableRow iRow = new TableRow(getApplicationContext());
                iRow.setLayoutParams(rowParams);


                classNameTV = new TextView(getApplicationContext());
                classNameTV.setLayoutParams(everyItemParam);
                classNameTV.setTextColor(Color.BLACK);
                classNameTV.setText(everyItem.getClazzName());

                firstNameTV = new TextView(getApplicationContext());
                firstNameTV.setLayoutParams(everyItemParam);
                firstNameTV.setTextColor(Color.BLACK);
                firstNameTV.setText(everyItem.getFirstNames());

                lastNameTV = new TextView(getApplicationContext());
                lastNameTV.setLayoutParams(everyItemParam);
                lastNameTV.setTextColor(Color.BLACK);
                lastNameTV.setText(everyItem.getLastName());

                studentIDTV = new TextView(getApplicationContext());
                studentIDTV.setLayoutParams(everyItemParam);
                studentIDTV.setTextColor(Color.BLACK);
                studentIDTV.setText(String.valueOf(everyItem.getPersonUid()));

                daysPresentTV = new TextView(getApplicationContext());
                daysPresentTV.setLayoutParams(everyItemParam);
                daysPresentTV.setTextColor(Color.BLACK);
                daysPresentTV.setText(String.valueOf(everyItem.getDaysPresent()));

                daysAbsentTV = new TextView(getApplicationContext());
                daysAbsentTV.setLayoutParams(everyItemParam);
                daysAbsentTV.setTextColor(Color.BLACK);
                daysAbsentTV.setText(String.valueOf(everyItem.getDaysAbsent()));

                daysPartialTV = new TextView(getApplicationContext());
                daysPartialTV.setLayoutParams(everyItemParam);
                daysPartialTV.setTextColor(Color.BLACK);
                daysPartialTV.setText(String.valueOf(everyItem.getDaysPartial()));

                totalClazzDaysTV = new TextView(getApplicationContext());
                totalClazzDaysTV.setLayoutParams(everyItemParam);
                totalClazzDaysTV.setTextColor(Color.BLACK);
                totalClazzDaysTV.setText(String.valueOf(everyItem.getClazzDays()));

                dateLeftTV = new TextView(getApplicationContext());
                dateLeftTV.setLayoutParams(everyItemParam);
                dateLeftTV.setTextColor(Color.BLACK);
                dateLeftTV.setText(UMCalendarUtil.getPrettyDateFromLong((everyItem.getDateLeft())));

                activeStatusTV = new TextView(getApplicationContext());
                activeStatusTV.setLayoutParams(everyItemParam);
                activeStatusTV.setTextColor(Color.BLACK);
                activeStatusTV.setText(everyItem.isClazzMemberActive()?
                        getText(R.string.yes_literal) : getText(R.string.no_literal));

                genderTV = new TextView(getApplicationContext());
                genderTV.setLayoutParams(everyItemParam);
                genderTV.setTextColor(Color.BLACK);
                String theGender = "";
                theGender = (String) getText(R.string.not_set);
                switch (everyItem.getGender()){
                    case Person.GENDER_FEMALE:
                        theGender = (String) getText(R.string.female);
                        break;
                    case Person.GENDER_MALE:
                        theGender = (String)getText(R.string.male);
                        break;
                    case Person.GENDER_OTHER:
                        theGender = (String)getText(R.string.other_not_set);
                        break;
                    case Person.GENDER_UNSET:
                        break;
                    default:
                        theGender = (String) getText(R.string.not_set);
                }
                genderTV.setText(theGender);

                dobTV = new TextView(getApplicationContext());
                dobTV.setLayoutParams(everyItemParam);
                dobTV.setTextColor(Color.BLACK);

                dobTV.setText(UMCalendarUtil.getPrettyDateFromLong(everyItem.getDateOfBirth()));

                iRow.addView(classNameTV);
                iRow.addView(firstNameTV);
                iRow.addView(lastNameTV);
                iRow.addView(studentIDTV);
                iRow.addView(daysPresentTV);
                iRow.addView(daysAbsentTV);
                iRow.addView(daysPartialTV);
                iRow.addView(totalClazzDaysTV);
                iRow.addView(dateLeftTV);
                iRow.addView(activeStatusTV);
                iRow.addView(genderTV);
                iRow.addView(dobTV);

                //BUILD TABLE TEXT DATA
                String[] rowItems = new String[iRow.getChildCount()];
                for(int i = 0; i < iRow.getChildCount(); i++){
                    rowItems[i] = ((TextView)iRow.getChildAt(i)).getText().toString();
                }
                tableTextData.add(rowItems);

                tableLayout.addView(iRow);

            }

        }
    }
}
