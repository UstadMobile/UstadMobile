package com.ustadmobile.staging.port.android.view


import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


/**
 * The ClazzEdit activity - responsible for the Class Edit screen activity on Android.
 * The ClazzEdit screen has Schedule recycler view and a DateRange calendar spinner along side
 * EditText for Class name and description.
 *
 * This Activity extends UstadBaseActivity and implements ClazzEditView
 */
class ClazzEditActivity : UstadBaseActivity(), ClazzEditView,
        SelectClazzFeaturesDialogFragment.ClazzFeaturesSelectDialogListener {

    private var toolbar: Toolbar? = null

    private var scheduleRecyclerView: RecyclerView? = null
    private var mPresenter: ClazzEditPresenter? = null

    internal var classNameTIP: TextInputLayout? = null
    internal var classDescTIP: TextInputLayout? = null
    internal var addScheduleButton: Button? = null
    internal var holidaySpinner: Spinner? = null
    internal var timezoneSpinner: Spinner? = null

    internal var featuresTextView: TextView? = null

    internal var customFieldsLL: LinearLayout? = null

    internal var fromET: EditText? = null
    internal var toET: EditText? = null

    private lateinit var locationAutoCompleteView: AutoCompleteTextView
    private lateinit var locationDataAdapter: ArrayAdapter<Location>

    private var mProgressBar: ProgressBar? = null

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        // Handle item selection
        val i = item.itemId
        //If this activity started from other activity
        if (i == R.id.menu_done) {
            handleClickDone()

            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun updateLocationDataAdapter(locations: List<Location>) {

        locationDataAdapter.clear()
        locationDataAdapter.addAll(locations)
        locationDataAdapter.notifyDataSetChanged()
    }

    private var textWatcher = object : TextWatcher {

        private var handler = Handler()
        private val DELAY: Long = 150 // milliseconds
        private var string: Editable? = null

        override fun afterTextChanged(s: Editable?) {
            string = s
            handler.removeCallbacks(myRunnable)
            handler = Handler()
            handler.postDelayed(myRunnable, DELAY)
        }

        var myRunnable = Runnable {
            val hash = string.hashCode()
            if (hash == locationAutoCompleteView.text.hashCode()) {
                val name = locationAutoCompleteView.text.toString()
                mPresenter!!.handleLocationTyped(name)

            }
        }


        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    override fun setInProgress(inProgress: Boolean) {
        mProgressBar!!.visibility = if (inProgress) View.VISIBLE else View.GONE
        disableFields(inProgress)
    }

    private fun disableFields(inProgress: Boolean){
        classNameTIP!!.editText!!.isEnabled=!inProgress
        classDescTIP!!.editText!!.isEnabled=!inProgress
        fromET!!.isEnabled=!inProgress
        toET!!.isEnabled=!inProgress
        locationAutoCompleteView.isEnabled=!inProgress
        timezoneSpinner!!.isEnabled=!inProgress
        holidaySpinner!!.isEnabled=!inProgress

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_clazz_edit)

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_edit_toolbar)
        toolbar!!.setTitle(R.string.class_setup)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar!!.isIndeterminate = true
        mProgressBar!!.scaleY = 3f


        locationAutoCompleteView = findViewById(R.id.locationAutoCompleteTextView)

        //Recycler View:
        scheduleRecyclerView = findViewById(
                R.id.activity_clazz_edit_schedule_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        scheduleRecyclerView!!.layoutManager = mRecyclerLayoutManager

        featuresTextView = findViewById(R.id.activity_clazz_edit_features_selected)

        customFieldsLL = findViewById(R.id.activity_clazz_edit_customfields_ll)

        //Call the Presenter
        mPresenter = ClazzEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))



        locationDataAdapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, listOf<Location>())
        locationAutoCompleteView.setAdapter(locationDataAdapter)
        locationAutoCompleteView.addTextChangedListener(textWatcher)
        locationAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            //locationAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as Location
            mPresenter!!.selectedLocation = selected
            locationAutoCompleteView.setText(selected!!.title)
        }


        fromET = findViewById(R.id.activity_clazz_edit_start_date_edittext)
        toET = findViewById(R.id.activity_clazz_edit_end_date_edittext)

        featuresTextView!!.setOnClickListener { v -> mPresenter!!.handleClickFeaturesSelection() }

        //Clazz Name
        classNameTIP = findViewById(R.id.activity_clazz_edit_name)
        classNameTIP!!.editText!!.addTextChangedListener(object :
                TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateName(s.toString())
            }
        })

        //Clazz Desc
        classDescTIP = findViewById(R.id.activity_clazz_edit_description)
        classDescTIP!!.editText!!.addTextChangedListener(object :
                TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateDesc(s.toString())
            }
        })

        //Add schedule button listener
        addScheduleButton = findViewById(R.id.activity_clazz_edit_add_schedule)
        addScheduleButton!!.setOnClickListener { v -> mPresenter!!.handleClickAddSchedule() }

        //DateRange Spinner (drop-down)
        holidaySpinner = findViewById(R.id.activity_clazz_edit_holiday_calendar_selected)
        holidaySpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                setHolidaySelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        timezoneSpinner = findViewById(R.id.activity_clazz_edit_timezone_spinner)
        timezoneSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                setTimezoneSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //Date preparation
        val myCalendarEnd = Calendar.getInstance()
        val myCalendarStart = Calendar.getInstance()
        val currentLocale = resources.configuration.locale

        //START DATE:
        fromET!!.isFocusable = false

        val startDateListener = { view: DatePicker, year: Int , month:Int , dayOfMonth: Int ->
            myCalendarStart.set(Calendar.YEAR, year)
            myCalendarStart.set(Calendar.MONTH, month)
            myCalendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val startDate = myCalendarStart.timeInMillis
            mPresenter!!.handleUpdateStartTime(startDate)

            if (startDate == 0L) {
                fromET!!.setText("-")
            } else {
                fromET!!.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(startDate,
                        currentLocale))
            }
        }

        //date listener - opens a new date picker.
        val startDatePicker = DatePickerDialog(
                this, startDateListener, myCalendarStart.get(Calendar.YEAR),
                myCalendarStart.get(Calendar.MONTH), myCalendarStart.get(Calendar.DAY_OF_MONTH))

        fromET!!.setOnClickListener { v -> startDatePicker.show() }


        //END DATE:

        toET!!.isFocusable = false

        //Date pickers's on click listener - sets text
        val endDateListener = { view: DatePicker, year: Int, month:Int, dayOfMonth:Int ->
            myCalendarEnd.set(Calendar.YEAR, year)
            myCalendarEnd.set(Calendar.MONTH, month)
            myCalendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val endDate = myCalendarEnd.timeInMillis

            mPresenter!!.handleUpdateEndTime(endDate)

            if (endDate == 0L) {
                toET!!.setText("-")
            } else {
                toET!!.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(endDate,
                        currentLocale))
            }
        }

        //date listener - opens a new date picker.
        val endDatePicker = DatePickerDialog(
                this, endDateListener, myCalendarEnd.get(Calendar.YEAR),
                myCalendarEnd.get(Calendar.MONTH), myCalendarEnd.get(Calendar.DAY_OF_MONTH))

        toET!!.setOnClickListener { v -> endDatePicker.show() }
    }

    private fun handleClickDone() {

        setInProgress(true)

        val customCount = customFieldsLL!!.childCount
        for (i in 0 until customCount) {
            val field = customFieldsLL!!.getChildAt(i)
            val fieldId = field.id
            var type = 0
            var valueObject: Any? = null
            if (field is TextInputLayout) {
                //Text custom field
                type = CustomField.FIELD_TYPE_TEXT
                valueObject = field.editText!!.text.toString()

            } else if (field is LinearLayout) {
                type = CustomField.FIELD_TYPE_DROPDOWN
                val s = field.getChildAt(1) as Spinner
                valueObject = s.selectedItemPosition
            }
            mPresenter!!.handleSaveCustomFieldValues(fieldId, type, valueObject!!)
        }

        val locationEntered = locationAutoCompleteView.text
        mPresenter!!.handleClickDone(locationEntered.toString())
    }

    override fun updateToolbarTitle(titleName: String) {
        toolbar!!.title = titleName
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setTitle(R.string.class_setup)
    }


    override fun setClazzScheduleProvider(factory : DataSource.Factory<Int, Schedule>) {

        val scheduleListRecyclerAdapter = ScheduleRecyclerAdapter(SCHEDULE_DIFF_CALLBACK, applicationContext,
                this, mPresenter!!)

        //Unchecked warning is expected.
        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<Schedule>> { scheduleListRecyclerAdapter.submitList(it) })
        }

        scheduleRecyclerView!!.adapter = scheduleListRecyclerAdapter
    }

    override fun updateLocationSetName(locationName: String) {

        locationAutoCompleteView.setText(locationName)
    }

    override fun updateClazzEditView(updatedClazz: Clazz) {

        var clazzName: String? = ""
        var clazzDesc: String? = ""

        if (updatedClazz != null) {
            if (updatedClazz.clazzName != null) {
                clazzName = updatedClazz.clazzName
            }

            if (updatedClazz.clazzDesc != null) {
                clazzDesc = updatedClazz.clazzDesc
            }
        }

        var featuresText = ""
        if (updatedClazz.isAttendanceFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.attendance)
        }
        if (updatedClazz.isActivityFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.activity_change)
        }
        if (updatedClazz.isSelFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.sel_caps)
        }
        featuresTextView!!.text = featuresText

        val finalClazzName = clazzName
        val finalClazzDesc = clazzDesc
        runOnUiThread {
            classNameTIP!!.editText!!.setText(finalClazzName)
            classDescTIP!!.editText!!.setText(finalClazzDesc)
        }

        var startTimeLong: Long = 0
        var endTimeLong: Long = 0

        startTimeLong = updatedClazz.clazzStartTime
        endTimeLong = updatedClazz.clazzEndTime

        val finalStartTimeLong = startTimeLong
        val finalEndTimeLong = endTimeLong
        runOnUiThread {
            if (finalStartTimeLong == 0L) {
                fromET!!.setText("-")
            } else {
                fromET!!.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        finalStartTimeLong))
            }
            if (finalEndTimeLong == 0L) {
                toET!!.setText("-")
            } else {
                toET!!.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        finalEndTimeLong))
            }
        }

    }

    override fun setHolidayPresets(presets: Array<String>, position: Int) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holidaySpinner!!.adapter = adapter
        holidaySpinner!!.setSelection(position)
    }

    override fun setLocationPresets(presets: Array<String>, position: Int) {
//        val adapter = ArrayAdapter(applicationContext,
//                R.layout.item_simple_spinner, presets)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        locationSpinner!!.adapter = adapter
//        locationSpinner!!.setSelection(position)
    }

    override fun setTimezonePresets(presets: Array<String?>, position: Int){
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timezoneSpinner!!.adapter = adapter
        timezoneSpinner!!.setSelection(position)
    }

    override fun setTimeZonePosition(position: Int) {
        timezoneSpinner!!.setSelection(position)
    }
    /**
     * Handles holiday selected
     * @param position    The id/position of the DateRange selected from the spinner.
     */
    override fun setHolidaySelected(position: Int) {
        mPresenter!!.updateHoliday(position)
    }

    override fun setLocationSelected(position: Int) {
        mPresenter!!.updateLocation(position)
    }

    override fun setTimezoneSelected(position: Int){
        mPresenter!!.updateTimezone(position)
    }

    override fun addCustomFieldText(label: CustomField, value: String) {
        //customFieldsLL

        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //The field is an input type. So we are gonna add a TextInputLayout:
        val fieldTextInputLayout = TextInputLayout(this)
        val viewId = View.generateViewId()
        mPresenter!!.addToMap(viewId, label.customFieldUid)
        fieldTextInputLayout.id = viewId
        //Edit Text is inside a TextInputLayout
        val textInputLayoutParams = LinearLayout.LayoutParams(displayWidth,
                LinearLayout.LayoutParams.MATCH_PARENT)

        val widthWithPadding = displayWidth - dpToPx(28)
        //The EditText
        val fieldEditText = EditText(this)
        fieldEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        val editTextParams = LinearLayout.LayoutParams(
                widthWithPadding,
                ViewGroup.LayoutParams.MATCH_PARENT)
        fieldEditText.layoutParams = editTextParams
        fieldEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        fieldEditText.setText(value)

        fieldEditText.hint = label.customFieldName

        fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams)
        fieldTextInputLayout.setPadding(dpToPx(8), 0, 0, 0)
        customFieldsLL!!.addView(fieldTextInputLayout)
    }

    override fun addCustomFieldDropdown(label: CustomField, options: Array<String>, selected: Int) {
        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //The field is an input type. So we are gonna add a TextInputLayout:
        //Edit Text is inside a TextInputLayout
        val textInputLayoutParams = LinearLayout.LayoutParams(displayWidth,
                LinearLayout.LayoutParams.MATCH_PARENT)

        val widthWithPadding = displayWidth - dpToPx(28)

        //Spinner time
        val spinner = Spinner(this)
        val spinnerArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                options)
        spinner.adapter = spinnerArrayAdapter

        spinner.setSelection(selected)

        //Spinner label
        val labelTV = TextView(this)
        labelTV.text = label.customFieldName

        val viewId = View.generateViewId()
        mPresenter!!.addToMap(viewId, label.customFieldUid)

        //VLL
        val vll = LinearLayout(this)
        vll.id = viewId
        vll.layoutParams = textInputLayoutParams
        vll.orientation = LinearLayout.VERTICAL

        vll.addView(labelTV)
        vll.addView(spinner)

        vll.setPadding(dpToPx(8), 0, 0, 0)
        customFieldsLL!!.addView(vll)
    }

    override fun clearAllCustomFields() {
        customFieldsLL!!.removeAllViews()
    }

    override fun onSelectClazzesFeaturesResult(clazz: Clazz?) {
        featuresTextView!!.text = ""
        var featuresText = ""

        if (clazz!!.isAttendanceFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.attendance)
        }
        if (clazz.isActivityFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.activity_change)
        }
        if (clazz.isSelFeature) {
            var addComma = ""
            if (featuresText != "") {
                addComma = ","
            }
            featuresText = featuresText + addComma + " " + getText(R.string.sel_caps)
        }
        featuresTextView!!.text = featuresText
        mPresenter!!.updateFeatures(clazz)
    }

    companion object {

        // Diff callback.
        val SCHEDULE_DIFF_CALLBACK: DiffUtil.ItemCallback<Schedule> = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
