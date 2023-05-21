package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.controller.ClazzLogEditAttendancePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceUiState
import com.google.accompanist.themeadapter.material.MdcTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.pager.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import kotlinx.coroutines.launch

interface ClazzLogEditAttendanceFragmentEventHandler {

}

class ClazzLogEditAttendanceFragment: UstadEditFragment<ClazzLog>(), ClazzLogEditAttendanceView, ClazzLogEditAttendanceFragmentEventHandler {

    internal var mBinding: FragmentClazzLogEditAttendanceBinding? = null

    private var mPresenter: ClazzLogEditAttendancePresenter? = null

    class MarkAllRecyclerAdapter(var presenter: ClazzLogEditAttendancePresenter?) : ListAdapter<Int, MarkAllRecyclerAdapter.MarkAllViewHolder>(DIFFUTIL_INT) {

        class MarkAllViewHolder(var binding: ItemClazzLogEditAttendanceMarkallBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkAllViewHolder {
            return MarkAllViewHolder(ItemClazzLogEditAttendanceMarkallBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: MarkAllViewHolder, position: Int) {
            val resourceIds = RESOURCE_ID_MAP[getItem(position)] ?: return
            val markAllValue = getItem(position)
            holder.binding.itemClazzLogEditAttendanceMarkallText.text = holder.itemView.context.getText(resourceIds.first)
            holder.binding.itemClazzLogEditAttendanceMarkallIcon.setImageResource(resourceIds.second)
            holder.binding.root.setOnClickListener {
                presenter?.handleClickMarkAll(markAllValue)
            }
        }

        companion object {
            val RESOURCE_ID_MAP = mapOf(
                    ClazzLogAttendanceRecord.STATUS_ATTENDED to Pair(R.string.mark_all_present, R.drawable.ic_checkbox_multiple_marked),
                    ClazzLogAttendanceRecord.STATUS_ABSENT to Pair(R.string.mark_all_absent, R.drawable.ic_checkbox_blank))
        }
    }

    inner class ClazzLogListDateHeaderRecyclerAdapter(): ListAdapter<ClazzLog, ClazzLogListDateHeaderRecyclerAdapter.DateHeaderViewHolder>(DIFFUTIL_CLAZZLOG) {

        inner class DateHeaderViewHolder(var binding: ItemClazzLogEditAttendanceDateheaderBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DateHeaderViewHolder(
            ItemClazzLogEditAttendanceDateheaderBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))

        override fun onBindViewHolder(holder: DateHeaderViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.date = item.logDate
            holder.binding.timeZone = _clazzLogTimezone
        }
    }

    inner class ClazzLogEditHeaderRecyclerAdapter(): ListAdapter<List<ClazzLog>, ClazzLogEditHeaderRecyclerAdapter.ClazzLogEditHeaderViewHolder>(DIFFUTIL_CLAZZLOGLIST) {

        inner class ClazzLogEditHeaderViewHolder(val binding: ItemClazzlogeditClazzlogviewpagerBinding) : RecyclerView.ViewHolder(binding.root){
            var clazzLogListDateHeaderRecyclerAdapter = ClazzLogListDateHeaderRecyclerAdapter()

            internal var mClazzLogList: List<ClazzLog>? = null

            internal val mOnPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val currentEntity = entity ?: return
                    val currentClazzLogList = mClazzLogList ?: return
                    if(currentClazzLogList[position].clazzLogUid == currentEntity.clazzLogUid)
                        return
                    updateNextPrevButtons()

                    mPresenter?.handleSelectClazzLog(currentEntity, currentClazzLogList[position])
                }
            }

            fun updateNextPrevButtons() {
                val pos = binding.clazzlogViewpager2.currentItem
                binding.nextButton.isEnabled = pos < (mClazzLogList?.size ?: 0) - 1
                binding.prevButton.isEnabled = pos > 0
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogEditHeaderViewHolder {
            val holder = ClazzLogEditHeaderViewHolder(ItemClazzlogeditClazzlogviewpagerBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            holder.binding.clazzlogViewpager2.apply {
                adapter = holder.clazzLogListDateHeaderRecyclerAdapter
            }

            holder.binding.nextButton.setOnClickListener {
                holder.binding.clazzlogViewpager2.currentItem = min(holder.binding.clazzlogViewpager2.currentItem + 1,
                        holder.mClazzLogList?.size ?: 0)
            }
            Locale.ENGLISH

            holder.binding.prevButton.setOnClickListener {
                holder.binding.clazzlogViewpager2.currentItem = max(holder.binding.clazzlogViewpager2.currentItem - 1, 0)
            }

            return holder
        }

        override fun onBindViewHolder(holder: ClazzLogEditHeaderViewHolder, position: Int) {
            val clazzLogList = getItem(position)
            holder.apply {
                mClazzLogList = clazzLogList
                clazzLogListDateHeaderRecyclerAdapter.submitList(clazzLogList)
                binding.clazzlogViewpager2.setCurrentItem(
                        max(clazzLogList.indexOfFirst { it.clazzLogUid == entity?.clazzLogUid }, 0),
                        false)
                updateNextPrevButtons()

                binding.clazzlogViewpager2.registerOnPageChangeCallback(holder.mOnPageChangeCallback)
            }
        }

        override fun onViewRecycled(holder: ClazzLogEditHeaderViewHolder) {
            super.onViewRecycled(holder)
            holder.binding.clazzlogViewpager2.unregisterOnPageChangeCallback(holder.mOnPageChangeCallback)
        }
    }

    private var clazzLogEditHeaderRecyclerAdapter: ClazzLogEditHeaderRecyclerAdapter? = null


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

    override var clazzLogAttendanceRecordList: MutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>? = null
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

    override var clazzLogsList: List<ClazzLog>? = null
        get() = field
        set(value) {
            field = value
            if(value != null)
                clazzLogEditHeaderRecyclerAdapter?.submitList(listOf(value))

        }

    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    private var mMarkAllRecyclerAdapter: MarkAllRecyclerAdapter? = null

    override var clazzLogTimezone: String? = null
        set(value) {
            field = value
            _clazzLogTimezone = TimeZone.getTimeZone(value)
        }

    internal var _clazzLogTimezone = TimeZone.getTimeZone("UTC")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzLogEditAttendanceBinding.inflate(inflater, container, false).also {
            rootView = it.root
            clazzLogAttendanceRecordRecyclerView = it.clazzLogEditRecyclerView
            it.clazzLogEditRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = ClazzLogEditAttendancePresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mMarkAllRecyclerAdapter = MarkAllRecyclerAdapter(mPresenter).also {
            it.submitList(listOf(ClazzLogAttendanceRecord.STATUS_ATTENDED, ClazzLogAttendanceRecord.STATUS_ABSENT))
        }

        clazzLogEditHeaderRecyclerAdapter = ClazzLogEditHeaderRecyclerAdapter()

        clazzLogAttendanceRecordRecyclerAdapter = ClazzLogAttendanceRecordRecyclerAdapter(
                this, mPresenter)

        clazzLogAttendanceRecordRecyclerView?.adapter = ConcatAdapter(clazzLogEditHeaderRecyclerAdapter,
                mMarkAllRecyclerAdapter, clazzLogAttendanceRecordRecyclerAdapter)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        clazzLogEditHeaderRecyclerAdapter = null
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzLog = value
            //TODO: pass this onwards
            val timeZoneVal = clazzLogTimezone
//            if(value != null && timeZoneVal != null)
//                mClazzLogListDateHeaderRecylerAdapter?.submitList(listOf(value.logDate to TimeZone.getTimeZone(timeZoneVal)))
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        val DIFFUTIL_CLAZZATTENDANCERECORD = object: DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>() {
            override fun areItemsTheSame(
                oldItem: ClazzLogAttendanceRecordWithPerson,
                newItem: ClazzLogAttendanceRecordWithPerson
            ): Boolean {
                return oldItem == newItem
            }

            //Required because we are using two way binding on a recycler view
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: ClazzLogAttendanceRecordWithPerson,
                newItem: ClazzLogAttendanceRecordWithPerson
            ): Boolean {
                return oldItem === newItem
            }
        }

        val DIFFUTIL_INT = object: DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_CLAZZLOG = object  : DiffUtil.ItemCallback<ClazzLog>() {
            override fun areItemsTheSame(
                oldItem: ClazzLog,
                newItem: ClazzLog
            ): Boolean {
                return oldItem.clazzLogUid == newItem.clazzLogUid
            }

            //Two way binding within recycler view
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: ClazzLog,
                newItem: ClazzLog
            ): Boolean {
                return oldItem === newItem
            }
        }

        //The clazz logs at the top (clazzlog date selector) are never changed after loading, always return true
        val DIFFUTIL_CLAZZLOGLIST = object  : DiffUtil.ItemCallback<List<ClazzLog>>() {
            override fun areItemsTheSame(
                oldItem: List<ClazzLog>,
                newItem: List<ClazzLog>
            ): Boolean {
                return true
            }

            override fun areContentsTheSame(
                oldItem: List<ClazzLog>,
                newItem: List<ClazzLog>
            ): Boolean {
                return true
            }
        }

        val STATUS_MAP = mapOf(ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
            ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
            ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button)

    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzLogEditAttendanceScreen(
    uiState: ClazzLogEditAttendanceUiState = ClazzLogEditAttendanceUiState(),
    onClickMarkAll: (status: Int) -> Unit = {},
    onChangeClazzLog: (ClazzLog) -> Unit = {},
    onClazzLogAttendanceChanged: (ClazzLogAttendanceRecordWithPerson) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    )  {

        item {
            PagerView (
                list = uiState.clazzLogsList,
                timeZone = uiState.timeZone,
                onChangeClazzLog = onChangeClazzLog
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)
                },
                text = { Text(stringResource(id = R.string.mark_all_present)) },
                icon = {
                    Icon(
                        Icons.Outlined.LibraryAddCheck,
                        contentDescription = ""
                    )
                }
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT)
                },
                text = { Text(stringResource(id = R.string.mark_all_absent)) },
                icon = {
                    Icon(
                        Icons.Outlined.CheckBox,
                        contentDescription = ""
                    )
                }
            )
        }

        items(
            items = uiState.clazzLogAttendanceRecordList,
            key = {clazzLog -> clazzLog.clazzLogAttendanceRecordUid}
        ){ clazzLogAttendance ->
            ClazzLogItemView(
                clazzLog = clazzLogAttendance,
                fieldsEnabled = uiState.fieldsEnabled,
                onClazzLogAttendanceChanged = onClazzLogAttendanceChanged
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PagerView(
    list: List<ClazzLog>,
    uiStateIndex: Int,
    timeZone: String,
    onChangeClazzLog: (ClazzLog) -> Unit = {},
) {
    val pagerState = rememberPagerState(0)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            onChangeClazzLog(list[it])
        }
    }

    LaunchedEffect(uiStateIndex) {
        if(pagerState.currentPage != uiStateIndex) {
            pagerState.scrollToPage(pagerState.currentPage - 1)
        }
    }

    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){

        IconButton(
            onClick = {
                if (pagerState.currentPage > 0){
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            modifier = Modifier.weight(1F),
            enabled = pagerState.currentPage > 0
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = ""
            )
        }

        HorizontalPager(
            modifier = Modifier.weight(8F),
            state = pagerState,
            count = list.size
        ) { index ->

            val dateFormatted = rememberFormattedDateTime(
                timeInMillis = list[index].logDate,
                timeZoneId = timeZone
            )

            Text(dateFormatted)
        }

        IconButton(
            onClick = {
                if(pagerState.currentPage < list.size -1) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.weight(1F),
            enabled = pagerState.currentPage < list.size -1
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = ""
            )
        }
    }
}

