package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AddReminderDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddReminderDialogView


class AddReminderDialogFragment : UstadDialogFragment(), AddReminderDialogView, DialogInterface.OnShowListener {


    override val viewContext: Any
        get() = context!!

    private var mPresenter: AddReminderDialogPresenter? = null
    private var dialog: AlertDialog? = null
    private var rootView: View? = null

    private var days: Int = 0

    private var mAttachedContext: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onShow(p0: DialogInterface?) {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_add_reminder, null)

        val np =
                rootView!!.findViewById<NumberPicker>(R.id.fragment_add_reminder_days_np)
        np.minValue = 1
        np.maxValue = 100

        val positiveOCL = {
            dialog: DialogInterface, which: Int ->
                days = np.value
                mPresenter!!.handleAddReminder(days)
        }

        val negativeOCL = {
            dialog: DialogInterface, which: Int ->
                dialog.dismiss() }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.add_reminder)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog!!.setOnShowListener(this)

        mPresenter = AddReminderDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments),
                this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(arguments))

        return dialog as AlertDialog

    }

    override fun finish() {
        dialog!!.dismiss()
    }



}
