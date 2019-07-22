package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptionsPresenter
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.XapiReportOptionsView
import java.util.*


class XapiReportOptionsActivity : UstadBaseActivity(), XapiReportOptionsView,
        /* SelectMultipleLocationTreeDialogFragment.MultiSelectLocationTreeDialogListener,*/
        SelectMultipleEntriesTreeDialogFragment.MultiSelectEntriesTreeDialogListener {


    private lateinit var visualTypeSpinner: Spinner

    private lateinit var yAxisSpinner: Spinner

    private lateinit var xAxisSpinner: Spinner

    private lateinit var subGroupSpinner: Spinner

    private lateinit var didDataAdapter: ArrayAdapter<XLangMapEntryDao.Verb>

    private lateinit var didAutoCompleteView: AutoCompleteTextView

    private lateinit var didFlexBoxLayout: FlexboxLayout

    private lateinit var whoDataAdapter: ArrayAdapter<PersonDao.PersonNameAndUid>

    private lateinit var whoAutoCompleteView: AutoCompleteTextView

    private lateinit var whoFlexBoxLayout: FlexboxLayout

    private lateinit var whenEditText: EditText

    //  private lateinit var whereEditText: EditText

    private lateinit var whatEditText: EditText

    private lateinit var presenter: XapiReportOptionsPresenter

    private lateinit var fromET: EditText

    private lateinit var toET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_options)

        visualTypeSpinner = findViewById(R.id.type_spinner)
        yAxisSpinner = findViewById(R.id.yaxis_spinner)
        xAxisSpinner = findViewById(R.id.xaxis_spinner)
        subGroupSpinner = findViewById(R.id.sub_group_spinner)
        didAutoCompleteView = findViewById(R.id.didAutoCompleteTextView)
        didFlexBoxLayout = findViewById(R.id.didFlex)
        whoAutoCompleteView = findViewById(R.id.whoAutoCompleteTextView)
        whoFlexBoxLayout = findViewById(R.id.whoFlex)
        whenEditText = findViewById(R.id.whenEditText)
        // whereEditText = findViewById(R.id.whereEditText)
        whatEditText = findViewById(R.id.whatEditText)

        whenEditText.setOnClickListener {
            createDateRangeDialog().show()
        }

        setUMToolbar(R.id.new_report_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        umToolbar.title = "Report Options"

        presenter = XapiReportOptionsPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        /*  whereEditText.setOnClickListener {
              presenter.handleWhereClicked()
          }
  */
        whatEditText.setOnClickListener {
            presenter.handleWhatClicked()
        }

        whoDataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, listOf<PersonDao.PersonNameAndUid>())
        whoAutoCompleteView.setAdapter(whoDataAdapter)
        whoAutoCompleteView.addTextChangedListener(textWatcher)
        whoAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            whoAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as PersonDao.PersonNameAndUid
            addChipToDidFlexLayout(selected.name, whoFlexBoxLayout, whoFlexBoxLayout.childCount - 1, selected.personUid)
        }

        didDataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, listOf<XLangMapEntryDao.Verb>())
        didAutoCompleteView.setAdapter(didDataAdapter)
        didAutoCompleteView.addTextChangedListener(textWatcher)
        didAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            didAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as XLangMapEntryDao.Verb
            addChipToDidFlexLayout(selected.valueLangMap, didFlexBoxLayout, didFlexBoxLayout.childCount - 1, selected.verbLangMapUid)
        }

        yAxisSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.handleSelectedYAxis(position)
            }

        })

        visualTypeSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.handleSelectedChartType(position)
            }

        })

        xAxisSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.handleSelectedXAxis(position)
            }

        })

        subGroupSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.handleSelectedSubGroup(position)
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }


    private fun setAdapterForSpinner(list: List<String>, spinner: Spinner) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter
    }


    override fun fillVisualChartType(translatedGraphList: List<String>) {
        setAdapterForSpinner(translatedGraphList, visualTypeSpinner)
    }

    override fun fillYAxisData(translatedYAxisList: List<String>) {
        setAdapterForSpinner(translatedYAxisList, yAxisSpinner)
    }

    override fun fillXAxisAndSubGroupData(translatedXAxisList: List<String>) {
        setAdapterForSpinner(translatedXAxisList, xAxisSpinner)
        setAdapterForSpinner(translatedXAxisList, subGroupSpinner)
    }

    override fun updateWhoDataAdapter(whoList: List<PersonDao.PersonNameAndUid>) {
        whoDataAdapter.clear()
        whoDataAdapter.addAll(whoList)
        whoDataAdapter.notifyDataSetChanged()
    }

    override fun updateDidDataAdapter(didList: List<XLangMapEntryDao.Verb>) {
        didDataAdapter.clear()
        didDataAdapter.addAll(didList)
        didDataAdapter.notifyDataSetChanged()
    }

    override fun updateFromDialogText(fromDate: String) {
        fromET.setText(fromDate)
    }

    override fun updateToDialogText(toDate: String) {
        toET.setText(toDate)
    }

    override fun updateWhenRangeText(rangeText: String) {
        whenEditText.setText(rangeText)
    }

    override fun updateChartTypeSelected(indexChart: Int) {
        visualTypeSpinner.setSelection(indexChart)
    }

    override fun updateYAxisTypeSelected(indexYAxis: Int) {
        yAxisSpinner.setSelection(indexYAxis)
    }

    override fun updateXAxisTypeSelected(indexXAxis: Int) {
        xAxisSpinner.setSelection(indexXAxis)
    }

    override fun updateSubgroupTypeSelected(indexSubgroup: Int) {
        subGroupSpinner.setSelection(indexSubgroup)
    }

    override fun updateWhoListSelected(personList: List<PersonDao.PersonNameAndUid>) {
        personList.forEach {
            addChipToDidFlexLayout(it.name, whoFlexBoxLayout, whoFlexBoxLayout.childCount - 1, it.personUid)
        }
    }

    override fun updateDidListSelected(verbs: List<XLangMapEntryDao.Verb>) {
        verbs.forEach {
            addChipToDidFlexLayout(it.valueLangMap, didFlexBoxLayout, didFlexBoxLayout.childCount - 1, it.verbLangMapUid)
        }
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
            if (hash == whoAutoCompleteView.text.hashCode()) {
                val name = whoAutoCompleteView.text.toString()
                presenter.handleWhoDataTyped(name, whoFlexBoxLayout.children.filter {
                    it is Chip
                }.map {
                    (it as Chip).tag as Long
                }.toList())

            } else if (hash == didAutoCompleteView.text.hashCode()) {
                val verb = didAutoCompleteView.text.toString()
                presenter.handleDidDataTyped(verb, didFlexBoxLayout.children.filter {
                    it is Chip
                }.map {
                    (it as Chip).tag as Long
                }.toList())
            }
        }


        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }


    private fun addChipToDidFlexLayout(text: String, flexGroup: FlexboxLayout, count: Int, uid: Long) {
        val chip = LayoutInflater.from(this).inflate(R.layout.view_chip, flexGroup, false) as Chip
        chip.text = text
        chip.tag = uid
        flexGroup.addView(chip, count)
        chip.setOnCloseIconClickListener {
            flexGroup.removeView(chip as View)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                presenter.handleViewReportPreview(
                        didFlexBoxLayout.children.filter { it is Chip }.map {
                            (it as Chip).tag as Long
                        }.toList(),
                        whoFlexBoxLayout.children.filter { it is Chip }.map {
                            (it as Chip).tag as Long
                        }.toList())
                return true
            }

        }
        return true
    }

    private fun createDateRangeDialog(): Dialog {

        val inflater = Objects.requireNonNull(getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)) as LayoutInflater

        val rootView = inflater.inflate(R.layout.fragment_select_date_range_dialog, null)

        fromET = rootView.findViewById(R.id.fragment_select_daterange_dialog_from_time)
        toET = rootView.findViewById(R.id.fragment_select_daterange_dialog_to_time)

        presenter.handleToCalendarSelected()
        presenter.handleFromCalendarSelected()

        //TO:
        //Date pickers's on click listener - sets text
        val toDateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            presenter.handleToCalendarSelected(year, month + 1, dayOfMonth)
        }

        //Default view: not focusable.
        toET.isFocusable = false

        //date listener - opens a new date picker.
        var dateFieldPicker = DatePickerDialog(
                this, toDateListener, presenter.toDateTime.yearInt,
                presenter.toDateTime.month0, presenter.toDateTime.dayOfMonth)

        dateFieldPicker = hideYearFromDatePicker(dateFieldPicker)

        //Set onclick listener
        val finalDateFieldPicker = dateFieldPicker
        toET.setOnClickListener { finalDateFieldPicker.show() }

        //FROM:
        //Date pickers's on click listener - sets text
        val fromDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            presenter.handleFromCalendarSelected(year, month + 1, dayOfMonth)
        }

        //Default view: not focusable.
        fromET.isFocusable = false

        //date listener - opens a new date picker.
        var fromDateFieldPicker = DatePickerDialog(
                this, fromDateListener, presenter.fromDateTime.yearInt,
                presenter.fromDateTime.month0, presenter.fromDateTime.dayOfMonth)

        fromDateFieldPicker = hideYearFromDatePicker(fromDateFieldPicker)

        val finalFromDateFieldPicker = fromDateFieldPicker
        fromET.setOnClickListener { finalFromDateFieldPicker.show() }

        val positiveOCL = DialogInterface.OnClickListener { dialog, _ ->
            presenter.handleDateRangeSelected()
            dialog.cancel()
            presenter
        }

        val negativeOCL = DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.date_range)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.ok, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)

        return builder.create()
    }

//TODO when varuna branch merges
/*  override fun onLocationResult(selected: MutableMap<String, Long>) {
      var locationList = selected.keys.joinToString { it }
      runOnUiThread {
          whereEditText.setText(locationList)
      }
      presenter.handleLocationListSelected(selected.values.toList())
  }*/

    override fun onEntriesSelectedResult(selected: MutableMap<String, Long>) {
        var entriesList = selected.keys.joinToString { it }
        runOnUiThread {
            whatEditText.setText(entriesList)
        }
        presenter.handleEntriesListSelected(selected.values.toList())
    }


    private fun hideYearFromDatePicker(dateFieldPicker: DatePickerDialog): DatePickerDialog {
        try {
            val f = dateFieldPicker.javaClass.declaredFields
            for (field in f) {
                if (field.name == "mYearPicker" || field.name == "mYearSpinner"
                        || field.name == "mCalendarView") {
                    field.isAccessible = true
                    val yearPicker: Any = field.get(dateFieldPicker)
                    (yearPicker as View).visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dateFieldPicker
    }
}


