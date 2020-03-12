package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEditScheduleDialogBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule

class ScheduleEditDialogFragment : UstadDialogFragment(), ScheduleEditView,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener{

    interface ScheduleEditDialogFragmentListener {

        fun onScheduleDone(schedule: Schedule?)

    }

    private var fragmentListener: ScheduleEditDialogFragmentListener? = null

    private var viewBinding: FragmentEditScheduleDialogBinding? = null

    private var mPresenter: ScheduleEditPresenter? = null

    override var schedule: Schedule? = null
        get() = field
        set(value) {
            viewBinding?.schedule = value
            field = value
        }

    override var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>? = null
        get() = field
        set(value) {
            viewBinding?.frequencyOptions = value
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewBindingVal = FragmentEditScheduleDialogBinding.inflate(inflater, null)
        viewBinding = viewBindingVal

        return AlertDialog.Builder(requireContext())
                .setView(viewBindingVal.root)
                .setPositiveButton(R.string.add, this)
                .setNegativeButton(R.string.cancel, this)
                .create().also {
                    it.setOnShowListener(this)
                }
    }

    override fun onShow(dialog: DialogInterface?) {
        mPresenter = ScheduleEditPresenter(requireContext(), arguments.toStringMap(), this)
        mPresenter?.onCreate(null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as? ScheduleEditDialogFragmentListener
    }



    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun finishWithResult(schedule: Schedule?) {
        fragmentListener?.onScheduleDone(schedule)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

    }

    override val viewContext: Any
        get() = requireContext()
}