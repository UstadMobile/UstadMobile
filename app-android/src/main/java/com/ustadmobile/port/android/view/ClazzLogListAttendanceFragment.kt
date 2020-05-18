package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.databinding.ItemClazzLogAttendanceListBinding
import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import java.util.*

class ClazzLogListAttendanceFragment(): UstadListViewFragment<ClazzLog, ClazzLog>(),
        ClazzLogListAttendanceView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzLogListAttendancePresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzLog>?
        get() = mPresenter

    override var clazzTimeZone: String?
        get() = (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone
        set(value) {
            (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone = value
        }

    class ClazzLogListViewHolder(val itemBinding: ItemClazzLogAttendanceListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzLogListRecyclerAdapter(var presenter: ClazzLogListAttendancePresenter?, var clazzTimeZone: String?)
        : SelectablePagedListAdapter<ClazzLog, ClazzLogListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogListViewHolder {
            val itemBinding = ItemClazzLogAttendanceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzLogListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzLogListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzLog = item
            val timezoneVal = clazzTimeZone
            if(timezoneVal != null){
                holder.itemBinding.clazzLogLocalTime = DateTime.fromUnix(item?.logDate ?: 0L)
                        .toOffsetByTimezone(timezoneVal)
                holder.itemBinding.clazzLocalTimeZone = TimeZone.getTimeZone(timezoneVal)
            }

            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzLogListAttendancePresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = ClazzLogListRecyclerAdapter(mPresenter, clazzTimeZone ?: "UTC")
        val createNewText = ""
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mActivityWithFab?.activityFloatingActionButton?.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        mActivityWithFab?.activityFloatingActionButton?.visibility = View.GONE
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        //if(view?.id == R.id.item_createnew_layout)
            //navigateToEditEntity(null, R.id.clazzlog_edit_dest, ClazzLog::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzLogDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzLog> = object
            : DiffUtil.ItemCallback<ClazzLog>() {
            override fun areItemsTheSame(oldItem: ClazzLog,
                                         newItem: ClazzLog): Boolean {
                return oldItem.clazzLogUid == newItem.clazzLogUid
            }

            override fun areContentsTheSame(oldItem: ClazzLog,
                                            newItem: ClazzLog): Boolean {
                return oldItem == newItem
            }
        }
    }
}