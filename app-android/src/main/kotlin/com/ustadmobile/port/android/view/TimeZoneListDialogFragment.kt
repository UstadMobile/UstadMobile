package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemTimezoneBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

interface OnTimeZoneSelectedListener {

    fun onTimeZoneSelected(timeZone: TimeZone)

}

class TimeZoneListDialogFragment: UstadDialogFragment(), DialogInterface.OnShowListener, OnTimeZoneSelectedListener {

    private val root: View? = null

    private var mRecyclerView: RecyclerView? = null

    private var mRecyclerViewAdapter: TimeZoneRecyclerViewAdapter? = null

    private var mOnTimeZoneSelectedListener: OnTimeZoneSelectedListener? = null

    class TimeZoneRecyclerViewAdapter(var listener: OnTimeZoneSelectedListener?): ListAdapter<TimeZone, TimeZoneRecyclerViewAdapter.TimeZoneViewHolder>(TIMEZONE_DIFF_UTIL) {

        class TimeZoneViewHolder(val binding: ItemTimezoneBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
            return TimeZoneViewHolder(ItemTimezoneBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)).also {
                it.binding.listener = listener
            }
        }

        override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
            holder.binding.timeZone = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            listener = null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.fragment_timezone_list, null)
        mRecyclerView = view?.findViewById(R.id.fragment_timezonelist_recyclerview)
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerViewAdapter = TimeZoneRecyclerViewAdapter(this)
        mRecyclerView?.adapter = mRecyclerViewAdapter

        return AlertDialog.Builder(requireContext(), R.style.FullScreenDialogStyle)
                .setView(view)
                .create().apply {
                    setOnShowListener(this@TimeZoneListDialogFragment)
                }
    }

    override fun onShow(dialog: DialogInterface?) {
        GlobalScope.launch {
            val timeZoneList = TimeZone.getAvailableIDs().map { TimeZone.getTimeZone(it) }
                    .sortedBy { it.rawOffset }
            var  initialTimeZone = arguments?.getString(ARG_CURRENT_TIMEZONE, "")
            if(initialTimeZone == null) {
                initialTimeZone = TimeZone.getDefault().id
            }

            val initialIndex = timeZoneList.indexOfFirst { it.id == initialTimeZone }
            GlobalScope.launch(Dispatchers.Main) {
                mRecyclerViewAdapter?.submitList(timeZoneList)
                mRecyclerView?.takeIf { initialIndex != -1 }?.scrollToPosition(initialIndex)
            }
        }
    }

    override fun onTimeZoneSelected(timeZone: TimeZone) {
        dismiss()
        mOnTimeZoneSelectedListener?.onTimeZoneSelected(timeZone)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mOnTimeZoneSelectedListener = context as? OnTimeZoneSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        mOnTimeZoneSelectedListener = null
    }

    override fun onDestroyView() {
        mRecyclerView?.adapter = null
        mRecyclerView = null
        mRecyclerViewAdapter = null
        super.onDestroyView()
    }

    companion object {
        val TIMEZONE_DIFF_UTIL = object: DiffUtil.ItemCallback<TimeZone>() {
            override fun areItemsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
                return oldItem.id == newItem.id
            }
        }

        const val ARG_CURRENT_TIMEZONE = "timezone"

        fun newInstance(currentTimeZone: String): TimeZoneListDialogFragment {
            return TimeZoneListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_TIMEZONE, currentTimeZone)
                }
            }
        }
    }
}