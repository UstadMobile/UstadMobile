package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentHolidaycalendarEditBinding
import com.toughra.ustadmobile.databinding.ItemHolidayBinding
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.core.viewmodel.HolidayCalendarEditUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class HolidayCalendarEditFragment() : UstadEditFragment<HolidayCalendar>(), HolidayCalendarEditView{

    private var mBinding: FragmentHolidaycalendarEditBinding? = null

    private var mPresenter: HolidayCalendarEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, HolidayCalendar>?
        get() = mPresenter

    class HolidayRecyclerAdapter(var presenter: HolidayCalendarEditPresenter?): ListAdapter<Holiday, HolidayRecyclerAdapter.HolidayViewHolder>(DIFF_CALLBACK_HOLIDAY) {

        class HolidayViewHolder(val binding: ItemHolidayBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
            val viewHolder = HolidayViewHolder(ItemHolidayBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            return viewHolder
        }

        override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
            holder.binding.holiday = getItem(position)
        }
    }

    override var holidayList: LiveData<List<Holiday>>? = null
        get() = field
        set(value) {
            field?.removeObserver(holidayObserver)
            field = value
            value?.observe(this, holidayObserver)
        }

    private var holidayRecyclerAdapter: HolidayRecyclerAdapter? = null

    private var holidayRecyclerView: RecyclerView? = null

    private val holidayObserver = Observer<List<Holiday>?> {
        t -> holidayRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentHolidaycalendarEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        holidayRecyclerView = rootView.findViewById(R.id.activity_holidaycalendar_holiday_recyclerview)
        holidayRecyclerAdapter = HolidayRecyclerAdapter(null)
        holidayRecyclerView?.adapter = holidayRecyclerAdapter
        holidayRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = HolidayCalendarEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        holidayRecyclerAdapter?.presenter = mPresenter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_a_new_holiday_calendar, R.string.edit_holiday_calendar)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        holidayRecyclerView?.adapter = null
        holidayRecyclerView = null
        holidayRecyclerAdapter = null
        holidayList = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter?.onSaveInstanceState(this) }.toBundle())
    }


    override var entity: HolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            mBinding?.holidaycalendar = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.loading = value
        }


    companion object {

        val DIFF_CALLBACK_HOLIDAY = object: DiffUtil.ItemCallback<Holiday>() {
            override fun areItemsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
                return oldItem.holUid == newItem.holUid
            }

            override fun areContentsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
                return oldItem == newItem
            }
        }

    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HolidayCalendarEditScreen(
    uiState: HolidayCalendarEditUiState,
    onHolidayCalendarChange: (HolidayCalendar?) -> Unit = {},
    onClickAddItem: () -> Unit = {},
    onDeleteItemClick: (HolidayCalendar) -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
        ){

        UstadTextEditField(
            value = uiState.holidayCalendar?.umCalendarName ?: "",
            label = stringResource(id = R.string.name),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onHolidayCalendarChange(uiState.holidayCalendar?.shallowCopy {
                    umCalendarName = it
                })
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickAddItem()
            },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(id = R.string.add_a_holiday)) }
        )

        uiState.calendarList?.forEach { item ->
            ListItem(
                text = { Text(text = item.umCalendarName ?: "") },
                trailing = {
                    IconButton(onClick = {
                        onDeleteItemClick(item)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        }

    }
}

@Composable
@Preview
fun HolidayCalendarEditPreview(){
    HolidayCalendarEditScreen(
        uiState = HolidayCalendarEditUiState(
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "my cal"
            },
            calendarList = listOf(
                HolidayCalendar().apply {
                    umCalendarName = "first"
                }
            )
        )
    )
}