package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText

import androidx.appcompat.app.AlertDialog

import com.toughra.ustadmobile.R
import com.ustadmobile.staging.core.controller.AddQuestionOptionDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddQuestionOptionDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.port.android.view.UstadDialogFragment

import java.util.Objects

import io.reactivex.annotations.NonNull

class AddQuestionOptionDialogFragment : UstadDialogFragment(), AddQuestionOptionDialogView, AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener, DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View
    internal lateinit var mPresenter: AddQuestionOptionDialogPresenter
    internal lateinit var optionText: EditText


    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = Objects.requireNonNull<Context>(context).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_add_question_option_dialog, null)

        optionText = rootView.findViewById(R.id.fragment_add_question_option_dialog_text)

        val positiveOCL = { _:DialogInterface, _:Int -> mPresenter.handleAddQuestionOption(optionText
                .text.toString()) }

        val negativeOCL = { _:DialogInterface, _:Int -> mPresenter
                .handleCancelQuestionOption() }

        val builder = AlertDialog.Builder(Objects.requireNonNull<Context>(context))
        builder.setTitle(R.string.add_option)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog.setOnShowListener(this)


        mPresenter = AddQuestionOptionDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))


        return dialog

    }


    override fun onClick(dialogInterface: DialogInterface, i: Int) {

    }

    override fun onShow(dialogInterface: DialogInterface) {

    }

    override fun onClick(view: View) {

    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {

    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    override fun finish() {

    }

    override fun setOptionText(text: String) {
        runOnUiThread(Runnable{ optionText.setText(text) })
    }
}
