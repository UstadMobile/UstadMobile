package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AddCustomFieldOptionDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.port.android.view.UstadDialogFragment
import io.reactivex.annotations.NonNull
import java.util.*

class AddCustomFieldOptionDialogFragment : UstadDialogFragment(),
        AddCustomFieldOptionDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    override val viewContext: Any
        get() = context!!

    internal var dialog: AlertDialog? = null
    internal var rootView: View? = null
    internal var mPresenter: AddCustomFieldOptionDialogPresenter? = null

    internal var optionText: EditText? = null


    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_add_custom_field_option_dialog, null)

        optionText = rootView!!.findViewById(R.id.fragment_add_question_option_dialog_text)

        val positiveOCL = { _: DialogInterface, _: Int -> mPresenter!!.handleClickOk() }

        val negativeOCL = { _ : DialogInterface, _: Int -> finish() }

        val builder = AlertDialog.Builder(Objects.requireNonNull(context!!))
        builder.setTitle(R.string.add_modify_option)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.ok, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog!!.setOnShowListener(this)

        mPresenter = AddCustomFieldOptionDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(getArguments()), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(getArguments()))


        optionText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.setOptionValue(s.toString())
            }
        })

        return dialog as AlertDialog

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

    override fun setOptionValue(optionValue: String) {
        runOnUiThread (Runnable { optionText!!.setText(optionValue) })
    }

}
