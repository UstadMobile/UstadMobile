package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolEditBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.TimeZoneListPresenter.Companion.RESULT_TIMEZONE_KEY
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap

class SchoolEditFragment: UstadEditFragment<SchoolWithHolidayCalendar>(), SchoolEditView{

    private var mBinding: FragmentSchoolEditBinding? = null

    private var mPresenter: SchoolEditPresenter? = null

    private var scopedGrantRecyclerAdapter: ScopedGrantAndNameEditRecyclerViewAdapter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    override var scopedGrants: LiveData<List<ScopedGrantAndName>>? = null
        set(value) {
            field?.removeObserver(scopedGrantListObserver)
            field = value
            value?.observe(viewLifecycleOwner, scopedGrantListObserver)
        }

    private val scopedGrantListObserver = Observer<List<ScopedGrantAndName>> {
            t -> scopedGrantRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentSchoolEditBinding.inflate(inflater, container,false).also {
                        rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_a_new_school, R.string.edit_school)
        val navController = findNavController()

        mPresenter = SchoolEditPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        mBinding?.mPresenter = mPresenter

        val permissionList = ScopedGrantEditPresenter.PERMISSION_LIST_MAP[School.TABLE_ID]
            ?: throw IllegalStateException("ScopedGrantEdit permission list not found!")
        scopedGrantRecyclerAdapter = ScopedGrantAndNameEditRecyclerViewAdapter(
            mPresenter?.scopedGrantOneToManyHelper, permissionList)

        mBinding?.schoolEditFragmentPermissionsInc?.itemScopedGrantOneToNRecycler?.apply {
            adapter = scopedGrantRecyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

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
            super.fieldsEnabled = value
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
}