package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.SelectAttendanceThresholdsDialogView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.core.view.SelectTwoDatesDialogView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATIONS_SET;
import static com.ustadmobile.core.view.ReportEditView.ARG_REPORT_LINK;
import static com.ustadmobile.core.view.ReportEditView.ARG_REPORT_NAME;
import static com.ustadmobile.core.view.ReportEditView.ARG_SHOW_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_SHOW_RADIO_GROUP;
import static com.ustadmobile.core.view.ReportEditView.ARG_SHOW_THERSHOLD;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_NUMBER;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_PERCENTAGE;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_HIGH;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_LOW;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_MID;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;


/**
 * The ReportEdit Presenter.
 */
public class ReportEditPresenter
        extends UstadBaseController<ReportEditView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;
    private String reportName = "";
    private String reportLink;
    private LinkedHashMap<Integer, String> timePeriodOptions;
    private boolean genderDisaggregate = false;
    private boolean studentNumbers = false;
    private boolean studentPercentages = false;
    private List<Long> selectedClasses;
    private List<Long> selectedLocations;
    private ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues thresholdValues;

    private static final int TIME_PERIOD_LAST_WEEK = 1;
    private static final int TIME_PERIOD_LAST_TWO_WEEK = 2;
    private static final int TIME_PERIOD_LAST_MONTH = 3;
    private static final int TIME_PERIOD_LAST_THREE_MONTHS = 4;
    private static final int TIME_PERIOD_CUSTOM = 5;

    private boolean showthreshold, showRadioGroup, showGenderDisaggregate;
    private long fromTime, toTime;

    public ReportEditPresenter(Object context, Hashtable arguments, ReportEditView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_REPORT_NAME)){
            reportName = (String) arguments.get(ARG_REPORT_NAME);
        }

        if(arguments.containsKey(ARG_REPORT_LINK)){
            reportLink = (String) arguments.get(ARG_REPORT_LINK);
        }

        if (arguments.containsKey(ARG_SHOW_THERSHOLD)) {
            showthreshold = (boolean) arguments.get(ARG_SHOW_THERSHOLD);
        }
        if(arguments.containsKey(ARG_SHOW_RADIO_GROUP)){
            showRadioGroup = (boolean) arguments.get(ARG_SHOW_RADIO_GROUP);
        }
        if(arguments.containsKey(ARG_SHOW_GENDER_DISAGGREGATE)){
            showGenderDisaggregate = (boolean) arguments.get(ARG_SHOW_GENDER_DISAGGREGATE);
        }

    }


    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update report name on top
        if(reportName.length()>0) {
            view.updateReportName(reportName);
        }
        view.showAttendanceThresholdView(showthreshold);
        view.showShowStudentNumberPercentageView(showRadioGroup);
        view.showGenderDisaggregate(showGenderDisaggregate);

        //Update time period options.
        updateTimePeriod();

    }

    /**
     * Generates the time period options. Generates the hashmap and sends it to the view.
     */
    private void updateTimePeriod(){
        timePeriodOptions = new LinkedHashMap<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        timePeriodOptions.put(TIME_PERIOD_LAST_WEEK,
                impl.getString(MessageID.last_week, context));
        timePeriodOptions.put(TIME_PERIOD_LAST_TWO_WEEK,
                impl.getString(MessageID.last_two_weeks, context));
        timePeriodOptions.put(TIME_PERIOD_LAST_MONTH,
                impl.getString(MessageID.last_month, context));
        timePeriodOptions.put(TIME_PERIOD_LAST_THREE_MONTHS,
                impl.getString(MessageID.last_three_months, context));
        timePeriodOptions.put(TIME_PERIOD_CUSTOM,
                impl.getString(MessageID.custom_date_range, context));

        view.populateTimePeriod(timePeriodOptions);

    }


    /**
     * Hanlde the time period drop down selector in the drop-down/list/spinner. This should set the
     * from and to times based on the time of selection that will be used to generate the
     * report that follows. The from and to time paramters are stored in the presenter and
     * sent to the report (via arguments) upon clicking primary action button (Create report)
     *
     * @param selected  The flag of drop - down / spinner item selected.
     */
    public void handleTimePeriodSelected(int selected){
        toTime = System.currentTimeMillis();
        selected++;
        switch (selected){
            case TIME_PERIOD_LAST_WEEK:
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-7);
                break;
            case TIME_PERIOD_LAST_TWO_WEEK:
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-14);
                break;
            case TIME_PERIOD_LAST_MONTH:
                //TODO: Figure out from the 1st of this month
                //TODO: Figure out if the last month had 30 or 31 or 28 days.
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-31);
                break;
            case TIME_PERIOD_LAST_THREE_MONTHS:
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-61);
                break;
            case TIME_PERIOD_CUSTOM:
                toTime = 0L;
                view.showCustomDateSelector();
                goToSelectTwoDatesDialog();
                break;
        }
    }

    public void goToSelectTwoDatesDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(SelectTwoDatesDialogView.VIEW_NAME, args, context);
    }

    /**
     * Hanlde the classes selected . TODO
     * @param selected  position of whats selected.
     */
    public void handleClassesSelected(int selected){
        //TODO:
    }

    public List<Long> getSelectedClasses() {
        return selectedClasses;
    }

    public void setSelectedClasses(List<Long> selectedClasses) {
        this.selectedClasses = selectedClasses;
    }

    public List<Long> getSelectedLocations() {
        return selectedLocations;
    }

    public void setSelectedLocations(List<Long> selectedLocations) {
        this.selectedLocations = selectedLocations;
    }

    public ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues getThresholdValues() {
        return thresholdValues;
    }

    public void setThresholdValues(ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues thresholdValues) {
        this.thresholdValues = thresholdValues;
    }

    /**
     * Goes and opens up the Location dialog
     */
    public void goToLocationDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();

        impl.go(SelectMultipleTreeDialogView.VIEW_NAME, args, context);
    }

    public void goToSelectClassesDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();

        //TODO: put locations set in the args to show only the classes in that location
        args.put(ARG_LOCATIONS_SET, new ArrayList<>());

        impl.go(SelectClazzesDialogView.VIEW_NAME, args, context);
    }

    public void goToSelectAttendanceThresholdsDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(SelectAttendanceThresholdsDialogView.VIEW_NAME, args, context);
    }

    /**
     * Hanldes what happens when you click the "Create Report" button.
     * ie: should go to the report with the info set on this presenter.
     */
    public void handleClickPrimaryActionButton() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        args.put(ARG_REPORT_NAME, reportName);

        //TODone: Add time period to argument
        args.put(ARG_FROM_DATE, fromTime);
        args.put(ARG_TO_DATE, toTime);

        //TODO: Add location to argument

        //TODO: Add Clazzes

        //TODO: Add Thresholds
        if(thresholdValues != null) {
            args.put(ARG_THRESHOLD_LOW, thresholdValues.low);
            args.put(ARG_THRESHOLD_MID, thresholdValues.med);
            args.put(ARG_THRESHOLD_HIGH, thresholdValues.high);
        }

        args.put(ARG_GENDER_DISAGGREGATE, genderDisaggregate);

        args.put(ARG_STUDENT_IDENTIFIER_NUMBER, studentNumbers);
        args.put(ARG_STUDENT_IDENTIFIER_PERCENTAGE, studentPercentages);

        //Go to report with those arguments / Generate report
        String linkViewName = reportLink.split("\\?")[0];
        impl.go(linkViewName, args, view.getContext());

    }

    public boolean isStudentNumbers() {
        return studentNumbers;
    }

    public void setStudentNumbers(boolean studentNumbers) {
        this.studentNumbers = studentNumbers;
    }

    public boolean isStudentPercentages() {
        return studentPercentages;
    }

    public void setStudentPercentages(boolean studentPercentages) {
        this.studentPercentages = studentPercentages;
    }

    /**
     * Get the from Time for the report.
     *
     * @return  the set from time.
     */
    public long getFromTime() {
        return fromTime;
    }

    /**
     * Set the from time for the report.
     * @param fromTime  the from time in long
     */
    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    /**
     * Gets the To time
     * @return  the to time in long
     */
    public long getToTime() {
        return toTime;
    }

    /**
     * Sets the to time.
     * @param toTime    the to time in long
     */
    public void setToTime(long toTime) {
        this.toTime = toTime;
    }

    /**
     * Gets current set gender disaggregate option
     *
     * @return  true if set (ticked/checked), false if not.
     */
    public boolean isGenderDisaggregate() {
        return genderDisaggregate;
    }

    /**
     * Sets gender disaggregate. Also updates the view.
     *
     * @param genderDisaggregate    true if to set as ticked/checked. false if not.
     */
    public void setGenderDisaggregate(boolean genderDisaggregate) {
        this.genderDisaggregate = genderDisaggregate;
        view.updateGenderDisaggregationSet(genderDisaggregate);
    }

    @Override
    public void setUIStrings() {

    }

}
