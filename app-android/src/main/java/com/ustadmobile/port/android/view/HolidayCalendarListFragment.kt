package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemHolidayCalendarBinding
import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.core.viewmodel.HolidayCalendarListUiState
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.R as CR

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
            holder.itemView.tag = item?.umCalendarUid
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
                this,  di, viewLifecycleOwner).withViewLifecycle()
        mDataRecyclerViewAdapter = HolidayCalendarListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(CR.string.add_a_new_holiday_calendar))
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text = requireContext().getString(CR.string.holiday_calendar)
    }

    override fun onClick(view: View?) {
        mPresenter?.handleClickCreateNewFab()
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
                return oldItem.umCalendarName == newItem.umCalendarName && oldItem.numEntries == newItem.numEntries
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HolidayCalendarListScreen(
    uiState: HolidayCalendarListUiState,
    onListItemClick: (HolidayCalendarWithNumEntries) -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultScreenPadding()
    ){
        items(
            uiState.holidayCalendarList,
            key = {it.umCalendarUid}
        ){ holidayCalendar ->
            ListItem(
                modifier = Modifier
                    .clickable{
                        onListItemClick(holidayCalendar)
                    },
                text = {
                    Text(text = holidayCalendar.umCalendarName ?: "")
                },
                secondaryText = {
                    Text(text = stringResource(id = CR.string.num_holidays, holidayCalendar.numEntries))
                }
            )
        }
    }
}

@Composable
@Preview
fun HolidayCalendarListScreenPreview(){
    HolidayCalendarListScreen(
        uiState = HolidayCalendarListUiState(
            holidayCalendarList = listOf(
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 1"
                    umCalendarUid = 898787
                    numEntries = 4
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 2"
                    umCalendarUid = 8
                    numEntries = 3
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 3"
                    umCalendarUid = 80
                    numEntries = 2
                }
            )
        )
    )
}