private val buttonsIdMap = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
    ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzLogItemView(
    fieldsEnabled: Boolean,
    clazzLog: ClazzLogAttendanceRecordWithPerson,
    onClazzLogAttendanceChanged: (ClazzLogAttendanceRecordWithPerson) -> Unit
) {

    ListItem(
        text = {
            Text(text = clazzLog.person?.personFullName() ?: "",)
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_person_black_24dp),
                contentDescription = ""
            )
        },
        trailing = {

            AndroidView(factory = {  context ->
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_clazz_log_attendance_status_toggle_buttons,
                    null, false
                )

                buttonsIdMap.forEach { (status, buttonId) ->
                    val button = view.findViewById<Button>(buttonId)
                    button.isEnabled = fieldsEnabled

                    button.setOnClickListener {
                        onClazzLogAttendanceChanged(clazzLog.shallowCopy{
                            attendanceStatus = status
                        })
                    }
                }

                    view
                },
                update = {
                    val buttonGroup = it as MaterialButtonToggleGroup
                    buttonsIdMap[clazzLog.attendanceStatus]?.let {
                            it1 -> buttonGroup.check(it1)
                    }
                }
            )
        }
    )
}

@Composable
@Preview
fun ClazzLogEditAttendanceScreenPreview() {
    val uiState = ClazzLogEditAttendanceUiState(
        clazzLogAttendanceRecordList = listOf(
            ClazzLogAttendanceRecordWithPerson().apply {
                clazzLogAttendanceRecordUid = 0
                attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                person = Person().apply {
                    firstNames = "Student Name"
                }
            },
            ClazzLogAttendanceRecordWithPerson().apply {
                clazzLogAttendanceRecordUid = 1
                attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                person = Person().apply {
                    firstNames = "Student Name"
                }
            },
            ClazzLogAttendanceRecordWithPerson().apply {
                clazzLogAttendanceRecordUid = 2
                attendanceStatus = ClazzLogAttendanceRecord.STATUS_ABSENT
                person = Person().apply {
                    firstNames = "Student Name"
                }
            }
        ),
        clazzLogsList = listOf(
            ClazzLog().apply {
                logDate = 1671629979000
            },
            ClazzLog().apply {
                logDate = 1655608510000
            },
            ClazzLog().apply {
                logDate = 1671975579000
            }
        )
    )
    MdcTheme {
        ClazzLogEditAttendanceScreen(uiState)
    }
}