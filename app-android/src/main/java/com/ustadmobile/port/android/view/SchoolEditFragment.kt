package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolEditBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.TimeZoneListPresenter.Companion.RESULT_TIMEZONE_KEY
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

interface SchoolEditFragmentEventHandler {
    fun handleClickTimeZone()
    fun showHolidayCalendarPicker()
}

class SchoolEditFragment: UstadEditFragment<SchoolWithHolidayCalendar>(), SchoolEditView,
        SchoolEditFragmentEventHandler{

    private var mBinding: FragmentSchoolEditBinding? = null

    private var mPresenter: SchoolEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolEditBinding.inflate(inflater, container,false).also {
                        rootView = it.root
                        it.activityEventHandler = this
                    }

        mPresenter = SchoolEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_a_new_school, R.string.edit_school)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                HolidayCalendar::class.java) {
            val holidayCalendar = it.firstOrNull() ?: return@observeResult
            entity?.holidayCalendar = holidayCalendar
            entity?.schoolHolidayCalendarUid = holidayCalendar.umCalendarUid
            mBinding?.school = entity
        }


        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(RESULT_TIMEZONE_KEY)
                ?.observe(viewLifecycleOwner) {
                    entity?.schoolTimeZone = it
                    mBinding?.school = entity
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_a_new_school, R.string.edit_school)
    }

    override var entity: SchoolWithHolidayCalendar? = null
        set(value) {
            field = value
            mBinding?.school = value
        }

    override var fieldsEnabled: Boolean = false
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object{
        val DIFF_CALLBACK_CLAZZ = object: DiffUtil.ItemCallback<Clazz>() {
            override fun areItemsTheSame(oldItem: Clazz, newItem: Clazz): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: Clazz,
                                            newItem: Clazz): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun handleClickTimeZone() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(String::class.java, R.id.time_zone_list_dest,
                destinationResultKey = RESULT_TIMEZONE_KEY)
    }

    override fun showHolidayCalendarPicker() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(HolidayCalendar::class.java, R.id.holidaycalendar_list_dest)
    }
}