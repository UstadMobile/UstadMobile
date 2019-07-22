package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleListSearchPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SaleListSearchView
import com.ustadmobile.core.view.SelectDateRangeDialogView
import java.util.*

class SelectDateRangeDialogFragment : UstadDialogFragment(), AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener, DialogInterface.OnShowListener, View.OnClickListener, SelectDateRangeDialogView, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal var fromDate: Long = 0
    internal var toDate: Long = 0
    internal lateinit var fromET: EditText
    internal lateinit var toET: EditText

    internal lateinit var mPresenter: SaleListSearchPresenter
    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    private var mAttachedContext: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    fun setToDate(toDate: Long) {
        this.toDate = toDate
    }

    fun setFromDate(fromDate: Long) {
        this.fromDate = fromDate
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val myCalendarFrom = Calendar.getInstance()
        val myCalendarTo = Calendar.getInstance()

        assert(inflater != null)

        rootView = inflater.inflate(R.layout.fragment_select_date_range_dialog, null)

        fromET = rootView.findViewById(R.id.fragment_select_daterange_dialog_from_time)
        toET = rootView.findViewById(R.id.fragment_select_daterange_dialog_to_time)

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
                mAttachedContext!!, toDateListener, myCalendarTo.get(Calendar.YEAR),
                myCalendarTo.get(Calendar.MONTH), myCalendarTo.get(Calendar.DAY_OF_MONTH))

        dateFieldPicker = hideYearFromDatePicker(dateFieldPicker)

        //Set onclick listener
        val finalDateFieldPicker = dateFieldPicker
        toET.setOnClickListener { v -> finalDateFieldPicker!!.show() }

        //FROM:
        //Date pickers's on click listener - sets text
        val fromDateListener =
                { _:DatePicker, year:Int, month:Int, dayOfMonth:Int ->
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
                mAttachedContext!!, fromDateListener, myCalendarFrom.get(Calendar.YEAR),
                myCalendarFrom.get(Calendar.MONTH), myCalendarFrom.get(Calendar.DAY_OF_MONTH))

        fromDateFieldPicker = hideYearFromDatePicker(fromDateFieldPicker)

        val finalFromDateFieldPicker = fromDateFieldPicker
        fromET.setOnClickListener { v -> finalFromDateFieldPicker!!.show() }


        val positiveOCL = { dialog: DialogInterface, which:Int ->
            val dateRangeText = UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
                    currentLocale) + " - " + UMCalendarUtil.getPrettyDateSimpleFromLong(toDate,
                    currentLocale)
            mPresenter.handleDateSelected(fromDate, toDate, dateRangeText)
        }

        val negativeOCL = { dialog: DialogInterface, which:Int -> dialog.dismiss() }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.date_range)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog.setOnShowListener(this)

        mPresenter = SaleListSearchPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), (activity as SaleListSearchView?)!!)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        return dialog
    }

    fun hideYearFromDatePicker(dateFieldPicker: DatePickerDialog?): DatePickerDialog? {
        try {
            val f = dateFieldPicker?.javaClass?.declaredFields
            if (f != null) {
                for (field in f) {
                    if (field.name == "mYearPicker" || field.name == "mYearSpinner"
                            || field.name == "mCalendarView") {
                        field.isAccessible = true
                        var yearPicker = Any()
                        yearPicker = field.get(dateFieldPicker)
                        (yearPicker as View).visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dateFieldPicker
    }

    //Required overrides
    override fun finish() {

    }

    override fun onClick(dialog: DialogInterface, which: Int) {

    }

    override fun onShow(dialog: DialogInterface) {

    }

    override fun onClick(v: View) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }
}
