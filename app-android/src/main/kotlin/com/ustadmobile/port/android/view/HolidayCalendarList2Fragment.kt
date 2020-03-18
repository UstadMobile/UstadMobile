package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.view.HolidayCalendarList2View
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.staging.port.android.view.HolidayCalendarListRecyclerAdapter

//class HolidayCalendarList2Fragment: UstadListViewFragment<HolidayCalendar, HolidayCalendarWithNumEntries, HolidayCalendarListRecyclerAdapter.HolidayCalendarListViewHolder>(), HolidayCalendarList2View {
//
//    class HolidayCalendarRecyclerViewAdapter: ListAdapter<HolidayCalendarWithNumEntries, HolidayCalendarRecyclerViewAdapter.HolidayCalendarViewHolder>(DIFF_UTIL) {
//
//        class HolidayCalendarViewHolder(view: View) : RecyclerView.ViewHolder(view)
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayCalendarViewHolder {
//
//        }
//
//        override fun onBindViewHolder(holder: HolidayCalendarViewHolder, position: Int) {
//
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = super.onCreateView(inflater, container, savedInstanceState)
//        mRecyclerViewAdapter = HolidayCalendarRecyclerViewAdapter()
//
//
//        return view
//    }
//
//
//    override fun finishWithResult(result: HolidayCalendar) {
//        mResultListener?.onHolidayCalendarListSelected(result)
//    }
//
//    override val viewContext: Any
//        get() = requireContext()
//
//    companion object {
//        val DIFF_UTIL = object: DiffUtil.ItemCallback<HolidayCalendarWithNumEntries>() {
//            override fun areItemsTheSame(oldItem: HolidayCalendarWithNumEntries, newItem: HolidayCalendarWithNumEntries): Boolean {
//
//            }
//
//            override fun areContentsTheSame(oldItem: HolidayCalendarWithNumEntries, newItem: HolidayCalendarWithNumEntries): Boolean {
//
//            }
//        }
//    }
//}