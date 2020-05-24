package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogEditAttendanceBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogAttendanceRecordEditBinding
import com.ustadmobile.core.controller.ClazzLogEditAttendancePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface ClazzLogEditAttendanceFragmentEventHandler {

}

class ClazzLogEditAttendanceFragment: UstadEditFragment<ClazzLog>(), ClazzLogEditAttendanceView, ClazzLogEditAttendanceFragmentEventHandler {

    internal var mBinding: FragmentClazzLogEditAttendanceBinding? = null

    private var mPresenter: ClazzLogEditAttendancePresenter? = null


    class ClazzLogAttendanceRecordRecyclerAdapter(val activityEventHandler: ClazzLogEditAttendanceFragmentEventHandler,
            var presenter: ClazzLogEditAttendancePresenter?): ListAdapter<ClazzLogAttendanceRecordWithPerson, ClazzLogAttendanceRecordRecyclerAdapter.ClazzLogAttendanceRecordViewHolder>(DIFFUTIL_CLAZZATTENDANCERECORD) {

            class ClazzLogAttendanceRecordViewHolder(val binding: ItemClazzLogAttendanceRecordEditBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogAttendanceRecordViewHolder {
                val viewHolder = ClazzLogAttendanceRecordViewHolder(ItemClazzLogAttendanceRecordEditBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                return viewHolder
            }

            override fun onBindViewHolder(holder: ClazzLogAttendanceRecordViewHolder, position: Int) {
                holder.binding.clazzLogAttendanceRecordWithPerson = getItem(position)
                holder.binding.attendanceRecordStatusMap = STATUS_MAP
            }
        }

    override var clazzLogAttendanceRecordList: DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>? = null
        get() = field
        set(value) {
            field?.removeObserver(clazzLogAttendanceRecordObserver)
            field = value
            value?.observe(this, clazzLogAttendanceRecordObserver)
        }

    private var clazzLogAttendanceRecordRecyclerAdapter: ClazzLogAttendanceRecordRecyclerAdapter? = null

    private var clazzLogAttendanceRecordRecyclerView: RecyclerView? = null

    private val clazzLogAttendanceRecordObserver = Observer<List<ClazzLogAttendanceRecordWithPerson>?> {
        t -> clazzLogAttendanceRecordRecyclerAdapter?.submitList(t)
    }


    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzLogEditAttendanceBinding.inflate(inflater, container, false).also {
            rootView = it.root
            clazzLogAttendanceRecordRecyclerView = it.clazzLogEditRecyclerView
            it.clazzLogEditRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = ClazzLogEditAttendancePresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        clazzLogAttendanceRecordRecyclerAdapter = ClazzLogAttendanceRecordRecyclerAdapter(
                this, mPresenter)
        clazzLogAttendanceRecordRecyclerView?.adapter = MergeAdapter(clazzLogAttendanceRecordRecyclerAdapter)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        //setEditFragmentTitle(R.string.clazzlog)
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzLog = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        val DIFFUTIL_CLAZZATTENDANCERECORD = object: DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>() {
            override fun areItemsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson, newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem.clazzLogAttendanceRecordUid == newItem.clazzLogAttendanceRecordUid
            }

            override fun areContentsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson, newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem === newItem
            }
        }

        val STATUS_MAP = mapOf(ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
            ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
            ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button)
    }

}