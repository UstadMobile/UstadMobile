package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.toughra.ustadmobile.databinding.ItemTimeZoneBinding
import com.ustadmobile.core.controller.OnSearchSubmitted
import com.ustadmobile.core.controller.TimeZoneListPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.TimeZoneListView
import com.ustadmobile.core.viewmodel.TimeZoneListUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import java.util.*

class TimeZoneListFragment : UstadBaseFragment() , TimeZoneListView, OnSearchSubmitted{

    class TimeZoneViewHolder(var binding: ItemTimeZoneBinding) : RecyclerView.ViewHolder(binding.root)

    inner class TimeZoneRecyclerViewAdapter: ListAdapter<TimeZone, TimeZoneViewHolder>(DIFFUTIL_TIMEZONE) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
            return TimeZoneViewHolder(ItemTimeZoneBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)).also {
                it.binding.fragment = this@TimeZoneListFragment
            }
        }

        override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
            holder.binding.timeZone = getItem(position)
        }
    }


    var mDataBinding: FragmentListBinding? = null

    private var mRecyclerAdapter: TimeZoneRecyclerViewAdapter? = null

    private var mPresenter: TimeZoneListPresenter? = null

    val allTimeZones : List<TimeZone> by lazy {
        TimeZone.getAvailableIDs().map { TimeZone.getTimeZone(it) }.sortedBy { it.rawOffset }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDataBinding = FragmentListBinding.inflate(inflater, container, false)
        mRecyclerAdapter = TimeZoneRecyclerViewAdapter()
        mDataBinding?.fragmentListRecyclerview?.layoutManager = LinearLayoutManager(requireContext())
        mDataBinding?.fragmentListRecyclerview?.adapter = mRecyclerAdapter
        mRecyclerAdapter?.submitList(allTimeZones)

        mPresenter = TimeZoneListPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return mDataBinding?.root
    }

    fun handleClickTimezone(timeZone: TimeZone) {
        mPresenter?.handleClickTimeZone(timeZone.id)
    }

    override fun onSearchSubmitted(text: String?) {
        if(text == null){
            mRecyclerAdapter?.submitList(allTimeZones)
            return
        }

        GlobalScope.launch {
            val searchWords = text.split(Regex("\\s+"))
            val filteredItems = allTimeZones.filter {timeZone ->
                searchWords.any { timeZone.id.contains(it, ignoreCase = true) }
                        || searchWords.any { timeZone.displayName.contains(it, ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
                mRecyclerAdapter?.submitList(filteredItems)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
        searchManager?.searchListener = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding?.fragmentListRecyclerview?.adapter = null
        mRecyclerAdapter = null
        mPresenter = null
        mDataBinding = null
    }

    companion object {
        val DIFFUTIL_TIMEZONE = object: DiffUtil.ItemCallback<TimeZone>() {
            override fun areItemsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimeZone, newItem: TimeZone): Boolean {
                return oldItem.id == newItem.id
            }
        }



    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TimeZoneListScreen(
    uiState: TimeZoneListUiState = TimeZoneListUiState(),
    onClickTimezone: (TimeZone) -> Unit = {},
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(
            items = uiState.timeZoneList,
            key = { timezone -> timezone.id }
        ){ timezone ->
            ListItem(
                modifier = Modifier.clickable {
                    onClickTimezone(timezone)
                },
                text = { Text("") },
                secondaryText = { Text("") }
            )
        }
    }
}


@Composable
@Preview
fun TimeZoneListScreenPreview() {
    val uiState = TimeZoneListUiState(

    )
    MdcTheme{
        TimeZoneListScreen(uiState)
    }
}
