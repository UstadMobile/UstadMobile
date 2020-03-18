package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemHolidaycalendarListItemBinding
import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

class HolidayCalendarListFragment(): UstadListViewFragment<HolidayCalendar, HolidayCalendarWithNumEntries, HolidayCalendarListFragment.HolidayCalendarListRecyclerAdapter.HolidayCalendarListViewHolder>(),
        HolidayCalendarListView, MessageIdSpinner.OnMessageIdOptionSelectedListener{

    private var mPresenter: HolidayCalendarListPresenter? = null

    private var dbRepo: UmAppDatabase? = null

    class HolidayCalendarListRecyclerAdapter(var presenter: HolidayCalendarListPresenter?): PagedListAdapter<HolidayCalendarWithNumEntries, HolidayCalendarListRecyclerAdapter.HolidayCalendarListViewHolder>(DIFF_CALLBACK) {

        class HolidayCalendarListViewHolder(val itemBinding: ItemHolidaycalendarListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayCalendarListViewHolder {
            val itemBinding = ItemHolidaycalendarListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return HolidayCalendarListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: HolidayCalendarListViewHolder, position: Int) {
            holder.itemBinding.holidayCalendar = getItem(position)
            holder.itemBinding.presenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = HolidayCalendarListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        mRecyclerViewAdapter = HolidayCalendarListRecyclerAdapter(mPresenter)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.HolidayCalendarDao")

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<HolidayCalendarWithNumEntries> = object
            : DiffUtil.ItemCallback<HolidayCalendarWithNumEntries>() {
            override fun areItemsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                         newItem: HolidayCalendarWithNumEntries): Boolean {
                TODO("e.g. insert primary keys here return oldItem.holidayCalendar == newItem.holidayCalendar")
            }

            override fun areContentsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                            newItem: HolidayCalendarWithNumEntries): Boolean {
                return oldItem == newItem
            }
        }
    }
}