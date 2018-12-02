package com.ustadmobile.core.controller;

import java.util.HashMap;
import java.util.Hashtable;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.core.view.ReportEditView.ARG_REPORT_LINK;
import static com.ustadmobile.core.view.ReportEditView.ARG_REPORT_NAME;


/**
 * The ReportEdit Presenter.
 */
public class ReportEditPresenter
        extends UstadBaseController<ReportEditView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;
    private String reportName = "";
    private String reportLink;
    private HashMap<Integer, String> timePeriodOptions;
    private boolean genderDisaggregate = false;

    public static final int TIME_PERIOD_LAST_WEEK = 1;
    public static final int TIME_PERIOD_LAST_TWO_WEEK = 2;
    public static final int TIME_PERIOD_LAST_MONTH = 3;
    public static final int TIME_PERIOD_LAST_THREE_MONTHS = 4;
    public static final int TIME_PERIOD_CUSTOM = 5;

    private long fromTime, toTime;

    public ReportEditPresenter(Object context, Hashtable arguments, ReportEditView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_REPORT_NAME)){
            reportName = (String) arguments.get(ARG_REPORT_NAME);
        }

        if(arguments.containsKey(ARG_REPORT_LINK)){
            reportLink = (String) arguments.get(ARG_REPORT_LINK);
        }
    }


    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update report name on top
        if(reportName.length()>0) {
            view.updateReportName(reportName);
        }

        //Update time period options.
        updateTimePeriod();

    }


    /**
     * Generates the time period options. Generates the hashmap and sends it to the view.
     */
    private void updateTimePeriod(){
        timePeriodOptions = new HashMap<>();
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
                break;
        }
    }

    /**
     * Hanlde the classes selected . TODO
     * @param selected  position of whats selected.
     */
    public void handleClassesSelected(int selected){
        //TODO:
    }

    /**
     * Handles what happens when you click Location.
     */
    public void handleClickLocation(){
        //TODO:
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

        //TODO: Add time period to argument

        //TODO: Add location to argument

        //Go to report with those arguments / Generate report
        String linkViewName = reportLink.split("\\?")[0];
        impl.go(linkViewName, args, view.getContext());

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
