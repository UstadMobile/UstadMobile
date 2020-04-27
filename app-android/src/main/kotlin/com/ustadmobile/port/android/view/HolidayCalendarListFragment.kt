package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemHolidayCalendarBinding
import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class HolidayCalendarListFragment()
    : UstadListViewFragment<HolidayCalendar, HolidayCalendarWithNumEntries>(),
        HolidayCalendarListView, MessageIdSpinner.OnMessageIdOptionSelectedListener,
        View.OnClickListener{

    private var mPresenter: HolidayCalendarListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in HolidayCalendarWithNumEntries>?
        get() = mPresenter

    class HolidayCalendarListViewHolder(val itemBinding: ItemHolidayCalendarBinding): RecyclerView.ViewHolder(itemBinding.root)

    class HolidayCalendarListRecyclerAdapter(var presenter: HolidayCalendarListPresenter?)
        : SelectablePagedListAdapter<HolidayCalendarWithNumEntries,
            HolidayCalendarListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayCalendarListViewHolder {
            val itemBinding = ItemHolidayCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.pagedListAdapter = this
            itemBinding.presenter = presenter
            return HolidayCalendarListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: HolidayCalendarListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.holidayCalendar = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mPresenter = HolidayCalendarListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataRecyclerViewAdapter = HolidayCalendarListRecyclerAdapter(mPresenter)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                requireContext().getString(R.string.create_new, requireContext().getString(R.string.holiday_calendar)))
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text = requireContext().getString(R.string.holiday_calendar)
    }

    override fun onClick(view: View?) {
        navigateToEditEntity(null, R.id.holidaycalendar_edit_dest, HolidayCalendar::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }


    override val displayTypeRepo: Any?
        get() = dbRepo?.holidayCalendarDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<HolidayCalendarWithNumEntries> = object
            : DiffUtil.ItemCallback<HolidayCalendarWithNumEntries>() {
            override fun areItemsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                         newItem: HolidayCalendarWithNumEntries): Boolean {
                return oldItem.umCalendarUid == newItem.umCalendarUid
            }

            override fun areContentsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                            newItem: HolidayCalendarWithNumEntries): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?): HolidayCalendarListFragment {
            return HolidayCalendarListFragment().apply {
                arguments = bundle
            }
        }
    }
}