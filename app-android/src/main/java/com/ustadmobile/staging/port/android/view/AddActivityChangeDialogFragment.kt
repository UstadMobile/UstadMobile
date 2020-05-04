package com.ustadmobile.staging.port.android.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

import androidx.appcompat.app.AlertDialog

import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.staging.core.controller.AddActivityChangeDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddActivityChangeDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.port.android.view.UstadDialogFragment

import java.util.Objects


/**
 * AddActivityChangeDialogFragment Android fragment
 */
class AddActivityChangeDialogFragment : UstadDialogFragment(), AddActivityChangeDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!


    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View
    internal lateinit var measurementPresets: Array<String>
    internal lateinit var measurementSpinner: Spinner
    internal lateinit var mPresenter: AddActivityChangeDialogPresenter
    internal lateinit var titleText: TextInputLayout

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //Inflate
        val inflater = Objects.requireNonNull<Context>(context).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_add_activity_change_dialog, null)

        //Get elements on view
        measurementSpinner = rootView.findViewById(R.id.fragment_add_activity_change_dialog_measurement_spinner)
        titleText = rootView.findViewById(R.id.fragment_add_activity_change_dialog_name_layout)

        //Positive and Negative listeners
        val positiveOCL = { _:DialogInterface, _:Int ->
            mPresenter.handleAddActivityChange() }

        val negativeOCL = { _:DialogInterface, _:Int-> mPresenter.handleCancelActivityChange() }

        //Create the dialog
        val builder = AlertDialog.Builder(Objects.requireNonNull<Context>(context))
        builder.setTitle(R.string.add_activity_literal)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog.setOnShowListener(this)

        //Set up Presenter
        mPresenter = AddActivityChangeDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        //UoM listenere
        measurementSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter.handleMeasurementSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //Title listener on view
        Objects.requireNonNull<EditText>(titleText.editText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter.handleTitleChanged(s.toString())
            }
        })

        return dialog

    }

    override fun onClick(dialog: DialogInterface, which: Int) {}

    override fun onShow(dialog: DialogInterface) {}

    override fun onClick(v: View) {}

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun finish() {}

    override fun setMeasurementDropdownPresets(presets: Array<String>) {
        this.measurementPresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull<Context>(context),
                android.R.layout.simple_spinner_item, measurementPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        measurementSpinner.adapter = adapter
    }

}
