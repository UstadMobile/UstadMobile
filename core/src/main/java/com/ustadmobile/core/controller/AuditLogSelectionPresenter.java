package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.AuditLogSelectionView;
import com.ustadmobile.core.view.AuditLogListView;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.core.view.SelectTwoDatesDialogView;

import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_ACTOR_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_CLASS_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_FROM_TIME;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_LOCATION_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_PEOPLE_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_TO_TIME;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLASSES_SET;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATIONS_SET;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;


/**
 * Presenter for AuditLogSelection view
 **/
public class AuditLogSelectionPresenter extends UstadBaseController<AuditLogSelectionView> {

    UmAppDatabase repository;

    private List<Long> selectedClasses;
    private List<Long> selectedLocations;
    private List<Long> selectedPeople;
    private List<Long> selectedActors;

    private long fromTime, toTime;

    private static final int TIME_PERIOD_LAST_WEEK = 1;
    private static final int TIME_PERIOD_LAST_TWO_WEEK = 2;
    private static final int TIME_PERIOD_LAST_MONTH = 3;
    private static final int TIME_PERIOD_LAST_THREE_MONTHS = 4;
    private static final int TIME_PERIOD_CUSTOM = 5;


    public AuditLogSelectionPresenter(Object context, Hashtable arguments, AuditLogSelectionView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
    }

    /**
     * Generates the time period options. Generates the hashmap and sends it to the view.
     */
    private void updateTimePeriod(){
        LinkedHashMap<Integer, String> timePeriodOptions = new LinkedHashMap<>();
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

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update time period options.
        updateTimePeriod();

    }

    public void handleClickPrimaryActionButton() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_AUDITLOG_FROM_TIME, fromTime);
        args.put(ARG_AUDITLOG_TO_TIME, toTime);

        if(selectedClasses != null && !selectedClasses.isEmpty()){
            Long[] classesArray = new Long[selectedClasses.size()];
            selectedClasses.toArray(classesArray);
            args.put(ARG_AUDITLOG_CLASS_LIST, classesArray);
        }

        if(selectedLocations != null && !selectedLocations.isEmpty()){
            Long[] locationsArray = new Long[selectedLocations.size()];
            selectedLocations.toArray(locationsArray);
            args.put(ARG_AUDITLOG_LOCATION_LIST, locationsArray);
        }

        if(selectedPeople != null && !selectedPeople.isEmpty()){
            Long[] peopleArray = new Long[selectedPeople.size()];
            selectedPeople.toArray(peopleArray);
            args.put(ARG_AUDITLOG_PEOPLE_LIST, peopleArray);
        }

        if(selectedActors != null && !selectedActors.isEmpty()){
            Long[] actorArray = new Long[selectedActors.size()];
            selectedActors.toArray(actorArray);
            args.put(ARG_AUDITLOG_ACTOR_LIST, actorArray);
        }

        impl.go(AuditLogListView.VIEW_NAME, args, context);
    }


    public void handleTimePeriodSelected(int selected) {
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
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-31);
                break;
            case TIME_PERIOD_LAST_THREE_MONTHS:
                fromTime = UMCalendarUtil.getDateInMilliPlusDays(-61);
                break;
            case TIME_PERIOD_CUSTOM:
                toTime = 0L;
                goToSelectTwoDatesDialog();
                break;
        }
    }

    private void goToSelectTwoDatesDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(SelectTwoDatesDialogView.VIEW_NAME, args, context);
    }

    public void goToSelectClassesDialog() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();


        if(selectedLocations != null && !selectedLocations.isEmpty()){
            Long[] selectedLocationsArray =
                    ReportOverallAttendancePresenter.convertLongList(selectedLocations);
            args.put(ARG_LOCATIONS_SET, selectedLocationsArray);
        }

        if(selectedClasses != null && !selectedClasses.isEmpty()) {
            Long[] selectedClassesArray =
                    ReportOverallAttendancePresenter.convertLongList(selectedClasses);
            args.put(ARG_CLASSES_SET, selectedClassesArray);
        }
        impl.go(SelectClazzesDialogView.VIEW_NAME, args, context);
    }

    public void goToLocationDialog() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();

        if(selectedLocations != null && !selectedLocations.isEmpty()){
            Long[] selectedLocationsArray =
                    ReportOverallAttendancePresenter.convertLongList(selectedLocations);
            args.put(ARG_LOCATIONS_SET, selectedLocationsArray);
        }

        impl.go(SelectMultipleTreeDialogView.VIEW_NAME, args, context);
    }

    public void goToPersonDialog() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();

        if(selectedPeople != null && !selectedPeople.isEmpty()){
            Long[] selectedPeopleArray =
                    ReportOverallAttendancePresenter.convertLongList(selectedPeople);
            args.put(ARG_LOCATIONS_SET, selectedPeopleArray);
        }

        //TODO: This
        //impl.go(SelectPeopleDialogView.VIEW_NAME, args, context);
    }

    public void goToActorDialog() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();

        if(selectedActors != null && !selectedActors.isEmpty()){
            Long[] selectedPeopleArray =
                    ReportOverallAttendancePresenter.convertLongList(selectedActors);
            args.put(ARG_LOCATIONS_SET, selectedPeopleArray);
        }

        //TODO: This
        //impl.go(SelectPeopleDialogView.VIEW_NAME, args, context);
    }


    //Getters and Setters:

    public List<Long> getSelectedClasses() {
        return selectedClasses;
    }

    public void setSelectedClasses(List<Long> selectedClasses) {
        if(selectedClasses == null){
            selectedClasses = new ArrayList<>();
        }
        this.selectedClasses = selectedClasses;
    }

    public List<Long> getSelectedLocations() {
        return selectedLocations;
    }

    public void setSelectedLocations(List<Long> selectedLocations) {
        if(selectedLocations == null){
            selectedLocations = new ArrayList<>();
        }
        this.selectedLocations = selectedLocations;
    }

    public List<Long> getSelectedPeople() {
        return selectedPeople;
    }

    public void setSelectedPeople(List<Long> selectedPeople) {
        if(selectedPeople == null){
            selectedPeople = new ArrayList<>();
        }
        this.selectedPeople = selectedPeople;
    }

    public List<Long> getSelectedActors() {
        return selectedActors;
    }

    public void setSelectedActors(List<Long> selectedActors) {
        if(selectedActors == null){
            selectedActors = new ArrayList<>();
        }
        this.selectedActors = selectedActors;
    }

    public long getFromTime() {
        return fromTime;
    }

    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(long toTime) {
        this.toTime = toTime;
    }



}
