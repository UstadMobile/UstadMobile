package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_HIGH_DEFAULT
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_LOW_DEFAULT
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_MED_DEFAULT
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * The ReportEdit activity.
 *
 *
 * This Activity extends UstadBaseActivity and implements ReportEditView
 */
class ReportEditActivity : UstadBaseActivity(), ReportEditView,
        SelectClazzesDialogFragment.ClazzSelectDialogListener,
        SelectMultipleTreeDialogFragment.MultiSelectTreeDialogListener,
        SelectAttendanceThresholdsDialogFragment.ThresholdsSelectedDialogListener,
        SelectTwoDatesDialogFragment.CustomTimePeriodDialogListener {



    private lateinit var locationsTextView: TextView
    private lateinit var locationsHeadingTextView: TextView
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var heading: TextView
    private lateinit var genderDisaggregateCheck: CheckBox
    private lateinit var mPresenter: ReportEditPresenter
    private lateinit var classesTextView: TextView
    private lateinit var classesHeadingTextView: TextView
    private lateinit var attendanceThresholdHeadingTextView: TextView
    private lateinit var attendanceThresholdsTextView: TextView
    private lateinit var studentNumberOrPercentageRadioGroup: RadioGroup

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_edit)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_report_edit_toolbar)
        toolbar.setTitle(R.string.choose_report_options)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        locationsTextView = findViewById(R.id.activity_report_edit_location_detail)
        timePeriodSpinner = findViewById(R.id.activity_report_edittime_period_spinner)
        locationsHeadingTextView = findViewById(R.id.activity_report_edit_location_heading)
        classesHeadingTextView = findViewById(R.id.activity_report_edit_classes_heading)

        heading = findViewById(R.id.activity_report_edit_report_title)
        genderDisaggregateCheck = findViewById(R.id.activity_report_edit_gender)
        studentNumberOrPercentageRadioGroup = findViewById(R.id.activity_report_edit_show_student_radio_options)

        classesTextView = findViewById(R.id.activity_report_classes_textview)
        attendanceThresholdsTextView = findViewById(R.id.activity_report_edit_attendance_threshold_selector)

        attendanceThresholdHeadingTextView = findViewById(R.id.activity_report_edit_attendance_thresholds_heading)

        updateClassesIfEmpty()
        updateLocationIfEmpty()

        //Call the Presenter
        mPresenter = ReportEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Set Default threshold value
        setDefaultThresholdValues()

        studentNumberOrPercentageRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.activity_report_edit_show_student_number_option) {
                mPresenter.setStudentNumbers(true)
                mPresenter.setStudentPercentages(false)
            } else if (checkedId == R.id.activity_report_edit_show_student_percentage_option) {
                mPresenter.setStudentPercentages(true)
                mPresenter.setStudentNumbers(false)
            } else {
                mPresenter.setStudentPercentages(false)
                mPresenter.setStudentNumbers(false)
            }
        }

        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter.handleTimePeriodSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        classesTextView.setOnClickListener { v -> mPresenter.goToSelectClassesDialog() }

        locationsTextView.setOnClickListener { v -> mPresenter.goToLocationDialog() }

        genderDisaggregateCheck.setOnCheckedChangeListener { buttonView, isChecked -> mPresenter.setGenderDisaggregated(isChecked) }

        attendanceThresholdsTextView.setOnClickListener { v -> mPresenter.goToSelectAttendanceThresholdsDialog() }

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_edit_fab)
        fab.setOnClickListener { v -> mPresenter.handleClickPrimaryActionButton() }

    }

    fun updateClassesIfEmpty() {
        updateClazzesSelected(getText(R.string.all).toString())
        updateLocationsSelected(getText(R.string.all).toString())
    }

    fun updateLocationIfEmpty() {
        updateClazzesSelected(getText(R.string.all).toString())
        updateLocationsSelected(getText(R.string.all).toString())
    }

    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun populateTimePeriod(options: HashMap<Int, String>) {

        val timePeriodPresets = options.values.toTypedArray()
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, timePeriodPresets)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter
    }

    override fun updateGenderDisaggregationSet(byGender: Boolean) {
        genderDisaggregateCheck.isChecked = byGender
    }

    override fun updateReportName(name: String) {
        heading.text = name
    }

    override fun showCustomDateSelector() {

    }

    override fun updateThresholdSelected(thresholdString: String) {
        attendanceThresholdsTextView.text = thresholdString
    }

    override fun updateLocationsSelected(locations: String) {
        locationsTextView.text = locations
        if (locations == "") {
            updateLocationIfEmpty()
        }
    }

    override fun updateClazzesSelected(clazzes: String) {
        classesTextView.text = clazzes
        if (clazzes == "") {
            updateClassesIfEmpty()
        }
    }

    override fun showAttendanceThresholdView(show: Boolean) {
        attendanceThresholdHeadingTextView.visibility = if (show) View.VISIBLE else View.GONE
        attendanceThresholdsTextView.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.activity_report_edit_hline2).visibility = if (show) View.VISIBLE else View.GONE

    }

    override fun showShowStudentNumberPercentageView(show: Boolean) {
        studentNumberOrPercentageRadioGroup.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showGenderDisaggregate(show: Boolean) {
        genderDisaggregateCheck.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showClazzes(show: Boolean) {
        classesTextView.visibility = if (show) View.VISIBLE else View.GONE
        classesHeadingTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showLocations(show: Boolean) {
        locationsTextView.visibility = if (show) View.VISIBLE else View.GONE
        locationsHeadingTextView.visibility = if (show) View.VISIBLE else View.GONE
    }


    override fun onSelectClazzesResult(selectedClazzes: HashMap<String, Long>?) {
        val classesSelectedString = StringBuilder()
        val selectedClazzesNameIterator = selectedClazzes!!.keys.iterator()
        while (selectedClazzesNameIterator.hasNext()) {
            classesSelectedString.append(selectedClazzesNameIterator.next())
            if (selectedClazzesNameIterator.hasNext()) {
                classesSelectedString.append(", ")
            }
        }
        val selectedClassesList = ArrayList(selectedClazzes.values)
        mPresenter.setSelectedClasses(selectedClassesList)

        updateClazzesSelected(classesSelectedString.toString())
    }

    override fun onLocationResult(selectedLocations: HashMap<String, Long>) {
        val selectedLocationsNameIterator = selectedLocations.keys.iterator()
        val locationsSelectedString = StringBuilder()
        while (selectedLocationsNameIterator.hasNext()) {
            locationsSelectedString.append(selectedLocationsNameIterator.next())
            if (selectedLocationsNameIterator.hasNext()) {
                locationsSelectedString.append(", ")
            }
        }
        val selectedLocationList = ArrayList(selectedLocations.values)
        mPresenter.setSelectedLocations(selectedLocationList)

        updateLocationsSelected(locationsSelectedString.toString())
    }

    /**
     * Sets default value at start only
     */
    fun setDefaultThresholdValues() {
        val defaultValue = ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues()
        defaultValue.low = THRESHOLD_LOW_DEFAULT
        defaultValue.med = THRESHOLD_MED_DEFAULT
        defaultValue.high = THRESHOLD_HIGH_DEFAULT

        onThresholdResult(defaultValue)
    }

    override fun onThresholdResult(values: ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues?) {
        val thresholdString = values!!.low.toString() + "%, " + values.med + "%, " +
                values.high +"%"
        mPresenter.setThresholdValues(values!!)
        updateThresholdSelected(thresholdString)
    }

    override fun onCustomTimesResult(from: Long, to: Long) {
        mPresenter.fromTime = from
        mPresenter.toTime = to

        Toast.makeText(
                applicationContext,
                "Custom date from : $from to $to",
                Toast.LENGTH_SHORT
        ).show()

    }
}
