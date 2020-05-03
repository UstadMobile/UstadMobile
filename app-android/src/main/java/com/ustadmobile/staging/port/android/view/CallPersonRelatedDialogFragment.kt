package com.ustadmobile.staging.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.Companion.NUMBER_FATHER
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.Companion.NUMBER_MOTHER
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.Companion.NUMBER_RETENTION_OFFICER
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.Companion.NUMBER_TEACHER
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.CallPersonRelatedDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.port.android.view.UstadDialogFragment

import java.util.*

class CallPersonRelatedDialogFragment : UstadDialogFragment(), CallPersonRelatedDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootView: View
    internal lateinit var dialog: AlertDialog

    internal lateinit var fatherEntry: TextView
    internal lateinit var motherEntry: TextView
    internal lateinit var teacherEntry: TextView
    internal lateinit var officerEntry: TextView
    internal lateinit var mPresenter: CallPersonRelatedDialogPresenter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = requireContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        rootView = inflater.inflate(R.layout.fragment_call_person_related_dialog, null)

        fatherEntry = rootView.findViewById(R.id.fragment_call_person_related_dialog_father)
        motherEntry = rootView.findViewById(R.id.fragment_call_person_related_dialog_mother)
        teacherEntry = rootView.findViewById(R.id.fragment_call_person_related_dialog_teacher)
        officerEntry = rootView.findViewById(R.id.fragment_call_person_related_dialog_retention)

        //Presenter stuff
        mPresenter = CallPersonRelatedDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("")
        builder.setView(rootView)
        dialog = builder.create()
        dialog.setOnShowListener(this)



        return dialog
    }

    override fun setOnDisplay(numbers: LinkedHashMap<Int, CallPersonRelatedDialogPresenter.NameWithNumber>) {
        val iterator = numbers.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val key = next.key
            val nameWithNumber = next.value
            runOnUiThread(Runnable{
                when (key) {
                    NUMBER_FATHER -> {
                        fatherEntry.text = nameWithNumber.name
                        fatherEntry.setOnClickListener { v -> handleClickCall(nameWithNumber.number) }
                    }
                    NUMBER_MOTHER -> {
                        motherEntry.text = nameWithNumber.name
                        motherEntry.setOnClickListener { v -> handleClickCall(nameWithNumber.number) }
                    }
                    NUMBER_TEACHER -> {
                        teacherEntry.text = nameWithNumber.name
                        teacherEntry.setOnClickListener { v -> handleClickCall(nameWithNumber.number) }
                    }
                    NUMBER_RETENTION_OFFICER -> {
                        officerEntry.text = nameWithNumber.name
                        officerEntry.setOnClickListener { v -> handleClickCall(nameWithNumber.number) }
                    }
                    else -> {
                    }
                }
            })

        }
    }

    override fun handleClickCall(number: String) {
        startActivity(Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:$number")))
    }

    override fun showRetention(show: Boolean) {
        officerEntry.visibility = if (show) View.VISIBLE else View.GONE
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

    override fun finish() {

    }
}
