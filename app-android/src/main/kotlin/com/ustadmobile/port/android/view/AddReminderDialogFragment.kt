package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.ustadmobile.core.controller.SaleItemDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddReminderDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SaleItemDetailView

import java.util.Objects
import com.toughra.ustadmobile.R


class AddReminderDialogFragment : UstadDialogFragment(),
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, AddReminderDialogView,
        DismissableDialog {

    private var mPresenter: SaleItemDetailPresenter? = null
    private var dialog: AlertDialog? = null
    private var rootView: View? = null

    private var days: Int = 0

    private var mAttachedContext: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }


    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = Objects.requireNonNull(context).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_add_reminder, null)

        val np = rootView!!.findViewById<NumberPicker>(R.id.fragment_add_reminder_days_np)
        np.minValue = 1
        np.maxValue = 100

        val positiveOCL = { dialog: DialogInterface, which: Int ->
            days = np.value
            mPresenter!!.handleAddReminder(days)
        }

        val negativeOCL = { dialog: DialogInterface, which: Int -> dialog.dismiss() }

        val builder = AlertDialog.Builder(Objects.requireNonNull(context))
        builder.setTitle(R.string.add_reminder)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog!!.setOnShowListener(this)

        mPresenter = SaleItemDetailPresenter(context!!,
                UMAndroidUtil.bundleToHashtable(arguments),
                (activity as SaleItemDetailView?)!!, false)
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(arguments))

        return dialog

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
