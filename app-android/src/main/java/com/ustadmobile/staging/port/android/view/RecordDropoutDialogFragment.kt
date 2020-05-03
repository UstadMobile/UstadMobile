package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.RecordDropoutDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.RecordDropoutDialogView
import com.ustadmobile.port.android.view.UstadDialogFragment

class RecordDropoutDialogFragment : UstadDialogFragment(), RecordDropoutDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var mPresenter: RecordDropoutDialogPresenter
    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    internal lateinit var otherNGO: CheckBox
    internal lateinit var move: CheckBox
    internal lateinit var cry: CheckBox
    internal lateinit var sick: CheckBox
    internal lateinit var permission: CheckBox
    internal lateinit var school: CheckBox
    internal lateinit var transportation: CheckBox
    internal lateinit var personal: CheckBox
    internal lateinit var other: CheckBox

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_record_dropout_dialog, null)


        val positiveOCL = { dialog:DialogInterface, which:Int -> mPresenter.handleClickOk() }

        val negativeOCL = { dialog:DialogInterface, which:Int  -> finish() }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.record_dropout)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.ok, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog.setOnShowListener(this)



        mPresenter = RecordDropoutDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        return dialog
    }

    override fun onClick(dialog: DialogInterface, which: Int) {}

    override fun onShow(dialog: DialogInterface) {}

    override fun onClick(v: View) {}

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun finish() {}
}
