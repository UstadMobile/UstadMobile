package com.ustadmobile.port.android.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportSELPresenter;
import com.ustadmobile.core.view.ReportSELView;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.port.android.view.ReportAttendanceGroupedByThresholdsActivity.dpToPx;

public class ReportSELActivity extends UstadBaseActivity implements
        ReportSELView, PopupMenu.OnMenuItemClickListener{

    private LinearLayout reportLinearLayout;

    public static final int TAG_NOMINEE_CLAZZMEMBER_UID = R.string.nomination;
    public static final int TAG_NOMINATOR_CLAZZMEMBER_UID = R.string.nominating;
    public static final int TAG_NOMINATOR_NAME = R.string.name;
    public static final int SEL_REPORT_QUESTION_BUTTON_RADIUS = 50;

    //For export line by line data.
    List<String[]> tableTextData;

    //Presenter
    ReportSELPresenter mPresenter;

    LinearLayout.LayoutParams imageLP =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

    //The clazz sel report data
    LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_report_sel);

        //Toolbar:
        Toolbar toolbar = findViewById(R.id.activity_report_sel_toolbar);
        toolbar.setTitle(R.string.sel_report);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        reportLinearLayout = findViewById(R.id.activity_report_sel_ll);

        //Call the Presenter
        mPresenter = new ReportSELPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_report_sel_fab);
        fab.setOnClickListener(this::showPopup);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_export, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_export_csv) {
            generateCSVReport();
            return true;
        } else if (i == R.id.menu_export_xls) {
            try {
                startXLSXReportGeneration();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;

        }
//        } else if (i == R.id.menu_export_json) {
//            mPresenter.dataToJSON();
//            return true;
//        }
        else {
            return false;
        }
    }

    /**
     * Starts the xlsx report process. Here it crates hte xlsx file.
     */
    private void startXLSXReportGeneration(){

        File dir = getFilesDir();
        String xlsxReportPath;

        String title = "report_sel_" + System.currentTimeMillis();

        File output = new File(dir, title + ".xlsx");
        xlsxReportPath = output.getAbsolutePath();

        File testDir = new File(dir, title);
        testDir.mkdir();
        String workingDir = testDir.getAbsolutePath();

        mPresenter.dataToXLSX(title, xlsxReportPath, workingDir);

    }

    @Override
    public void generateCSVReport() {

        String csvReportFilePath;
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "report_sel_" +
                System.currentTimeMillis() + ".csv");
        csvReportFilePath = output.getAbsolutePath();

        try {
            FileWriter fileWriter = new FileWriter(csvReportFilePath);

            for (String[] aTableTextData : tableTextData) {
                boolean firstDone = false;
                for (String aLineArray : aTableTextData) {
                    if (firstDone) {
                        fileWriter.append(",");
                    }
                    firstDone = true;
                    fileWriter.append(aLineArray);
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
    public void generateXLSReport(String xlsxReportPath) {

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
     *
     * Updates the raw data given and starts to construct the tables on the SEL report.
     *
     * @param clazzMap          The raw sel report data in a map grouped by clazz, further grouped
     *                          by questions and further by nominator -> nominee list
     * @param clazzToStudents   A map of every clazz and its clazz members for the view to construct
     */
    @Override
    public void createTables(LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap,
                             HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents) {

        //Build a string array of the data
        tableTextData = new ArrayList<>();

        this.clazzMap = clazzMap;

        //Work with: reportLinearLayout linear layout
        TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        headingParams.setMargins(dpToPx(8),dpToPx(32),dpToPx(8),dpToPx(16));

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        everyItemParam.gravity = Gravity.CENTER_VERTICAL;

        //For every clazz:
        for (String currentClazzName : clazzMap.keySet()) {
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


            LinkedHashMap<String, Map<Long, List<Long>>> clazzNominationData = clazzMap.get(currentClazzName);

            //Create a tableLayout for this clazz for all the Students.
            TableLayout tableLayout = generateTableLayoutForClazz(clazzMembers);

            //Default look up first question when constructing the SEL report tables.
            assert clazzNominationData != null;
            String firstQuestionTitle = clazzNominationData.keySet().iterator().next();
            updateTableBasedOnQuestionSelected(currentClazzName, firstQuestionTitle, tableLayout);

            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
            horizontalScrollView.addView(tableLayout);

            reportLinearLayout.addView(clazzHeading);
            reportLinearLayout.addView(horizontalScrollView);

            LinearLayout hLL = new LinearLayout(this);
            LinearLayout.LayoutParams hllP =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            hLL.setLayoutParams(hllP);
            hLL.setOrientation(LinearLayout.HORIZONTAL);

            Set<String> questions = clazzNominationData.keySet();
            List<Button> allButtons = new ArrayList<>();

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.weight = 1;

            for (String everyQuestion : questions) {

                Button questionButton = new Button(this);
                questionButton.setLayoutParams(buttonParams);
                questionButton.setTextSize(12);
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(SEL_REPORT_QUESTION_BUTTON_RADIUS);

                if(firstQuestionTitle.equals(everyQuestion)){
                    shape.setColorFilter(getResources().getColor(R.color.primary),
                            PorterDuff.Mode.ADD);
                }else{
                    shape.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD);
                }
                questionButton.setBackground(shape);
                questionButton.setText(everyQuestion);
                questionButton.setPadding(12,20,12,8);
                //questionButton.setLines(3);
                questionButton.setGravity(1);

                allButtons.add(questionButton);
                questionButton.setOnClickListener(v ->
                {
                    updateTableBasedOnQuestionSelected(currentClazzName, everyQuestion,
                            tableLayout);


                    //Gray out other buttons.
                    for(Button everyButton:allButtons){
                        GradientDrawable shapeb = new GradientDrawable();
                        shapeb.setCornerRadius(SEL_REPORT_QUESTION_BUTTON_RADIUS);
                        shapeb.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD);
                        everyButton.setBackground(shapeb);
                    }
                    GradientDrawable shapea = new GradientDrawable();
                    shapea.setCornerRadius(SEL_REPORT_QUESTION_BUTTON_RADIUS);
                    shapea.setColorFilter(getResources().getColor(R.color.primary),
                            PorterDuff.Mode.ADD);
                    v.setBackground(shapea);

                });

                hLL.addView(questionButton);
            }
            reportLinearLayout.addView(hLL);


        }
    }

    /**
     * Updates the current Clazz SEL table and updates its sel result nomination markings based
     * on the question uid selected from the raw rel report data
     * @param clazzName         The clazzName of the table we want to update (used to get raw data)
     * @param questionTitle     The question title we want the table to reflect
     * @param tableLayout       The table it self that needs updating.
     */
    private void updateTableBasedOnQuestionSelected(String clazzName, String questionTitle,
                                                TableLayout tableLayout){

        LinkedHashMap<String, Map<Long, List<Long>>> clazzNominationData = clazzMap.get(clazzName);
        assert clazzNominationData != null;
        Map<Long, List<Long>> testFirstQuestionData = clazzNominationData.get(questionTitle);
        assert testFirstQuestionData != null;
        //For every nominations in that question ..
        for (Long nominatorUid : testFirstQuestionData.keySet()) {
            List<Long> nomineeList = testFirstQuestionData.get(nominatorUid);
            // ..update the markings on the TableLayout
            processTable(nominatorUid, nomineeList, tableLayout);
        }
    }

    /**
     * Update the sel nomination ticks to the table provided for the given nominator.
     *
     * @param nominatorUid  The Nominator's ClazzMember Uid
     * @param nomineeList   The Nominations (nominee List) nominated by the nominatorUid
     *                      in a list of ClazzMember Uids
     * @param tableLayout   The table to update the ticks/crosses for the given Nominator and
     *                      its nominations.
     */
    private void processTable(Long nominatorUid, List<Long> nomineeList, TableLayout tableLayout) {

        TableRow nominatorRow = null;
        //Find the table Row that is marked with the Nominator Uid we want.
        for(int i=0; i<tableLayout.getChildCount(); i++){
            View child = tableLayout.getChildAt(i);
            if(child instanceof  TableRow){
                Long childNominatorUid = (Long) child.getTag(TAG_NOMINATOR_CLAZZMEMBER_UID);
                if(childNominatorUid != null){
                    if(childNominatorUid.longValue() == nominatorUid.longValue()) {
                        //Save it for the next loop
                        nominatorRow = (TableRow) child;
                        break;
                    }
                }
            }
        }

        if(nominatorRow != null){
            //Find all views within that nominator Row that have an id in the NomineeList
            for(int j=0;j<nominatorRow.getChildCount();j++){
                View rowChild = nominatorRow.getChildAt(j);
                if(rowChild instanceof ImageView){
                    Long rowChildNomineeUid = (Long) rowChild.getTag(TAG_NOMINEE_CLAZZMEMBER_UID);
                    if(rowChildNomineeUid != null){
                        if(nomineeList.contains(rowChildNomineeUid)){
                            //If this cell is in the nominee list , change the view to be a tick!
                            ImageView nomineeImageView = (ImageView) rowChild;
                            nomineeImageView.setImageResource(R.drawable.ic_check_black_24dp);
                        }else{
                            //if not, its a cross
                            ImageView nomineImageView = (ImageView) rowChild;
                            nomineImageView.setImageResource(R.drawable.ic_clear_black_24dp);
                        }
                    }
                }
            }
        }

    }

    /**
     * Generates an SEL table layout view for the given Clazz Members.
     *  ____________________________________________
     * |SEL Heading Row ... |         |         |   |
     * |  Nominating:       |Student 1|Student 2|...|
     * |____________________|_________|_________|___|
     * |Nomination Row 1..  |         |         |   |
     * |  Student 1         |   -     |    ✓    |   |
     * |____________________|_________|_________|___|
     * |Nomination Row 2..  |         |         |   |
     * |  Student 2         |   ✖     |   -     |   |
     * |____________________|_________|_________|___|
     * |  ...               |         |         |   |
     * |____________________|_________|_________|___|
     *
     *
     * @param clazzMembers  A list of type ClazzMemberWithPerson of all the clazz members
     *
     * @return  The TabLayout View with the SEL report.
     *
     */
    public TableLayout generateTableLayoutForClazz(List<ClazzMemberWithPerson> clazzMembers){

        //LAYOUT Parameters
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams everyItemParam = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        everyItemParam.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        everyItemParam.gravity = Gravity.CENTER_VERTICAL;

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        //Create a new table layout
        TableLayout selTableLayout  = new TableLayout(getApplicationContext());
        selTableLayout.setLayoutParams(tableParams);

        //SEL Table's heading row
        TableRow selTableTopRow = new TableRow(getApplicationContext());
        selTableTopRow.setLayoutParams(rowParams);

        //SEL Table's nomination rows
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
        //Add vertical line after every item in the rows
        selTableTopRow.addView(getVerticalLine());

        // Loop through every student in this Clazz and add to Nominee names in the heading row.
        for(ClazzMemberWithPerson everyClazzMember:clazzMembers){

            //Add this textview to the heading row (nominees)
            TextView aStudentTopRowTV = new TextView(getApplicationContext());
            aStudentTopRowTV.setLayoutParams(everyItemParam);
            aStudentTopRowTV.setTextSize(12);
            aStudentTopRowTV.setTextColor(Color.BLACK);
            String personName = everyClazzMember.getPerson().getFirstNames() + " " +
                    everyClazzMember.getPerson().getLastName();
            aStudentTopRowTV.setText(personName);

            selTableTopRow.addView(aStudentTopRowTV);
            //Add vertical line every time in the rows
            selTableTopRow.addView(getVerticalLine());


            //Create Nomination Rows for every clazz member (students here)
            TableRow nominationRow = new TableRow(getApplicationContext());
            nominationRow.setLayoutParams(rowParams);

            //Add this textView as the first cell in the nomination rows
            TextView nominationRowStudentTV = new TextView(this);
            nominationRowStudentTV.setLayoutParams(everyItemParam);
            nominationRowStudentTV.setTextSize(12);
            nominationRowStudentTV.setTextColor(Color.BLACK);
            nominationRowStudentTV.setText(personName);
            nominationRowStudentTV.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID,
                    everyClazzMember.getClazzMemberUid());

            //Add vertical line between cells in the row:
            nominationRow.addView(getVerticalLine());
            //Add the nominator name to the nominator Row.
            nominationRow.addView(nominationRowStudentTV);

            //Set tags on the rows so we can find these rows when populating the sel results
            nominationRow.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID, everyClazzMember.getClazzMemberUid());
            nominationRow.setTag(TAG_NOMINATOR_NAME, everyClazzMember.getPerson().getFirstNames()
                    + " " + everyClazzMember.getPerson().getLastName());
            //vertical line after nominator name
            nominationRow.addView(getVerticalLine());
            //Loop through All Students again to addd the default tick/cross/dash image views and
            // assign them nominee and nominator tags (so we can alter then later)
            for(ClazzMemberWithPerson againClazzMember: clazzMembers){
                View crossView = getCross();
                crossView.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID, everyClazzMember.getClazzMemberUid());
                crossView.setTag(TAG_NOMINEE_CLAZZMEMBER_UID, againClazzMember.getClazzMemberUid());
                if(everyClazzMember.getClazzMemberUid() == againClazzMember.getClazzMemberUid()){
                    nominationRow.addView(getNA());
                }else {
                    nominationRow.addView(crossView);
                }
                //need that vertical line
                nominationRow.addView(getVerticalLine());
            }
            //All all Nominee imageview and vertical line views to the nomination rows
            nominationRows.add(nominationRow);
        }

        //Table layout top horizontal line
        selTableLayout.addView(getHorizontalLine());
        //Table layout top row
        selTableLayout.addView(selTableTopRow);
        //another horizontal line
        selTableLayout.addView(getHorizontalLine());

        //Get every nomination rows
        for(TableRow everyRow : nominationRows){
            //..and add it to the table layout
            selTableLayout.addView(everyRow);
            //can't forget the horizontal line
            selTableLayout.addView(getHorizontalLine());
        }

        //Make it scrollable
        selTableLayout.setScrollContainer(true);
        return selTableLayout;
    }

    /**
     * Creates a new Vertical line for a table's row
     * @return  The vertical line view.
     */
    public View getVerticalLine(){
        //Vertical line
        TableRow.LayoutParams vLineParams =
                new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT);
        View vla = new View(this);
        vla.setBackgroundColor(Color.GRAY);
        vla.setLayoutParams(vLineParams);
        return vla;
    }

    /**
     * Creates a new Horizontal line for a table's row.
     * @return  The horizontal line view.
     */
    public View getHorizontalLine(){
        //Horizontal line
        TableRow.LayoutParams hlineParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1);
        View hl = new View(this);
        hl.setBackgroundColor(Color.GRAY);
        hl.setLayoutParams(hlineParams);
        return hl;
    }

    /**
     * Creates and returns a new tick Image View.
     * @return  The imageview view
     */
    public View getTick(){
        ImageView tickIV = new ImageView(this);
        tickIV.setImageResource(R.drawable.ic_check_black_24dp);
        tickIV.setColorFilter(Color.GRAY);
        //tickIV.setLayoutParams(imageLP);
        return tickIV;
    }

    /**
     * Creates and returns a new cross/cancel/remove Image View
     * @return  The imageview view
     */
    public View getCross(){
        imageLP.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        ImageView crossIV = new ImageView(this);
        //crossIV.setLayoutParams(imageLP);
        crossIV.setImageResource(R.drawable.ic_clear_black_24dp);
        crossIV.setColorFilter(Color.GRAY);
        return crossIV;
    }

    /**
     * Create and returns a new - (dash) ImageView
     * @return  The imageview view
     */
    public View getNA(){
        ImageView naIV = new ImageView(this);
        naIV.setImageResource(R.drawable.ic_remove_black_24dp);
        naIV.setColorFilter(Color.GRAY);
        //naIV.setLayoutParams(imageLP);
        return naIV;
    }

}
