package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentScheduleEditBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.lib.db.entities.Schedule

fun NavController.navigateToScheduleEdit(schedule: Schedule?, destinationKey: String = Schedule::class.java.simpleName) {
    val argBundle = Bundle()
    val backStateEntryVal = currentBackStackEntry
    if(backStateEntryVal != null && backStateEntryVal.arguments?.containsKey(UstadBaseFragment.ARG_RESULT_DEST_ID) != true) {
        argBundle.putString(UstadBaseFragment.ARG_RESULT_DEST_ID,
                backStateEntryVal.destination.id.toString())
        argBundle.putString(UstadBaseFragment.ARG_RESULT_DEST_KEY, destinationKey)
    }

    if(schedule != null)
        argBundle.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, schedule)

    navigate(R.id.schedule_edit_dest, argBundle)
}

class ScheduleEditFragment: UstadEditFragment<Schedule>(), ScheduleEditView {

    private var mBinding: FragmentScheduleEditBinding? = null

    private var mPresenter: ScheduleEditPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentScheduleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ScheduleEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        findNavController().previousBackStackEntry?.savedStateHandle!!.set("hello", "world")
        setHasOptionsMenu(true)

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    override var loading: Boolean = false

    override val viewContext: Any
        get() = requireContext()

    override var entity: Schedule? = null
        get() = field
        set(value) {
            field = value
            mBinding?.schedule = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>? = null
        get() = field
        set(value) {
            mBinding?.frequencyOptions = value
            field  = value
        }

    override var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>? = null
        get() = field
        set(value) {
            mBinding?.dayOptions = value
            field = value
        }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_done -> {
                val entityVal = mBinding?.schedule ?: return false
                mPresenter?.handleClickSave(entityVal)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}