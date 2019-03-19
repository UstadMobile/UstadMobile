package com.ustadmobile.core.view;


import java.util.HashMap;

/**
 * Core View. Screen is for AuditLogSelection's View
 */
public interface AuditLogSelectionView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AuditLogSelection";

    //Any argument keys:
    String ARG_AUDITLOG_FROM_TIME = "AuditLogFromTime";
    String ARG_AUDITLOG_TO_TIME = "AuditLogToTime";
    String ARG_AUDITLOG_CLASS_LIST="AuditLogClassList";
    String ARG_AUDITLOG_LOCATION_LIST = "AuditLogLocationList";
    String ARG_AUDITLOG_PEOPLE_LIST = "AuditLogPeopleList";
    String ARG_AUDITLOG_ACTOR_LIST = "AuditLogActorList";

    /**
     * Method to finish the screen / view.
     */
    void finish();

    /**
     * Populate the view with the list of time periods. The hashmap also has every option mapped
     * to an id to handle.
     *
     * @param options   A hashmap of the id and the string option of time periods
     */
    void populateTimePeriod(HashMap<Integer, String> options);

    /**
     * Updates locations list as string on the locations text field on the view.
     *
     * @param locations The location selected string (eg: "Location1, Location2")
     */
    void updateLocationsSelected(String locations);

    /**
     * Updates string to view
     *
     * @param clazzes   The string to update to the view
     */
    void updateClazzesSelected(String clazzes);

    void updatePeopleSelected(String people);

    void updateActorsSelected(String actors);



}

