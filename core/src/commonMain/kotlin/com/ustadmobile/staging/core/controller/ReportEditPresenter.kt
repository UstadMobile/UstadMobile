package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLASSES_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_REPORT_DESC
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_REPORT_LINK
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_REPORT_NAME
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_SHOW_CLAZZES
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_SHOW_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_SHOW_LOCATIONS
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_SHOW_RADIO_GROUP
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_SHOW_THERSHOLD
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_NUMBER
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_PERCENTAGE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_HIGH
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_LOW
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_MID
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE


/**
 * The ReportEdit Presenter.
 */
class ReportEditPresenter(context: Any, arguments: Map<String, String>?, view: ReportEditView,
                          val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<ReportEditView>(context, arguments!!, view) {

    private var reportName = ""
    private var reportDesc = ""
    private var reportLink: String? = null
    private var genderDisaggregated = false
    private var studentNumbers = false
    private var studentPercentages = false
    private var selectedClasses: List<Long>? = null
    private var selectedLocations: List<Long>? = null
    private var thresholdValues: ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues? = null
    /**
     * Get the from Time for the report.
     *
     * @return  the set from time.
     */
    /**
     * Set the from time for the report.
     * @param fromTime  the from time in long
     */
    var fromTime: Long = 0
    /**
     * Gets the To time
     * @return  the to time in long
     */
    /**
     * Sets the to time.
     * @param toTime    the to time in long
     */
    var toTime: Long = 0

    private var showThreshold: Boolean = false
    private var showRadioGroup: Boolean = false
    private var showGenderDisaggregated: Boolean = false
    private var showClazzes: Boolean = false
    private var showLocations: Boolean = false


    init {

        if (arguments!!.containsKey(ARG_REPORT_NAME)) {
            reportName = arguments!!.get(ARG_REPORT_NAME)!!.toString()
        }

        if (arguments!!.containsKey(ARG_REPORT_DESC)) {
            reportDesc = arguments!!.get(ARG_REPORT_DESC)!!.toString()
        }

        if (arguments!!.containsKey(ARG_REPORT_LINK)) {
            reportLink = arguments!!.get(ARG_REPORT_LINK)
        }

        if (arguments!!.containsKey(ARG_SHOW_THERSHOLD)) {
            //TODO: Check Kotlin arg to boolean conversion
            showThreshold = arguments!!.get(ARG_SHOW_THERSHOLD)!!.toBoolean()
        }
        if (arguments!!.containsKey(ARG_SHOW_RADIO_GROUP)) {
            //TODO: Check Kotlin arg to boolean conversion
            showRadioGroup = arguments!!.get(ARG_SHOW_RADIO_GROUP)!!.toBoolean()
        }
        if (arguments!!.containsKey(ARG_SHOW_GENDER_DISAGGREGATE)) {
            //TODO: Check Kotlin arg to boolean conversion
            showGenderDisaggregated = arguments!!.get(ARG_SHOW_GENDER_DISAGGREGATE)!!.toBoolean()
        }
        if (arguments!!.containsKey(ARG_SHOW_CLAZZES)) {
            //TODO: Check Kotlin arg to boolean conversion
            showClazzes = arguments!!.get(ARG_SHOW_CLAZZES)!!.toBoolean()
        }
        if (arguments!!.containsKey(ARG_SHOW_LOCATIONS)) {
            //TODO: Check Kotlin arg to boolean conversion
            showLocations = arguments!!.get(ARG_SHOW_LOCATIONS)!!.toBoolean()
        }

    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Update report name on top
        if (reportName.length > 0) {
            view.updateReportName(reportName)
            if (reportDesc.length > 0) {
                view.updateReportName(reportDesc)
            }
        }
        view.showAttendanceThresholdView(showThreshold)
        view.showShowStudentNumberPercentageView(showRadioGroup)
        view.showGenderDisaggregate(showGenderDisaggregated)
        view.showClazzes(showClazzes)
        view.showLocations(showLocations)

        //Update time period options.
        updateTimePeriod()

    }

    /**
     * Generates the time period options. Generates the hashmap and sends it to the view.
     */
    private fun updateTimePeriod() {
        val timePeriodOptions = HashMap<Int, String>()
        timePeriodOptions[TIME_PERIOD_LAST_WEEK] = impl.getString(MessageID.last_week, context)
        timePeriodOptions[TIME_PERIOD_LAST_TWO_WEEK] = impl.getString(MessageID.last_two_weeks, context)
        timePeriodOptions[TIME_PERIOD_LAST_MONTH] = impl.getString(MessageID.last_month, context)
        timePeriodOptions[TIME_PERIOD_LAST_THREE_MONTHS] = impl.getString(MessageID.last_three_months, context)
        timePeriodOptions[TIME_PERIOD_CUSTOM] = impl.getString(MessageID.custom_date_range, context)

        view.populateTimePeriod(timePeriodOptions)

    }

    /**
     * Hanlde the time period drop down selector in the drop-down/list/spinner. This should set the
     * from and to times based on the time of selection that will be used to generate the
     * report that follows. The from and to time paramters are stored in the presenter and
     * sent to the report (via arguments) upon clicking primary action button (Create report)
     *
     * @param selected  The flag of drop - down / spinner item selected.
     */
    fun handleTimePeriodSelected(selected: Int) {
        var selected = selected
        toTime = UMCalendarUtil.getToday235959()
        selected++
        when (selected) {
            TIME_PERIOD_LAST_WEEK -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-7)
            TIME_PERIOD_LAST_TWO_WEEK -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-14)
            TIME_PERIOD_LAST_MONTH -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-31)
            TIME_PERIOD_LAST_THREE_MONTHS -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-61)
            TIME_PERIOD_CUSTOM -> {
                toTime = 0L
                view.showCustomDateSelector()
                goToSelectTwoDatesDialog()
            }
        }
    }

    private fun goToSelectTwoDatesDialog() {
        val args = HashMap<String, String>()
        impl.go(SelectTwoDatesDialogView.VIEW_NAME, args, context)
    }


    /**
     * Goes and opens up the Location dialog
     */
    fun goToLocationDialog() {
        val args = HashMap<String, String>()

        if (selectedLocations != null && !selectedLocations!!.isEmpty()) {
            val selectedLocationsArray = ReportOverallAttendancePresenter.convertLongList(selectedLocations!!)
            //TODOne: Flatted Array to String CSV.
            val selectedLocationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSV)
        }

        impl.go(SelectMultipleTreeDialogView.VIEW_NAME, args, context)
    }

    fun goToSelectClassesDialog() {
        val args = HashMap<String, String>()


        if (selectedLocations != null && !selectedLocations!!.isEmpty()) {
            val selectedLocationsArray = ReportOverallAttendancePresenter.convertLongList(selectedLocations!!)
            //TODOne: Flatted Array to String CSV.
            val selectedLocationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSV)
        }

        if (selectedClasses != null && !selectedClasses!!.isEmpty()) {
            val selectedClassesArray = ReportOverallAttendancePresenter.convertLongList(selectedClasses!!)
            //TODOne: Flatted Array to String CSV.
            val selectedClassesCSV = ArgumentUtil.convertLongListToStringCSV(selectedClasses!!)
            args.put(ARG_CLASSES_SET, selectedClassesCSV)
        }
        //impl.go(SelectClazzesDialogView.VIEW_NAME, args, context)
    }

    fun goToSelectAttendanceThresholdsDialog() {
        val args = HashMap<String, String>()
        if (thresholdValues != null) {
            args.put(ARG_THRESHOLD_LOW, thresholdValues!!.low.toString())
            args.put(ARG_THRESHOLD_MID, thresholdValues!!.med.toString())
            args.put(ARG_THRESHOLD_HIGH, thresholdValues!!.high.toString())
        }
        impl.go(SelectAttendanceThresholdsDialogView.VIEW_NAME, args, context)
    }

    /**
     * Hanldes what happens when you click the "Create Report" button.
     * ie: should go to the report with the info set on this presenter.
     */
    fun handleClickPrimaryActionButton() {

        //Create arguments
        val args = HashMap<String, String>()
        args.put(ARG_REPORT_NAME, reportName)
        args.put(ARG_FROM_DATE, fromTime.toString())
        args.put(ARG_TO_DATE, toTime.toString())

        if (selectedClasses != null && !selectedClasses!!.isEmpty()) {
            selectedClasses!!.toTypedArray()

            val clazzesCSV = ArgumentUtil.convertLongListToStringCSV(selectedClasses!!)
            args.put(ARG_CLAZZ_LIST, clazzesCSV)
        }

        if (selectedLocations != null && !selectedLocations!!.isEmpty()) {
            selectedLocations!!.toTypedArray()

            val locationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATION_LIST, locationsCSV)
        }

        if (thresholdValues != null) {
            args.put(ARG_THRESHOLD_LOW, thresholdValues!!.low.toString())
            args.put(ARG_THRESHOLD_MID, thresholdValues!!.med.toString())
            args.put(ARG_THRESHOLD_HIGH, thresholdValues!!.high.toString())
        }

        args.put(ARG_GENDER_DISAGGREGATE, genderDisaggregated.toString())

        args.put(ARG_STUDENT_IDENTIFIER_NUMBER, studentNumbers.toString())
        args.put(ARG_STUDENT_IDENTIFIER_PERCENTAGE, studentPercentages.toString())

        //Go to report with those arguments / Generate report
        val linkViewName = reportLink!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        impl.go(linkViewName, args, view.viewContext)

    }

    //Getts and Setters:

    fun setStudentNumbers(studentNumbers: Boolean) {
        this.studentNumbers = studentNumbers
    }

    fun setStudentPercentages(studentPercentages: Boolean) {
        this.studentPercentages = studentPercentages
    }

    /**
     * Sets gender disaggregate. Also updates the view.
     *
     * @param genderDisaggregated    true if to set as ticked/checked. false if not.
     */
    fun setGenderDisaggregated(genderDisaggregated: Boolean) {
        this.genderDisaggregated = genderDisaggregated
        view.updateGenderDisaggregationSet(genderDisaggregated)
    }

    fun setSelectedClasses(selectedClasses: List<Long>) {
        this.selectedClasses = selectedClasses
    }

    fun setSelectedLocations(selectedLocations: List<Long>) {
        this.selectedLocations = selectedLocations
    }

    fun setThresholdValues(thresholdValues: ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues) {
        this.thresholdValues = thresholdValues
    }

    companion object {

        private val TIME_PERIOD_LAST_WEEK = 1
        private val TIME_PERIOD_LAST_TWO_WEEK = 2
        private val TIME_PERIOD_LAST_MONTH = 3
        private val TIME_PERIOD_LAST_THREE_MONTHS = 4
        private val TIME_PERIOD_CUSTOM = 5
    }

}
