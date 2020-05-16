package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_ACTOR_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_CLASS_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_FROM_TIME
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_LOCATION_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_PEOPLE_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_TO_TIME
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_ACTOR_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLASSES_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_PEOPLE_SET


/**
 * Presenter for AuditLogSelection view
 */
class AuditLogSelectionPresenter(context: Any, arguments: Map<String, String>?, view:
AuditLogSelectionView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<AuditLogSelectionView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase

    //Getters and Setters:

    var selectedClasses: List<Long>? = null
        set(selectedClasses) {
            var selectedClasses = selectedClasses
            if (selectedClasses == null) {
                selectedClasses = ArrayList()
            }
            field = selectedClasses
        }
    var selectedLocations: List<Long>? = null
        set(selectedLocations) {
            var selectedLocations = selectedLocations
            if (selectedLocations == null) {
                selectedLocations = ArrayList()
            }
            field = selectedLocations
        }
    var selectedPeople: List<Long>? = null
        set(selectedPeople) {
            var selectedPeople = selectedPeople
            if (selectedPeople == null) {
                selectedPeople = ArrayList()
            }
            field = selectedPeople
        }
    var selectedActors: List<Long>? = null
        set(selectedActors) {
            var selectedActors = selectedActors
            if (selectedActors == null) {
                selectedActors = ArrayList()
            }
            field = selectedActors
        }

    var fromTime: Long = 0
    var toTime: Long = 0


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
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

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Update time period options.
        updateTimePeriod()

    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        args.put(ARG_AUDITLOG_FROM_TIME, fromTime.toString())
        args.put(ARG_AUDITLOG_TO_TIME, toTime.toString())

        if (this.selectedClasses != null && !this.selectedClasses!!.isEmpty()) {
            val classesArray = arrayOfNulls<Long>(this.selectedClasses!!.size)
            this.selectedClasses!!.toTypedArray()
            //TODOne: KMP flatten out list to CSVs
            val classesCSV = ArgumentUtil.convertLongListToStringCSV(selectedClasses!!)
            args.put(ARG_AUDITLOG_CLASS_LIST, classesCSV)
        }

        if (this.selectedLocations != null && !this.selectedLocations!!.isEmpty()) {
            val locationsArray = arrayOfNulls<Long>(this.selectedLocations!!.size)
            this.selectedLocations!!.toTypedArray()
            //TODOne: KMP flatten out list to CSVs
            val locationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_AUDITLOG_LOCATION_LIST, locationsCSV)
        }

        if (this.selectedPeople != null && !this.selectedPeople!!.isEmpty()) {
            val peopleArray = arrayOfNulls<Long>(this.selectedPeople!!.size)
            this.selectedPeople!!.toTypedArray()
            //TODOne: KMP flatten out list to CSVs
            val peopleCSV = ArgumentUtil.convertLongListToStringCSV(selectedPeople!!)
            args.put(ARG_AUDITLOG_PEOPLE_LIST, peopleCSV)
        }

        if (this.selectedActors != null && !this.selectedActors!!.isEmpty()) {
            val actorArray = arrayOfNulls<Long>(this.selectedActors!!.size)
            this.selectedActors!!.toTypedArray()
            //TODOne: KMP flatten out list to CSVs
            val actorCSV = ArgumentUtil.convertLongListToStringCSV(selectedActors!!)
            args.put(ARG_AUDITLOG_ACTOR_LIST, actorCSV)
        }

        impl.go(AuditLogListView.VIEW_NAME, args, context)
    }


    fun handleTimePeriodSelected(selected: Int) {
        var selected = selected
        toTime = UMCalendarUtil.getDateInMilliPlusDays(0)
        selected++
        when (selected) {
            TIME_PERIOD_LAST_WEEK -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-7)
            TIME_PERIOD_LAST_TWO_WEEK -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-14)
            TIME_PERIOD_LAST_MONTH -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-31)
            TIME_PERIOD_LAST_THREE_MONTHS -> fromTime = UMCalendarUtil.getDateInMilliPlusDays(-61)
            TIME_PERIOD_CUSTOM -> {
                toTime = 0L
                goToSelectTwoDatesDialog()
            }
        }
    }

    private fun goToSelectTwoDatesDialog() {
        val args = HashMap<String, String>()
        impl.go(SelectTwoDatesDialogView.VIEW_NAME, args, context)
    }

    fun goToSelectClassesDialog() {
        val args = HashMap<String, String>()


        if (this.selectedLocations != null && !this.selectedLocations!!.isEmpty()) {
            val selectedLocationsArray = ReportOverallAttendancePresenter.convertLongList(this.selectedLocations!!)
            //TODOne: KMP flatten out list to CSVs
            val selectedLocationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSV)
        }

        if (this.selectedClasses != null && !this.selectedClasses!!.isEmpty()) {
            val selectedClassesArray = ReportOverallAttendancePresenter.convertLongList(this.selectedClasses!!)
            //TODOne: KMP flatten out list to CSVs
            val selectedClassesCSV = ArgumentUtil.convertLongListToStringCSV(selectedClasses!!)
            args.put(ARG_CLASSES_SET, selectedClassesCSV)
        }
        //impl.go(SelectClazzesDialogView.VIEW_NAME, args, context)
    }

    fun goToLocationDialog() {
        val args = HashMap<String, String>()

        if (this.selectedLocations != null && !this.selectedLocations!!.isEmpty()) {
            val selectedLocationsArray = ReportOverallAttendancePresenter.convertLongList(this.selectedLocations!!)
            //TODOne: KMP flatten out list to CSVs
            val selectedLocationsCSV = ArgumentUtil.convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSV)
        }

        impl.go(SelectMultipleTreeDialogView.VIEW_NAME, args, context)
    }

    fun goToPersonDialog() {

        val args = HashMap<String, String>()

        if (this.selectedPeople != null && !this.selectedPeople!!.isEmpty()) {
            val selectedPeopleArray = ReportOverallAttendancePresenter.convertLongList(this.selectedPeople!!)
            //TODOne: KMP flatten out list to CSVs
            val selectedPeopleCSV = ArgumentUtil.convertLongListToStringCSV(selectedPeople!!)
            args.put(ARG_PEOPLE_SET, selectedPeopleCSV)
        }

        //impl.go(SelectPeopleDialogView.VIEW_NAME, args, context)
    }

    fun goToActorDialog() {

        val args = HashMap<String, String>()

        if (this.selectedActors != null && !this.selectedActors!!.isEmpty()) {
            val selectedPeopleArray = ReportOverallAttendancePresenter.convertLongList(this.selectedActors!!)
            //TODOne: KMP flatten out list to CSVs
            val selectedActorCSV = ArgumentUtil.convertLongListToStringCSV(selectedActors!!)
            args.put(ARG_ACTOR_SET, selectedActorCSV)
        }
        //args.put(ARG_SELECT_ACTOR, "yes")
        //impl.go(SelectPeopleDialogView.VIEW_NAME, args, context)
    }

    companion object {

        private val TIME_PERIOD_LAST_WEEK = 1
        private val TIME_PERIOD_LAST_TWO_WEEK = 2
        private val TIME_PERIOD_LAST_MONTH = 3
        private val TIME_PERIOD_LAST_THREE_MONTHS = 4
        private val TIME_PERIOD_CUSTOM = 5
    }


}
