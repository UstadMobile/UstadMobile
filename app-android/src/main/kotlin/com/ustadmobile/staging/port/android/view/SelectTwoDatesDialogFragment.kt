package com.ustadmobile.staging.port.android.view

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
import com.ustadmobile.core.controller.SelectTwoDatesDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectTwoDatesDialogView
import com.ustadmobile.port.android.view.UstadDialogFragment
import io.reactivex.annotations.NonNull
import java.util.*

/**
 * SelectTwoDatesDialogFragment Android fragment extends UstadBaseFragment
 */
class SelectTwoDatesDialogFragment : UstadDialogFragment(), SelectTwoDatesDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootView: View
    internal lateinit var dialog: AlertDialog

    internal lateinit var mPresenter: SelectTwoDatesDialogPresenter
    var fromDate: Long = 0
    var toDate: Long = 0
    internal lateinit var fromET: EditText
    internal lateinit var toET: EditText

    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    interface CustomTimePeriodDialogListener {
        fun onCustomTimesResult(from: Long, to: Long)
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val currentLocale = resources.configuration.locale

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_two_dates_dialog, null)

        fromET = rootView.findViewById(R.id.fragment_select_two_dates_dialog_from_date_edittext)
        toET = rootView.findViewById(R.id.fragment_select_two_dates_dialog_to_date_edittext)

        fromET.setFocusable(false)
        toET.setFocusable(false)

        mPresenter = SelectTwoDatesDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        val myCalendar = Calendar.getInstance()

        //Date pickers's on click listener - sets text
        val toDate = { view:DatePicker, year:Int, month:Int, dayOfMonth:Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            toDate = myCalendar.timeInMillis
            toET.setText(UMCalendarUtil.getPrettyDateFromLong(myCalendar.timeInMillis,
                    currentLocale))
        }

        //date listener - opens a new date picker.
        val dateFieldPicker = DatePickerDialog(
                mAttachedContext!!, toDate, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
        dateFieldPicker.datePicker.maxDate = System.currentTimeMillis()

        toET.setOnClickListener({ v -> dateFieldPicker.show() })

        //from
        //Date pickers's on click listener - sets text
        val fromDate = { view:DatePicker, year:Int, month:Int, dayOfMonth:Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            fromDate = myCalendar.timeInMillis
            fromET.setText(UMCalendarUtil.getPrettyDateFromLong(myCalendar.timeInMillis,
                    currentLocale))

        }
        //date listener - opens a new date picker.
        val fromDateFieldPicker = DatePickerDialog(
                mAttachedContext!!, fromDate, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
        fromET.setOnClickListener({ v -> fromDateFieldPicker.show() })

        //Dialog's positive / negative listeners :
        val positiveOCL = { dialog: DialogInterface, which:Int -> finish() }

        val negativeOCL = { dialog: DialogInterface, which:Int  -> println("Negative") }


        //Set any view components and its listener (post presenter work)

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.custom_date_range)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()

        return dialog

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        fromDate = 0L
        toDate = 0L
    }

    override fun finish() {
        if (mAttachedContext is CustomTimePeriodDialogListener) {
            (mAttachedContext as CustomTimePeriodDialogListener).onCustomTimesResult(fromDate, toDate)
        }
        dialog.dismiss()
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

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SelectTwoDatesDialogFragment.
         */
        fun newInstance(): SelectTwoDatesDialogFragment {
            val fragment = SelectTwoDatesDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
