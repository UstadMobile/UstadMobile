package com.ustadmobile.staging.core.view

import com.ustadmobile.core.view.UstadView


/**
 * ReportEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ReportEditView : UstadView {


    /**
     * For Android: closes the activity.
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
     * Sets if the "Disaggregate by gender" checkbox is set
     * @param byGender
     */
    fun updateGenderDisaggregationSet(byGender: Boolean)

    /**
     * Updates the report name heading.
     *
     * @param name  The report name
     */
    fun updateReportName(name: String)

    /**
     * Shows the custom from/to date selector when Custom date option selected on time period spinner
     */
    fun showCustomDateSelector()

    /**
     * Update text to view
     *
     * @param thresholdString   The text
     */
    fun updateThresholdSelected(thresholdString: String)

    /**
     * Updates string to view
     *
     * @param clazzes   The string to update to the view
     */
    fun updateClazzesSelected(clazzes: String)

    /**
     * Show attendance threshold options or remove them.
     * @param show  true if yes. false if no
     */
    fun showAttendanceThresholdView(show: Boolean)

    /**
     * Show radio button group or not (to show student numbers and percentages(
     * @param show  true is yes, false if no
     */
    fun showShowStudentNumberPercentageView(show: Boolean)

    /**
     * Show gender disaggregated checkbox
     * @param show  if true will show it
     */
    fun showGenderDisaggregate(show: Boolean)

    /**
     * Shows the clazz list selector option or not
     * @param show  if true will show it
     */
    fun showClazzes(show: Boolean)

    /**
     * Shows the locations list selector option or not
     * @param show  if true, will show it
     */
    fun showLocations(show: Boolean)

    companion object {

        val VIEW_NAME = "ReportEdit"

        val ARG_REPORT_NAME = "ReportName"
        val ARG_REPORT_DESC = "ReportDesc"
        val ARG_REPORT_LINK = "ReportLink"
        val ARG_LOCATIONS_SET = "LocationsSelected"
        val ARG_CLASSES_SET = "ClassesSelected"
        val ARG_ACTOR_SET = "ActorsSet"
        val ARG_PEOPLE_SET = "PeopleSet"

        val ARG_SHOW_THERSHOLD = "ArgShowthreshold"
        val ARG_SHOW_RADIO_GROUP = "ArgShowRadioGroup"
        val ARG_SHOW_GENDER_DISAGGREGATE = "ArgShowGenderDisaggregate"
        val ARG_SHOW_CLAZZES = "ArgShowClazzes"
        val ARG_SHOW_LOCATIONS = "ArgShowLocations"

        val ARG_FROM_DATE = "fromDate"
        val ARG_TO_DATE = "toDate"
        val ARG_LOCATION_LIST = "locationList"
        val ARG_CLAZZ_LIST = "clazzList"
        val ARG_GENDER_DISAGGREGATE = "genderDisaggregate"
        val ARG_STUDENT_IDENTIFIER_NUMBER = "studentIdentifierNumber"
        val ARG_STUDENT_IDENTIFIER_PERCENTAGE = "studentIdentifierPercentage"
        val ARG_THRESHOLD_LOW = "thesholdLow"
        val ARG_THRESHOLD_MID = "thresholdMid"
        val ARG_THRESHOLD_HIGH = "thresholdHigh"

        val THRESHOLD_LOW_DEFAULT = 60
        val THRESHOLD_MED_DEFAULT = 70
        val THRESHOLD_HIGH_DEFAULT = 80
    }
}
