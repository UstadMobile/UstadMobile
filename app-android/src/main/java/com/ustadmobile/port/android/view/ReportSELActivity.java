package com.ustadmobile.port.android.view;

import android.content.Intent;
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
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportSELPresenter;
import com.ustadmobile.core.view.ReportSELView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.port.android.view.ReportAttendanceGroupedByThresholdsActivity.dpToPx;

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

        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "report_sel_" +
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

    @Override
    public void generateXLSReport() {

    }

    @Override
    public void updateTables(LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap,
                             HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents) {

        //Build a string array of the data
        tableTextData = new ArrayList<>();

        //Work with: reportLinearLayout linear layout
        //LAYOUT
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        headingParams.setMargins(dpToPx(8),dpToPx(32),dpToPx(8),dpToPx(16));

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        everyItemParam.gravity = Gravity.CENTER_VERTICAL;

        Iterator<String> mapIterator = clazzMap.keySet().iterator();

        //For every clazz:
        while(mapIterator.hasNext()){
            String currentClazzName = mapIterator.next();

            List<ClazzMemberWithPerson> clazzMembers = clazzToStudents.get(currentClazzName);

            //Class Name heading
            TextView clazzHeading = new TextView(getApplicationContext());
            clazzHeading.setLayoutParams(headingParams);
            clazzHeading.setTextColor(Color.BLACK);
            clazzHeading.setTypeface(null, Typeface.BOLD);
            clazzHeading.setText(currentClazzName);

            //table text data for reports
            String[] titleItems = new String[]{currentClazzName};
            tableTextData.add(titleItems);

            TableLayout tableLayout = generateTableLayoutForClazz(clazzMembers);

            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
            horizontalScrollView.addView(tableLayout);

            reportLinearLayout.addView(clazzHeading);
            //reportLinearLayout.addView(tableLayout);
            reportLinearLayout.addView(horizontalScrollView);


        }
    }

    public TableLayout generateTableLayoutForClazz(List<ClazzMemberWithPerson> clazzMembers){

        //LAYOUT
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        everyItemParam.gravity = Gravity.CENTER_VERTICAL;

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        TableLayout selTableLayout  = new TableLayout(getApplicationContext());
        selTableLayout.setLayoutParams(tableParams);

        //SEL Table's heading
        TableRow selTableTopRow = new TableRow(getApplicationContext());
        selTableTopRow.setLayoutParams(rowParams);

        List<TableRow> nominationRows = new ArrayList<>();


        //SEL Table's heading's 1st item: Nominating.
        selTableTopRow.addView(getVerticalLine());
        TextView nominatingHeading = new TextView(getApplicationContext());
        nominatingHeading.setLayoutParams(everyItemParam);
        nominatingHeading.setTextSize(12);
        nominatingHeading.setTextColor(Color.BLACK);
        String nominatingString = getText(R.string.nominating) + ":";
        nominatingHeading.setText(nominatingString);

        selTableTopRow.addView(nominatingHeading);

        selTableTopRow.addView(getVerticalLine());

        // Loop through every student in this Clazz and add to clazzSELTableHeading view
        int index = 0;
        for(ClazzMemberWithPerson everyClazzMember:clazzMembers){
            index++;
            TextView aStudentTopRowTV = new TextView(getApplicationContext());
            aStudentTopRowTV.setLayoutParams(everyItemParam);
            aStudentTopRowTV.setTextSize(12);
            aStudentTopRowTV.setTextColor(Color.BLACK);
            String personName = everyClazzMember.getPerson().getFirstNames() + " " +
                    everyClazzMember.getPerson().getLastName();
            aStudentTopRowTV.setText(personName);

            selTableTopRow.addView(aStudentTopRowTV);
            selTableTopRow.addView(getVerticalLine());


            //Create Nomination Rows:
            TableRow nominationRow = new TableRow(getApplicationContext());
            nominationRow.setLayoutParams(rowParams);



            TextView nominationRowStudentTV = new TextView(this);
            nominationRowStudentTV.setLayoutParams(everyItemParam);
            nominationRowStudentTV.setTextSize(12);
            nominationRowStudentTV.setTextColor(Color.BLACK);
            nominationRowStudentTV.setText(personName);

            nominationRow.addView(getVerticalLine());
            nominationRow.addView(nominationRowStudentTV);
            nominationRow.addView(getVerticalLine());
            for(ClazzMemberWithPerson againClazzMember: clazzMembers){
                nominationRow.addView(getCross());
                nominationRow.addView(getVerticalLine());
            }

            nominationRows.add(nominationRow);
        }

        selTableLayout.addView(getHorizontalLine());
        selTableLayout.addView(selTableTopRow);
        selTableLayout.addView(getHorizontalLine());

        //Create every nomination rows
        for(TableRow everyRow : nominationRows){
            selTableLayout.addView(everyRow);
            selTableLayout.addView(getHorizontalLine());
        }

        selTableLayout.setScrollContainer(true);
        return selTableLayout;
    }

    public View getVerticalLine(){
        //V line
        //Vertical line params
        TableRow.LayoutParams vLineParams =
                new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT);
        View vla = new View(this);
        vla.setBackgroundColor(Color.GRAY);
        vla.setLayoutParams(vLineParams);
        return vla;
    }

    public View getHorizontalLine(){
        //Horizontal line params
        TableRow.LayoutParams hlineParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, 1);
        View hl = new View(this);
        hl.setBackgroundColor(Color.GRAY);
        hl.setLayoutParams(hlineParams);
        return hl;
    }

    public View getTick(){

        ImageView tickIV = new ImageView(this);
        tickIV.setImageResource(R.drawable.ic_check_black_24dp);
        tickIV.setColorFilter(Color.GRAY);
        return tickIV;
    }

    public View getCross(){

        LinearLayout.LayoutParams imageLP =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        imageLP.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        ImageView crossIV = new ImageView(this);
        //crossIV.setLayoutParams(imageLP);
        crossIV.setImageResource(R.drawable.ic_clear_black_24dp);
        crossIV.setColorFilter(Color.GRAY);
        return crossIV;
    }

    public View getNA(){
        ImageView naIV = new ImageView(this);
        naIV.setImageResource(R.drawable.ic_remove_black_24dp);
        naIV.setColorFilter(Color.GRAY);
        return naIV;
    }

}
