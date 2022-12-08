package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzEditBinding
import com.toughra.ustadmobile.databinding.ItemScheduleBinding
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.locale.entityconstants.DayConstants
import com.ustadmobile.core.impl.locale.entityconstants.EnrolmentPolicyConstants
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.viewmodel.ClazzEditUiState
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN
import com.ustadmobile.port.android.view.binding.ImageViewLifecycleObserver2
import com.ustadmobile.port.android.view.binding.MODE_END_OF_DAY
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.composable.*
import java.util.*

interface ClazzEditFragmentEventHandler {

    fun onAddCourseBlockClicked()

    fun handleAttendanceClicked(isChecked: Boolean)
}
class ClazzEditFragment() : UstadEditFragment<ClazzWithHolidayCalendarAndSchoolAndTerminology>(),
        ClazzEdit2View, ClazzEditFragmentEventHandler,
        TitleDescBottomSheetOptionSelectedListener {

    private var bottomSheetOptionList: List<TitleDescBottomSheetOption> = listOf()
    private var mDataBinding: FragmentClazzEditBinding? = null

    private var mPresenter: ClazzEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWithHolidayCalendarAndSchoolAndTerminology>?
        get() = mPresenter

    private var scheduleRecyclerAdapter: ScheduleRecyclerAdapter? = null

    private var scheduleRecyclerView: RecyclerView? = null

    private var courseBlockRecyclerAdapter: CourseBlockRecyclerAdapter? = null

    private var courseBlockRecyclerView: RecyclerView? = null

    private val scheduleObserver = Observer<List<Schedule>?> {
        t -> scheduleRecyclerAdapter?.submitList(t)
    }

    private val courseBlockObserver = Observer<List<CourseBlockWithEntity>?> {
        t -> courseBlockRecyclerAdapter?.dataSet = t
    }

    override var clazzSchedules: MutableLiveData<List<Schedule>>? = null
        set(value) {
            field?.removeObserver(scheduleObserver)
            field = value
            value?.observe(this, scheduleObserver)
        }


    override var courseBlocks: MutableLiveData<List<CourseBlockWithEntity>>? = null
        set(value) {
            field?.removeObserver(courseBlockObserver)
            field = value
            value?.observe(this, courseBlockObserver)
        }

    override var clazzEndDateError: String? = null
        get() = field
        set(value) {
            field = value
            mDataBinding?.clazzEndDateError = value
        }
    override var clazzStartDateError: String? = null
        get() = field
        set(value) {
            field = value
            mDataBinding?.clazzStartDateError = value
        }


    override var enrolmentPolicyOptions: List<ClazzEdit2Presenter.EnrolmentPolicyOptionsMessageIdOption>? = null
        set(value){
            field = value
            mDataBinding?.enrolmentPolicy = value
        }

    private var imageViewLifecycleObserver: ImageViewLifecycleObserver2? = null

    override var coursePicture: CoursePicture?
        get() = mDataBinding?.coursePicture
        set(value) {
            mDataBinding?.coursePicture = value
        }

    class ScheduleRecyclerAdapter(
        var oneToManyEditListener: OneToManyJoinEditListener<Schedule>?,
        var presenter: ClazzEdit2Presenter?
    ): ListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>(DIFF_CALLBACK_SCHEDULE) {

        class ScheduleViewHolder(val binding: ItemScheduleBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            val viewHolder = ScheduleViewHolder(ItemScheduleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.oneToManyJoinListener = oneToManyEditListener
            return viewHolder
        }

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.binding.schedule = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)

            oneToManyEditListener = null
            presenter = null
        }
    }



    override var entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null
        get() = field
        set(value) {
            mDataBinding?.clazz = value
            mDataBinding?.dateTimeMode = MODE_START_OF_DAY
            mDataBinding?.dateTimeModeEnd = MODE_END_OF_DAY
            mDataBinding?.timeZoneId = value?.clazzTimeZone?:value?.school?.schoolTimeZone?:"UTC"
            field = value
        }


    override var fieldsEnabled: Boolean = true
        set(value) {
            super.fieldsEnabled = value
            field = value
            mDataBinding?.fieldsEnabled = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View

        mDataBinding = FragmentClazzEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.featuresBitmaskFlags = BitmaskEditPresenter.FLAGS_AVAILABLE
            it.activityEventHandler = this
        }

        scheduleRecyclerView = rootView.findViewById(R.id.activity_clazz_edit_schedule_recyclerview)
        courseBlockRecyclerView = rootView.findViewById(R.id.activity_clazz_edit_course_block_recyclerview)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_a_new_course, R.string.edit_course)


        imageViewLifecycleObserver = ImageViewLifecycleObserver2(
            requireActivity().activityResultRegistry,null, 1
        ).also {
            mDataBinding?.imageViewLifecycleObserver = it
            viewLifecycleOwner.lifecycle.addObserver(it)
        }

        mPresenter = ClazzEdit2Presenter(requireContext(), arguments.toStringMap(), this@ClazzEditFragment,
            di, viewLifecycleOwner).withViewLifecycle()

        mDataBinding?.scheduleOneToManyListener = mPresenter?.scheduleOneToManyJoinListener
        mDataBinding?.mPresenter = mPresenter
        scheduleRecyclerAdapter = ScheduleRecyclerAdapter(
            mPresenter?.scheduleOneToManyJoinListener, mPresenter)

        scheduleRecyclerView?.adapter = scheduleRecyclerAdapter
        scheduleRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mDataBinding?.courseBlockOneToManyListener = mPresenter
        courseBlockRecyclerAdapter = CourseBlockRecyclerAdapter(
                mPresenter, mDataBinding?.activityClazzEditCourseBlockRecyclerview)

        courseBlockRecyclerView?.adapter = courseBlockRecyclerAdapter
        courseBlockRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        bottomSheetOptionList = listOf(
                TitleDescBottomSheetOption(
                        requireContext().getString(R.string.module),
                        requireContext().getString(R.string.course_module),
                        CourseBlock.BLOCK_MODULE_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(R.string.text),
                        requireContext().getString(R.string.formatted_text_to_show_to_course_participants),
                        CourseBlock.BLOCK_TEXT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(R.string.content),
                        requireContext().getString(R.string.add_course_block_content_desc),
                        CourseBlock.BLOCK_CONTENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(R.string.assignments),
                        requireContext().getString(R.string.add_assignment_block_content_desc),
                        CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(R.string.discussion_board),
                        requireContext().getString(R.string.add_discussion_board_desc),
                        CourseBlock.BLOCK_DISCUSSION_TYPE),
        )

        mPresenter?.onCreate(backStackSavedState)

    }

    override fun onAddCourseBlockClicked() {
        val sheet = TitleDescBottomSheetOptionFragment(bottomSheetOptionList, this)
        sheet.show(childFragmentManager, sheet.tag)
    }

    override fun handleAttendanceClicked(isChecked: Boolean) {
        val clazz = mDataBinding?.clazz
        clazz?.clazzFeatures = if(isChecked) Clazz.CLAZZ_FEATURE_ATTENDANCE else 0
        mDataBinding?.clazz = clazz
    }

    override fun onBottomSheetOptionSelected(optionSelected: TitleDescBottomSheetOption) {
        when(optionSelected.optionCode) {
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> mPresenter?.handleClickAddAssignment()
            CourseBlock.BLOCK_MODULE_TYPE -> mPresenter?.handleClickAddModule()
            CourseBlock.BLOCK_CONTENT_TYPE -> mPresenter?.handleClickAddContent()
            CourseBlock.BLOCK_TEXT_TYPE -> mPresenter?.handleClickAddText()
            CourseBlock.BLOCK_DISCUSSION_TYPE -> mPresenter?.handleClickAddDiscussion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding?.activityClazzEditScheduleRecyclerview?.adapter = null
        mDataBinding?.activityEventHandler = null
        mDataBinding = null
        scheduleRecyclerView = null
        scheduleRecyclerAdapter = null
        courseBlockRecyclerView = null
        courseBlockRecyclerAdapter = null
        courseBlocks = null
        clazzSchedules = null
        mPresenter = null
    }

    companion object {

        @JvmField
        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

        @JvmField
        val BLOCK_WITH_ENTRY_MAP = BLOCK_ICON_MAP + ContentEntryList2Fragment.CONTENT_ENTRY_TYPE_ICON_MAP


        val DIFF_CALLBACK_SCHEDULE: DiffUtil.ItemCallback<Schedule> = object: DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ClazzEditScreen(
    uiState: ClazzEditUiState = ClazzEditUiState(),
    onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit = {},
    onClickSchool: () -> Unit = {},
    onClickTimezone: () -> Unit = {},
    onClickAddCourseBlock: () -> Unit = {},
    onClickAddSchedule: () -> Unit = {},
    onClickEditSchedule: (Schedule) -> Unit = {},
    onClickDeleteSchedule: (Schedule) -> Unit = {},
    onClickHolidayCalendar: () -> Unit = {},
    onCheckedAttendance: (Boolean) -> Unit = {},
    onClickTerminology: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            Text(text = stringResource(id = R.string.basic_details),
                style = Typography.h6
            )
        }
        
        item { 
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.clazzName ?: "",
                label = stringResource(id = R.string.name),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzName = it
                        }
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.clazzDesc ?: "",
                label = stringResource(id = R.string.description).addOptionalSuffix(),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzDesc = it
                        }
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.school?.schoolName ?: "",
                label = stringResource(id = R.string.institution),
                enabled = uiState.fieldsEnabled,
                onClick = onClickSchool,
                onValueChange = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadDateEditTextField(
                value = uiState.entity?.clazzStartTime ?: 0,
                label = stringResource(id = R.string.start_date),
                error = uiState.clazzStartDateError,
                enabled = uiState.fieldsEnabled,
                timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzStartTime = it
                        }
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadDateEditTextField(
                value = uiState.entity?.clazzEndTime ?: 0,
                label = stringResource(id = R.string.end_date).addOptionalSuffix(),
                error = uiState.clazzEndDateError,
                enabled = uiState.fieldsEnabled,
                timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzEndTime = it
                        }
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.clazzTimeZone ?: "",
                label = stringResource(id = R.string.timezone),
                enabled = uiState.fieldsEnabled,
                onClick = { onClickTimezone() },
                onValueChange = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(text = stringResource(id = R.string.course_blocks))
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddCourseBlock()
                },
                text = { Text(stringResource(id = R.string.add_block)) },
            )
        }

        items (
            items = uiState.courseBlockList
        ) { courseBlock ->

            val courseBlockEditAlpha: Float = if (courseBlock.cbHidden) 0.5f else 1f

            ListItem(
                modifier = Modifier.alpha(courseBlockEditAlpha),
                icon = {
                    Row {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null
                        )
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null
                        )
                    }
                },
                text = { Text("ListItem text") },
                trailing = {
                    PopUpMenu()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(text = stringResource(id = R.string.schedule))
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            TextButton(
                onClick = onClickAddSchedule,
            ) {
                Row {
                    Icon(Icons.Filled.Add,"contentDescription")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.add_a_schedule).uppercase())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(
            uiState.clazzSchedules.size
        ){

            val fromTimeFormatted = rememberFormattedTime(timeInMs = uiState.clazzSchedules[it].sceduleStartTime.toInt())
            val toTimeFormatted = rememberFormattedTime(timeInMs = uiState.clazzSchedules[it].scheduleEndTime.toInt())
            val text = "${messageIdResource(id = uiState.clazzSchedules[it].scheduleFrequency)} " +
                    " ${messageIdResource(uiState.clazzSchedules[it].scheduleDay)} " +
                    " $fromTimeFormatted - $toTimeFormatted "

            UstadDetailField(
                valueText = text,
                labelText = "",
                onClick = { onClickEditSchedule(uiState.clazzSchedules[it]) },
                secondaryActionContent = {
                    IconButton(
                        onClick = { onClickDeleteSchedule(uiState.clazzSchedules[it]) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                        )
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.holidayCalendar?.umCalendarName ?: "",
                label = stringResource(id = R.string.holiday_calendar),
                enabled = uiState.fieldsEnabled,
                onValueChange = {},
                onClick = onClickHolidayCalendar
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(text = stringResource(id = R.string.course_setup))
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadSwitchField(
                label = stringResource(id = R.string.attendance),
                checked = uiState.clazzEditAttendanceChecked,
                onChange = { onCheckedAttendance(it) },
                enabled = uiState.fieldsEnabled
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.entity?.clazzEnrolmentPolicy ?: 0,
                label = stringResource(R.string.enrolment_policy),
                options = EnrolmentPolicyConstants.ENROLMENT_POLICY_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onClazzChanged(uiState.entity?.shallowCopy{
                        clazzEnrolmentPolicy = it.value
                    })
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.terminology?.ctTitle ?: "",
                label = stringResource(id = R.string.terminology),
                enabled = uiState.fieldsEnabled,
                onValueChange = {},
                onClick = onClickTerminology
            )
        }
    }
}

@Composable
fun PopUpMenu() {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .wrapContentSize(Alignment.TopStart)) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                Text("Refresh")
            }
            DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                Text("Settings")
            }
            Divider()
            DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
                Text("Send Feedback")
            }
        }
    }
}

@Composable
@Preview
fun ClazzEditScreenPreview() {
    val uiState = ClazzEditUiState(
        entity = ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {

        },
        clazzSchedules = listOf(
            Schedule().apply {
                sceduleStartTime = 0
                scheduleEndTime = 0
                scheduleFrequency = MessageID.yearly
                scheduleDay = MessageID.sunday
            }
        ),
        courseBlockList = listOf(
            CourseBlockWithEntity().apply {
                cbTitle = "First"
            },
            CourseBlockWithEntity().apply {
                cbTitle = "Second"
            },
            CourseBlockWithEntity().apply {
                cbTitle = "Third"
            }
        )
    )
    MdcTheme {
        ClazzEditScreen(uiState)
    }
}