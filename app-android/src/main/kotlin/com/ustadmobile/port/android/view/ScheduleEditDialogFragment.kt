package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEditScheduleDialogBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule

class ScheduleEditDialogFragment : UstadDialogFragment(), ScheduleEditView,
        DialogInterface.OnClickListener{

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewBindingVal = FragmentEditScheduleDialogBinding.inflate(layoutInflater, null)
        viewBinding = viewBindingVal

        return AlertDialog.Builder(requireContext())
                .setView(viewBindingVal.root)
                .setPositiveButton(R.string.add, this)
                .setNegativeButton(R.string.cancel, this)
                .create()
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
    }

    override fun finishWithResult(schedule: Schedule?) {
        fragmentListener?.onScheduleDone(schedule)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

    }

    override val viewContext: Any
        get() = requireContext()
}