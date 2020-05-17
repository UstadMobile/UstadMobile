package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzSimpleEditBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface SchoolEditFragmentEventHandler {
    fun onClickEditClazz(clazz: Clazz?)
    fun onClickDeleteClazz(clazz: Clazz)
    fun onClickAddClazz()
    fun showHolidayCalendarPicker()
}

class SchoolEditFragment: UstadEditFragment<SchoolWithHolidayCalendar>(), SchoolEditView,
        SchoolEditFragmentEventHandler,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mBinding: FragmentSchoolEditBinding? = null

    private var mPresenter: SchoolEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    private var clazzRecyclerAdapter: ClazzRecyclerAdapter? = null

    private var clazzRecyclerView : RecyclerView? = null

    private val clazzObserver = Observer<List<Clazz>?>{
        t -> clazzRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()

    class ClazzRecyclerAdapter(
            val activityEventHandler: SchoolEditFragmentEventHandler,
            var presenter: SchoolEditPresenter?)
        : ListAdapter<Clazz,
            ClazzRecyclerAdapter.ClazzViewHolder>(DIFF_CALLBACK_CLAZZ) {

        class ClazzViewHolder(val binding: ItemClazzSimpleEditBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
            val viewHolder = ClazzViewHolder(ItemClazzSimpleEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzViewHolder, position: Int) {
            holder.binding.clazz = getItem(position)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolEditBinding.inflate(inflater, container,false).also {
                        rootView = it.root
                        it.activityEventHandler = this
                        it.genderSelectionListener = this
                    }

        clazzRecyclerView = rootView.findViewById(R.id.fragment_school_edit_class_rv)
        clazzRecyclerAdapter = ClazzRecyclerAdapter(this, null)
        clazzRecyclerView?.adapter = clazzRecyclerAdapter
        clazzRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mPresenter = SchoolEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        clazzRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.school)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
            Clazz::class.java){
            val clazz = it.firstOrNull()?: return@observeResult
            mPresenter?.handleAddOrEditClazz(clazz)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                HolidayCalendar::class.java) {
            val holidayCalendar = it.firstOrNull() ?: return@observeResult
            entity?.holidayCalendar = holidayCalendar
            entity?.schoolHolidayCalendarUid = holidayCalendar.umCalendarUid
            mBinding?.school = entity
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        clazzRecyclerAdapter = null
        clazzRecyclerView = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.school)
    }

    override var entity: SchoolWithHolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            mBinding?.school = value
            mBinding?.genderOptions = this.genderOptions
        }

    override var schoolClazzes: DoorMutableLiveData<List<Clazz>>? = null
        get() = field
        set(value) {
            field?.removeObserver(clazzObserver)
            field = value
            value?.observe(this, clazzObserver)
        }

    override var genderOptions: List<SchoolEditPresenter.GenderTypeMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
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

    override fun onClickEditClazz(clazz: Clazz?) {
        onSaveStateToBackStackStateHandle()

        navigateToEditEntity(clazz, R.id.clazz_detail_dest, Clazz::class.java)

    }

    override fun onClickDeleteClazz(clazz: Clazz) {
        mPresenter?.handleRemoveSchedule(clazz)
    }

    override fun onClickAddClazz() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Clazz::class.java,
                R.id.clazz_list_dest,
                bundleOf(ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to entity?.schoolUid.toString()))
    }

    override fun showHolidayCalendarPicker() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(HolidayCalendar::class.java, R.id.holidaycalendar_list_dest)
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {

    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }
}