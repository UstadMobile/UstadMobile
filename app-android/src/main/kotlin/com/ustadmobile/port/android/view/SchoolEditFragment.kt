package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzSimpleBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.School
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
        SchoolEditFragmentEventHandler {

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
            ClazzRecyclerAdapter.SelQuestionViewHolder>(DIFF_CALLBACK_CLAZZ) {

        class SelQuestionViewHolder(val binding: ItemClazzSimpleBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelQuestionViewHolder {
            val viewHolder = SelQuestionViewHolder(ItemClazzSimpleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: SelQuestionViewHolder, position: Int) {
            holder.binding.clazz = getItem(position)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolEditBinding.inflate(inflater, container,false).also {
                        rootView = it.root
                    }

        //clazzRecyclerView = rootView.findViewById(R.id.)
        clazzRecyclerAdapter = ClazzRecyclerAdapter(this, null)
        clazzRecyclerView?.adapter = clazzRecyclerAdapter
        clazzRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mPresenter = SchoolEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        //mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
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
            mBinding?.genderOptions = value
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
        navigateToEditEntity(clazz, R.id.clazz_edit_dest, Clazz::class.java)
    }

    override fun onClickDeleteClazz(clazz: Clazz) {
        mPresenter?.handleRemoveSchedule(clazz)
    }

    override fun onClickAddClazz() {
        onClickEditClazz(null)
    }

    override fun showHolidayCalendarPicker() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(HolidayCalendar::class.java, R.id.holidaycalendar_list_dest)
    }
}