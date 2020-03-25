package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEditScheduleDialogBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.serialization.json.Json

class ScheduleEditDialogFragment : UstadDialogFragment(), ScheduleEditView,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener{


    private var fragmentListener: ScheduleEditPresenter.ScheduleEditDoneListener? = null

    private var viewBinding: FragmentEditScheduleDialogBinding? = null

    private var mPresenter: ScheduleEditPresenter? = null

    private var mToolbar: Toolbar? = null

    override var entity: Schedule? = null
        get() = field
        set(value) {
            viewBinding?.schedule = value
            field = value
        }


    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>? = null
        get() = field
        set(value) {
            viewBinding?.frequencyOptions = value
            field  = value
        }

    override var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>? = null
        get() = field
        set(value) {
            viewBinding?.dayOptions = value
            field = value
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewBindingVal = FragmentEditScheduleDialogBinding.inflate(inflater, null)
        viewBinding = viewBindingVal
        mToolbar = viewBinding?.root?.findViewById(R.id.fragment_edit_schedule_dialog_toolbar)
        mToolbar?.setNavigationOnClickListener { v -> dismiss() }
        mToolbar?.inflateMenu(R.menu.menu_done)
        mToolbar?.setOnMenuItemClickListener {
            val currentSchedule = viewBinding?.schedule
            if(currentSchedule != null) {
                mPresenter?.handleClickSave(currentSchedule)
            }
            true
        }

        return AlertDialog.Builder(requireContext(), R.style.FullScreenDialogStyle)
                .setView(viewBindingVal.root)
                .create().also {
                    it.setOnShowListener(this)
                }
    }

    override fun onShow(dialog: DialogInterface?) {
        mPresenter = ScheduleEditPresenter(requireContext(), arguments.toStringMap(), this,
            this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mPresenter?.onCreate(null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as? ScheduleEditPresenter.ScheduleEditDoneListener
    }



    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        mToolbar = null
        mPresenter = null
    }

    override fun finishWithResult(result: Schedule) {
        fragmentListener?.onScheduleEditDone(result, 0)
        dismiss()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

    }

    override val viewContext: Any
        get() = requireContext()

    companion object {
        fun newInstance(schedule: Schedule): ScheduleEditDialogFragment {
            return ScheduleEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ScheduleEditView.ARG_SCHEDULE,
                            Json.stringify(Schedule.serializer(), schedule))
                }
            }
        }
    }
}