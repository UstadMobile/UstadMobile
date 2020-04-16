package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_HIGH
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_LOW
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_MID
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_HIGH_DEFAULT
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_LOW_DEFAULT
import com.ustadmobile.core.view.ReportEditView.Companion.THRESHOLD_MED_DEFAULT
import com.ustadmobile.core.view.SelectAttendanceThresholdsDialogView
import com.ustadmobile.port.android.view.UstadDialogFragment

/**
 * SelectAttendanceThresholdsDialogFragment Android fragment extends UstadBaseFragment
 */
class SelectAttendanceThresholdsDialogFragment : UstadDialogFragment(),
        SelectAttendanceThresholdsDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener, View.OnClickListener,
        DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootView: View
    internal lateinit var dialog: AlertDialog

    internal lateinit var lowNumberPicker: NumberPicker
    internal lateinit var midNumberPicker: NumberPicker
    internal lateinit var highNumberPicker: NumberPicker

    //Context (Activity calling this)
    private var mAttachedContext: Context? = null

    private var selectedValues: ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues? = null

    //Activity should implement this.
    interface ThresholdsSelectedDialogListener {
        fun onThresholdResult(values: ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues?)
    }

    fun setUpNP(np: NumberPicker, setVal: Int?) {

        np.minValue = 1
        np.maxValue = 100

        np.wrapSelectorWheel = false

        val nums = arrayOfNulls<String>(101)
        for (i in 1 until nums.size)
            nums[i] = Integer.toString(i)

        np.value = setVal!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_attendance_thresholds_dialog, null)

        lowNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_low)
        midNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_medium)
        highNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_high)

        assert(arguments != null)
        if (arguments!!.containsKey(ARG_THRESHOLD_LOW)) {
            setUpNP(lowNumberPicker, arguments!!.get(ARG_THRESHOLD_LOW).toString().toInt())
        } else {
            setUpNP(lowNumberPicker, THRESHOLD_LOW_DEFAULT)
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_MID)) {
            setUpNP(midNumberPicker, arguments!!.get(ARG_THRESHOLD_MID).toString().toInt())
        } else {
            setUpNP(midNumberPicker, THRESHOLD_MED_DEFAULT)
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_HIGH)) {
            setUpNP(highNumberPicker, arguments!!.get(ARG_THRESHOLD_HIGH).toString().toInt())
        } else {
            setUpNP(highNumberPicker, THRESHOLD_HIGH_DEFAULT)
        }


        //Dialog stuff:
        //Dialog's positive / negative listeners :
        val positiveOCL = { dialog:DialogInterface, which:Int->

            selectedValues!!.low = lowNumberPicker.value
            selectedValues!!.med = midNumberPicker.value
            selectedValues!!.high = highNumberPicker.value

            finish()
        }


        val negativeOCL = { dialog:DialogInterface, which:Int -> selectedValues = null }

        //Set any view components and its listener (post presenter work)


        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.select_attendance_thresholds)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.done, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mAttachedContext = context
        selectedValues = ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues()
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
        selectedValues = null
    }

    override fun finish() {
        if (mAttachedContext is ThresholdsSelectedDialogListener) {
            (mAttachedContext as ThresholdsSelectedDialogListener).onThresholdResult(selectedValues)
        }
        dialog.dismiss()
    }

    override fun onClick(v: View) {

    }

    override fun onClick(dialog: DialogInterface, which: Int) {

    }

    override fun onShow(dialog: DialogInterface) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment SelectAttendanceThresholdsDialogFragment.
         */
        fun newInstance(): SelectAttendanceThresholdsDialogFragment {
            val fragment = SelectAttendanceThresholdsDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
