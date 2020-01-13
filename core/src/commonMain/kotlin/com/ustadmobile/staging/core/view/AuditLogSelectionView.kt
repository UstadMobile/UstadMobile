package com.ustadmobile.core.view


/**
 * Core View. Screen is for AuditLogSelection's View
 */
interface AuditLogSelectionView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Populate the view with the list of time periods. The hashmap also has every option mapped
     * to an id to handle.
     *
     * @param options   A hashmap of the id and the string option of time periods
     */
    fun populateTimePeriod(options: HashMap<Int, String>)

    /**
     * Updates locations list as string on the locations text field on the view.
     *
     * @param locations The location selected string (eg: "Location1, Location2")
     */
    fun updateLocationsSelected(locations: String)

    /**
     * Updates string to view
     *
     * @param clazzes   The string to update to the view
     */
    fun updateClazzesSelected(clazzes: String)

    fun updatePeopleSelected(people: String)

    fun updateActorsSelected(actors: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "AuditLogSelection"

        //Any argument keys:
        val ARG_AUDITLOG_FROM_TIME = "AuditLogFromTime"
        val ARG_AUDITLOG_TO_TIME = "AuditLogToTime"
        val ARG_AUDITLOG_CLASS_LIST = "AuditLogClassList"
        val ARG_AUDITLOG_LOCATION_LIST = "AuditLogLocationList"
        val ARG_AUDITLOG_PEOPLE_LIST = "AuditLogPeopleList"
        val ARG_AUDITLOG_ACTOR_LIST = "AuditLogActorList"
    }


}

