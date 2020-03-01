package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportOptionsDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportOptionsDetailView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportOptionsDetailActivity : UstadBaseActivity(),
        ReportOptionsDetailView,
        SelectMultipleLocationTreeDialogFragment.MultiSelectLocationTreeDialogListener,
        SelectMultipleProductTypeTreeDialogFragment.MultiSelectProductTypeTreeDialogListener,
        SelectMultiplePeopleFragment.PersonSelectDialogListener {

    private var toolbar: Toolbar? = null
    private var mPresenter: ReportOptionsDetailPresenter? = null

    internal lateinit var productTypesCL: ConstraintLayout
    internal lateinit var groupByCL: ConstraintLayout
    internal lateinit var showAverageCL: ConstraintLayout
    internal lateinit var lesCL: ConstraintLayout
    internal lateinit var locationCL: ConstraintLayout
    internal lateinit var dateRangeCL: ConstraintLayout
    internal lateinit var salesPriceCL : ConstraintLayout
    internal lateinit var productTypesTV: TextView
    internal lateinit var lesHeading : TextView
    internal lateinit var lesTV: TextView
    internal lateinit var locationTV: TextView
    internal lateinit var dateRangeTV: TextView
    internal lateinit var salesPriceTV: TextView
    internal lateinit var groupBySpinner: Spinner
    internal lateinit var rangeSeek: CrystalRangeSeekbar
    internal lateinit var showAverageCB: CheckBox

    internal var menu: Menu? = null
    internal var editMode = false

    private var fromDate: Long = 0
    private var toDate: Long = 0
    internal lateinit var dialog: AlertDialog

    internal lateinit var groupByPresets: Array<String?>

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param thisMenu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(thisMenu: Menu): Boolean {
        menu = thisMenu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_report, menu)

        menu!!.findItem(R.id.menu_create_report).isVisible = true
        if (editMode) {
            menu!!.findItem(R.id.menu_create_report).setTitle(R.string.save)
        } else {
            menu!!.findItem(R.id.menu_create_report).setTitle(R.string.create_report)
        }
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        } else if (i == R.id.menu_create_report) {
            mPresenter!!.handleClickCreateReport()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showLEsOption(show: Boolean){
        if(show){
            lesTV.visibility = View.VISIBLE
            lesHeading.visibility = View.VISIBLE
        }else{
            lesTV.visibility = View.GONE
            lesHeading.visibility = View.GONE
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_options_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_options_detail_toolbar)
        toolbar!!.title = getText(R.string.report_options)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        productTypesCL = findViewById(R.id.activity_report_options_detail_product_types_cl)
        groupByCL = findViewById(R.id.activity_report_options_detail_group_by_cl)
        showAverageCL = findViewById(R.id.activity_report_options_detail_show_average_cl)
        lesCL = findViewById(R.id.activity_report_options_detail_les_cl)
        locationCL = findViewById(R.id.activity_report_options_detail_location_cl)
        dateRangeCL = findViewById(R.id.activity_report_options_detail_date_range_cl)
        rangeSeek = findViewById(R.id.activity_report_options_detail_sales_price_rangeseekcustom)
        showAverageCB = findViewById(R.id.activity_report_options_detail_show_average_cb)
        salesPriceCL = findViewById(R.id.activity_report_options_detail_sales_price_cl)


        productTypesTV = findViewById(R.id.activity_report_options_detail_product_types_value)
        groupBySpinner = findViewById(R.id.activity_report_options_detail_group_by_value)
        lesTV = findViewById(R.id.activity_report_options_detail_les_value)
        lesHeading = findViewById(R.id.activity_report_options_detail_les_heading)
        locationTV = findViewById(R.id.activity_report_options_detail_location_value)
        dateRangeTV = findViewById(R.id.activity_report_options_detail_date_range_value)
        salesPriceTV = findViewById(R.id.activity_report_options_detail_sales_price_value)

        //Sales price based on range seeker
        rangeSeek.setMaxValue(100000f)
        rangeSeek.setMinValue(0f)


        //Call the Presenter
        mPresenter = ReportOptionsDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Views

        rangeSeek.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            mPresenter!!.setFromPrice(minValue.toInt())
            mPresenter!!.setToPrice(maxValue.toInt())
            mPresenter!!.updateSalePriceRangeOnView()

        }

        //Date range
        dateRangeCL.setOnClickListener { v -> showDateRangeDialog() }

        //Group by
        groupBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeGroupBy(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //Product type
        productTypesCL.setOnClickListener { v -> mPresenter!!.goToProductSelect() }

        //LEs
        lesCL.setOnClickListener { v -> mPresenter!!.goToLEsSelect() }

        //Average:
        showAverageCB.setOnCheckedChangeListener { buttonView, isChecked -> mPresenter!!.handleToggleAverage(isChecked) }

        //Location
        locationCL.setOnClickListener { v -> mPresenter!!.goToLocationSelect() }


    }

    private fun showDateRangeDialog() {
        val rangeDialog = createDateRangeDialog()
        rangeDialog.show()
    }


    override fun setTitle(title: String) {
        runOnUiThread { toolbar!!.title = title }
    }

    override fun setShowAverage(showAverage: Boolean) {
        runOnUiThread { showAverageCB.isChecked = showAverage }
    }

    override fun setLocationSelected(locationSelected: String) {
        runOnUiThread { locationTV.text = locationSelected }
    }

    override fun setLESelected(leSelected: String) {
        runOnUiThread { lesTV.text = leSelected }
    }

    override fun setProductTypeSelected(productTypeSelected: String) {
        runOnUiThread { productTypesTV.text = productTypeSelected }
    }

    override fun setDateRangeSelectedLongs(fromDate: Long, toDate: Long) {
        val locale = Locale(UstadMobileSystemImpl.instance.getLocale(this))
        val formatter = SimpleDateFormat("dd/MMM/yyyy", locale)
        val dateRangeText = formatter.format(fromDate) + " - " + formatter.format(toDate)
        runOnUiThread{ dateRangeTV.text = dateRangeText}
    }

    override fun setSalePriceRangeSelected(from: Int, to: Int, salePriceSelected: String) {
        val locale = Locale(UstadMobileSystemImpl.instance.getLocale(this))
        val fromL = NumberFormat.getInstance(locale).format(from)
        val toL = NumberFormat.getInstance(locale).format(to)
        val currency = getText(R.string.currency_afs)
        val rangeText = getText(R.string.from).toString() + " " +
                fromL + " " + currency + "-" + toL + " " + currency
        runOnUiThread { salesPriceTV.text = rangeText }
    }

    override fun setGroupByPresets(presets: Array<String?>, selectedPosition: Int) {
        this.groupByPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner, groupByPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        runOnUiThread {
            groupBySpinner.adapter = adapter
            if (selectedPosition > 0) {
                groupBySpinner.setSelection(selectedPosition)
            }
        }
    }

    override fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        if (menu != null) {
            menu!!.findItem(R.id.menu_create_report).setTitle(if (editMode) R.string.save else R.string.create_report)
        }
    }

    fun setToDate(toDate: Long) {
        this.toDate = toDate
    }

    fun setFromDate(fromDate: Long) {
        this.fromDate = fromDate
    }

    private fun createDateRangeDialog(): Dialog {

        val inflater = Objects.requireNonNull(getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)) as LayoutInflater
        val myCalendarFrom = Calendar.getInstance()
        val myCalendarTo = Calendar.getInstance()

        assert(inflater != null)

        val rootView = inflater.inflate(R.layout.fragment_select_date_range_dialog, null)

        val fromET = rootView.findViewById<EditText>(R.id.fragment_select_daterange_dialog_from_time)
        val toET = rootView.findViewById<EditText>(R.id.fragment_select_daterange_dialog_to_time)

        val currentLocale = resources.configuration.locale

        //TO:
        //Date pickers's on click listener - sets text
        val toDateListener =
                { view: DatePicker, year:Int, month:Int, dayOfMonth:Int ->
            myCalendarTo.set(Calendar.YEAR, year)
            myCalendarTo.set(Calendar.MONTH, month)
            myCalendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            setToDate(myCalendarTo.timeInMillis)

            toET.setText(UMCalendarUtil.getPrettyDateSimpleFromLong(toDate,
                    currentLocale))
        }

        //Default view: not focusable.
        toET.isFocusable = false

        //date listener - opens a new date picker.
        var dateFieldPicker: DatePickerDialog? = DatePickerDialog(
                this, toDateListener, myCalendarTo.get(Calendar.YEAR),
                myCalendarTo.get(Calendar.MONTH), myCalendarTo.get(Calendar.DAY_OF_MONTH))

        dateFieldPicker = hideYearFromDatePicker(dateFieldPicker!!)

        //Set onclick listener
        val finalDateFieldPicker = dateFieldPicker
        toET.setOnClickListener { v -> finalDateFieldPicker!!.show() }

        //FROM:
        //Date pickers's on click listener - sets text
        val fromDateListener = { view:DatePicker, year:Int, month:Int, dayOfMonth:Int ->
            myCalendarFrom.set(Calendar.YEAR, year)
            myCalendarFrom.set(Calendar.MONTH, month)
            myCalendarFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            setFromDate(myCalendarFrom.timeInMillis)

            fromET.setText(UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
                    currentLocale))

        }

        //Default view: not focusable.
        fromET.isFocusable = false

        //date listener - opens a new date picker.
        var fromDateFieldPicker: DatePickerDialog? = DatePickerDialog(
                this, fromDateListener, myCalendarFrom.get(Calendar.YEAR),
                myCalendarFrom.get(Calendar.MONTH), myCalendarFrom.get(Calendar.DAY_OF_MONTH))

        fromDateFieldPicker = hideYearFromDatePicker(fromDateFieldPicker!!)

        val finalFromDateFieldPicker = fromDateFieldPicker
        fromET.setOnClickListener { v -> finalFromDateFieldPicker!!.show() }

        val positiveOCL =
                { dialog:DialogInterface, which:Int ->
            mPresenter!!.setFromDate(fromDate)
            mPresenter!!.setToDate(toDate)
            mPresenter!!.updateDateRangeOnView()
        }

        val negativeOCL =
                { dialog: DialogInterface, which:Int -> dialog.dismiss() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.date_range)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()

        return dialog
    }

    private fun hideYearFromDatePicker(dateFieldPicker: DatePickerDialog): DatePickerDialog? {
        try {
            val f = dateFieldPicker.javaClass.declaredFields
            for (field in f) {
                if (field.name == "mYearPicker" || field.name == "mYearSpinner"
                        || field.name == "mCalendarView") {
                    field.isAccessible = true
                    val yearPicker: Any
                    yearPicker = field.get(dateFieldPicker)
                    (yearPicker as View).visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dateFieldPicker
    }


    override fun onLocationResult(selectedLocations: MutableMap<String, Long>) {
        val selectedLocationsNameIterator = selectedLocations.keys.iterator()
        val locationsSelectedString = StringBuilder()
        while (selectedLocationsNameIterator.hasNext()) {
            locationsSelectedString.append(selectedLocationsNameIterator.next())
            if (selectedLocationsNameIterator.hasNext()) {
                locationsSelectedString.append(", ")
            }
        }
        val selectedLocationList = ArrayList(selectedLocations.values)
        mPresenter!!.setSelectedLocations(selectedLocationList)

        setLocationSelected(locationsSelectedString.toString())
    }

    override fun onProductTypesResult(selectedProductTypes: HashMap<String, Long>) {
        val selectedProductTypesNameIterator = selectedProductTypes.keys.iterator()
        val productTypesSelectedString = StringBuilder()
        while (selectedProductTypesNameIterator.hasNext()) {
            productTypesSelectedString.append(selectedProductTypesNameIterator.next())
            if (selectedProductTypesNameIterator.hasNext()) {
                productTypesSelectedString.append(", ")
            }
        }
        val selectedProductTypeList = ArrayList(selectedProductTypes.values)
        mPresenter!!.setSelectedProducts(selectedProductTypeList)

        setProductTypeSelected(productTypesSelectedString.toString())
    }

    override fun onSelectPeopleListener(selected: HashMap<String, Long>?, actor: Boolean) {
        val selectedPeopleIterator = selected!!.keys.iterator()
        val peopleSelectedString = StringBuilder()
        while (selectedPeopleIterator.hasNext()) {
            peopleSelectedString.append(selectedPeopleIterator.next())
            if (selectedPeopleIterator.hasNext()) {
                peopleSelectedString.append(", ")
            }
        }
        val selectedPeopleList = ArrayList(selected.values)
        mPresenter!!.setSelectedLEs(selectedPeopleList)

        setLESelected(peopleSelectedString.toString())

        setProductTypeSelected(peopleSelectedString.toString())
    }
}
