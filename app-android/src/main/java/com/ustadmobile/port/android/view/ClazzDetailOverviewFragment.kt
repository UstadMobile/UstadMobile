package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.viewmodel.ClazzDetailOverviewUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.composable.UstadClazzAssignmentListItem
import com.ustadmobile.port.android.view.composable.UstadContentEntryListItem
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.paddingCourseBlockIndent
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

interface ClazzDetailOverviewEventListener {
    fun onClickClassCode(code: String?)

    fun onClickShare()

    fun onClickDownloadAll()

    fun onClickPermissions()
}

class ClazzDetailOverviewFragment: UstadDetailFragment<ClazzWithDisplayDetails>(),
        ClazzDetailOverviewView,  Observer<PagedList<Schedule>>,
        ClazzDetailOverviewEventListener {



    private var mBinding: FragmentCourseDetailOverviewBinding? = null

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null

    private var detailRecyclerAdapter: CourseHeaderDetailRecyclerAdapter? = null

    private var scheduleHeaderAdapter: SimpleHeadingRecyclerAdapter? = null

    private var downloadRecyclerAdapter: CourseDownloadDetailRecyclerAdapter? = null

    private var courseImageAdapter: CourseImageAdapter? = null

    private var currentLiveData: LiveData<PagedList<Schedule>>? = null

    private var courseBlockLiveData: LiveData<PagedList<CourseBlockWithCompleteEntity>>? = null

    private var repo: UmAppDatabase? = null

    private var mScheduleListRecyclerAdapter: ScheduleRecyclerViewAdapter? = null

    private var courseBlockDetailRecyclerAdapter: CourseBlockDetailRecyclerViewAdapter? = null

    override var showPermissionButton: Boolean = false
        set(value) {
            downloadRecyclerAdapter?.permissionButtonVisible = value
            field = value
        }

    private val courseBlockObserver = Observer<PagedList<CourseBlockWithCompleteEntity>?> {
        t -> courseBlockDetailRecyclerAdapter?.submitList(t)
    }

    class ScheduleRecyclerViewAdapter: PagedListAdapter<Schedule,
            ScheduleRecyclerViewAdapter.ScheduleViewHolder>(SCHEDULE_DIFF_UTIL) {

        class ScheduleViewHolder(val binding: ItemScheduleSimpleBinding): RecyclerView.ViewHolder(binding.root)

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.binding.schedule = getItem(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            return ScheduleViewHolder(ItemScheduleSimpleBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }
    }

    class CourseBlockDetailRecyclerViewAdapter(
        var mPresenter: ClazzDetailOverviewPresenter?,
        private var lifecycleOwner: LifecycleOwner?,
        di: DI
    ): PagedListAdapter<CourseBlockWithCompleteEntity,
            RecyclerView.ViewHolder>(COURSE_BLOCK_DIFF_UTIL) {

        var timeZone: String? = null
            set(value){
                field = value
                boundViewHolders.forEach {
                    when(it){
                        is AssignmentCourseBlockViewHolder -> {
                            it.binding.timeZoneId = value
                        }
                    }
                }
            }

        private val boundViewHolders = mutableSetOf<RecyclerView.ViewHolder>()

        private val accountManager: UstadAccountManager by di.instance()

        private val appDatabase: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

        class ModuleCourseBlockViewHolder(val binding: ItemCourseBlockBinding): RecyclerView.ViewHolder(binding.root)

        class TextCourseBlockViewHolder(val binding: ItemTextCourseBlockBinding): RecyclerView.ViewHolder(binding.root)

        class AssignmentCourseBlockViewHolder(val binding: ItemAssignmentCourseBlockBinding): RecyclerView.ViewHolder(binding.root)

        class DiscussionCourseBlockViewHolder(val binding: ItemDiscussionBoardCourseBlockBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun getItemViewType(position: Int): Int {
            return getItem(position)?.cbType ?: 0
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val block = getItem(position)
            boundViewHolders += holder
            when(block?.cbType){
                CourseBlock.BLOCK_MODULE_TYPE -> {
                    val moduleHolder = (holder as ModuleCourseBlockViewHolder)
                    moduleHolder.binding.block = block
                    moduleHolder.binding.presenter = mPresenter
                }
                CourseBlock.BLOCK_TEXT_TYPE -> {
                    val textHolder = (holder as TextCourseBlockViewHolder)
                    textHolder.binding.block = block
                    textHolder.binding.presenter = mPresenter
                }
                CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                    val assignmentHolder = (holder as AssignmentCourseBlockViewHolder)
                    assignmentHolder.binding.assignment = block.assignment
                    assignmentHolder.binding.block = block
                    assignmentHolder.binding.presenter = mPresenter
                    assignmentHolder.binding.timeZoneId = timeZone
                    assignmentHolder.binding.dateTimeMode = MODE_START_OF_DAY
                }
                CourseBlock.BLOCK_CONTENT_TYPE -> {
                    val entryHolder = (holder as ContentEntryListRecyclerAdapter.ContentEntryListViewHolder)
                    val entry = block.entry
                    entryHolder.itemBinding.contentEntry = entry
                    entryHolder.itemBinding.itemListener = mPresenter
                    entryHolder.itemBinding.indentLevel = block.cbIndentLevel
                    if(entry != null) {
                        holder.downloadJobItemLiveData = RateLimitedLiveData(appDatabase, listOf("ContentJobItem"), 1000) {
                            appDatabase.contentEntryDao.statusForContentEntryList(entry.contentEntryUid)
                        }
                    }else{
                        holder.downloadJobItemLiveData = null
                    }
                }

                CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                    val discussionHolder = (holder as DiscussionCourseBlockViewHolder)
                    discussionHolder.binding.discussion = block.courseDiscussion
                    discussionHolder.binding.block = block
                    discussionHolder.binding.presenter = mPresenter


                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType){
                CourseBlock.BLOCK_MODULE_TYPE -> ModuleCourseBlockViewHolder(
                    ItemCourseBlockBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
                CourseBlock.BLOCK_CONTENT_TYPE ->
                    ContentEntryListRecyclerAdapter.ContentEntryListViewHolder(
                    ItemContentEntryListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), lifecycleOwner).apply {

                }
                CourseBlock.BLOCK_TEXT_TYPE -> TextCourseBlockViewHolder(
                    ItemTextCourseBlockBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
                CourseBlock.BLOCK_ASSIGNMENT_TYPE -> AssignmentCourseBlockViewHolder(
                    ItemAssignmentCourseBlockBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
                CourseBlock.BLOCK_DISCUSSION_TYPE -> DiscussionCourseBlockViewHolder(
                    ItemDiscussionBoardCourseBlockBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
                )
                else -> ModuleCourseBlockViewHolder(
                    ItemCourseBlockBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
            }

        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            boundViewHolders -= holder
        }
    }

    override var scheduleList: DataSource.Factory<Int, Schedule>? = null
        set(value) {
            currentLiveData?.removeObserver(this)
            field = value
            val scheduleDao = repo?.scheduleDao ?: return
            currentLiveData = value?.asRepositoryLiveData(scheduleDao)
            currentLiveData?.observe(this, this)
        }


    override var courseBlockList: DataSource.Factory<Int, CourseBlockWithCompleteEntity>? = null
        set(value) {
            courseBlockLiveData?.removeObserver(courseBlockObserver)
            field = value
            val blockDao = repo?.courseBlockDao ?: return
            courseBlockLiveData = value?.asRepositoryLiveData(blockDao)
            courseBlockLiveData?.observe(this, courseBlockObserver)
        }

    override fun onChanged(t: PagedList<Schedule>?) {
        scheduleHeaderAdapter?.visible = !t.isNullOrEmpty()
        mScheduleListRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View

        mBinding = FragmentCourseDetailOverviewBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_course_detail_overview)

        // 0
        courseImageAdapter = CourseImageAdapter()

        // 1
        downloadRecyclerAdapter = CourseDownloadDetailRecyclerAdapter(this)

        // 1
        detailRecyclerAdapter = CourseHeaderDetailRecyclerAdapter(this, di, requireContext())

        // 2
        scheduleHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.schedule).toString()).apply {
            visible = false
        }

        // 3
        mScheduleListRecyclerAdapter = ScheduleRecyclerViewAdapter()

        // 4
        courseBlockDetailRecyclerAdapter = CourseBlockDetailRecyclerViewAdapter(
            mPresenter, viewLifecycleOwner, di)


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
        mPresenter = ClazzDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        courseBlockDetailRecyclerAdapter?.mPresenter = mPresenter

        detailMergerRecyclerAdapter = ConcatAdapter(courseImageAdapter, downloadRecyclerAdapter,
            detailRecyclerAdapter, scheduleHeaderAdapter,
            mScheduleListRecyclerAdapter, courseBlockDetailRecyclerAdapter)

        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null

        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null

        courseImageAdapter = null
        downloadRecyclerAdapter = null
        detailRecyclerAdapter = null
        scheduleHeaderAdapter = null
        mScheduleListRecyclerAdapter = null
        courseBlockDetailRecyclerAdapter = null
        currentLiveData = null
        courseBlockLiveData = null

    }


    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            detailRecyclerAdapter?.clazz = value
            courseImageAdapter?.submitList(value?.let { listOf(it) } ?: listOf())
            courseBlockDetailRecyclerAdapter?.timeZone = value?.clazzTimeZone ?: value?.clazzSchool?.schoolTimeZone ?: "UTC"
        }

    override var clazzCodeVisible: Boolean
        get() = detailRecyclerAdapter?.clazzCodeVisible ?: false
        set(value) {
            detailRecyclerAdapter?.clazzCodeVisible = value
        }

    override fun onClickClassCode(code: String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", code)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    override fun onClickShare() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mPresenter?.deepLink)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onClickDownloadAll() {
        mPresenter?.handleDownloadAllClicked()
    }

    override fun onClickPermissions() {
        mPresenter?.handleClickPermissions()
    }

    companion object {

        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

        val SCHEDULE_DIFF_UTIL = object: DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }
        }

        val COURSE_BLOCK_DIFF_UTIL = object: DiffUtil.ItemCallback<CourseBlockWithCompleteEntity>() {
            override fun areItemsTheSame(oldItem: CourseBlockWithCompleteEntity, newItem: CourseBlockWithCompleteEntity): Boolean {
                return oldItem.cbUid == newItem.cbUid
            }

            override fun areContentsTheSame(oldItem: CourseBlockWithCompleteEntity, newItem: CourseBlockWithCompleteEntity): Boolean {
                var isSame = oldItem.cbType == newItem.cbType
                        && oldItem.cbTitle == newItem.cbTitle
                        && oldItem.cbDescription == newItem.cbDescription
                        && oldItem.expanded == newItem.expanded
                when(newItem.cbType){
                    CourseBlock.BLOCK_CONTENT_TYPE -> {
                        val newEntry = newItem.entry
                        val oldEntry = oldItem.entry
                        if(newEntry != null && oldEntry != null){
                            isSame = isSame && ContentEntryList2Fragment.DIFF_CALLBACK
                                .areContentsTheSame(oldEntry, newEntry)
                        }
                    }
                    CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                        val newAssignment = newItem.assignment
                        val oldAssignment = oldItem.assignment
                        isSame = isSame
                                && oldItem.cbDeadlineDate == newItem.cbDeadlineDate
                                && oldItem.cbMaxPoints == newItem.cbMaxPoints
                                && oldItem.cbLateSubmissionPenalty == newItem.cbLateSubmissionPenalty
                                && oldAssignment?.caTitle == newAssignment?.caTitle
                                && oldAssignment?.caDescription == newAssignment?.caDescription
                                && oldAssignment?.fileSubmissionStatus == newAssignment?.fileSubmissionStatus

                        val newMark = newAssignment?.mark
                        val oldMark = oldAssignment?.mark
                        isSame = isSame
                                && newMark?.averagePenalty == oldMark?.averagePenalty
                                && newMark?.averageScore == oldMark?.averageScore
                    }

                    CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                        val newDiscussion = newItem.courseDiscussion
                        val oldDiscussion = oldItem.courseDiscussion
                        isSame = isSame
                                && newDiscussion?.courseDiscussionTitle == oldDiscussion?.courseDiscussionTitle
                    }
                }
                return isSame
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzDetailOverviewScreen(
    uiState: ClazzDetailOverviewUiState = ClazzDetailOverviewUiState(),
    onClickClassCode: (String) -> Unit = {},
    onClickCourseDiscussion: (CourseDiscussion?) -> Unit = {},
    onClickCourseExpandCollapse: (CourseBlockWithCompleteEntity) -> Unit = {},
    onClickTextBlock: (CourseBlockWithCompleteEntity) -> Unit = {},
    onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit = {},
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
) {
    val numMembers = stringResource(R.string.x_teachers_y_students,
        uiState.clazz?.numTeachers ?: 0,
        uiState.clazz?.numStudents ?: 0)

    val clazzStartTime = rememberFormattedTime(
        timeInMs = (uiState.clazz?.clazzStartTime ?: 0).toInt()
    )

    val clazzEndTime = rememberFormattedTime(
        timeInMs = (uiState.clazz?.clazzEndTime ?: 0).toInt()
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ){

        item {
            Text(text = uiState.clazz?.clazzDesc ?: "")
        }

        item {
            UstadDetailField(
                modifier = Modifier.fillMaxWidth(),
                imageId = R.drawable.ic_group_black_24dp,
                valueText = numMembers,
                labelText = stringResource(R.string.members)
            )
        }

        item {
            if (uiState.clazzCodeVisible) {
                UstadDetailField(
                    imageId = R.drawable.ic_login_24px,
                    valueText = numMembers,
                    labelText = stringResource(R.string.class_code),
                    onClick = {
                        onClickClassCode(uiState.clazz?.clazzCode ?: "")
                    }
                )
            }
        }

        item {
            if (uiState.clazzSchoolUidVisible){
                TextImageRow(
                    imageId = R.drawable.ic_school_black_24dp,
                    text = uiState.clazz?.clazzSchool?.schoolName ?: ""
                )
            }
        }

        item {
            if (uiState.clazzDateVisible){
                TextImageRow(
                    imageId = R.drawable.ic_event_black_24dp,
                    text = "$clazzStartTime - $clazzEndTime"
                )
            }
        }

        item {
            if (uiState.clazzHolidayCalendarVisible){
                TextImageRow(
                    imageId = R.drawable.ic_event_black_24dp,
                    text = uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: ""
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = stringResource(id = R.string.schedule)
            )
        }

        items(
            items = uiState.scheduleList,
            key = { Pair(1, it.scheduleUid) }
        ){ schedule ->

            val fromTimeFormatted = rememberFormattedTime(
                timeInMs = schedule.sceduleStartTime.toInt()
            )
            val toTimeFormatted = rememberFormattedTime(
                timeInMs = schedule.scheduleEndTime.toInt()
            )
            val text = "${messageIdResource(id = schedule.scheduleFrequency)} " +
                    " ${messageIdResource(schedule.scheduleDay)} " +
                    " $fromTimeFormatted - $toTimeFormatted "

            ListItem(
                text = { Text(text) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(
            items = uiState.courseBlockList,
            key = { Pair(2, it.cbUid) }
        ){ courseBlock ->

            CourseBlockListItem(
                courseBlock = courseBlock,
                onClickCourseDiscussion = onClickCourseDiscussion,
                onClickCourseExpandCollapse = onClickCourseExpandCollapse,
                onClickTextBlock = onClickTextBlock,
                onClickAssignment = onClickAssignment,
                onClickContentEntry = onClickContentEntry,
                onClickDownloadContentEntry = onClickDownloadContentEntry
            )
        }
    }
}

@Composable
fun TextImageRow(
    imageId: Int,
    text: String,
){
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Image(
            painter = painterResource(id = imageId),
            contentDescription = null)

        Text(text)
    }
}

val ICON_SIZE = 40.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseBlockListItem(
    courseBlock: CourseBlockWithCompleteEntity,
    onClickCourseDiscussion: (CourseDiscussion?) -> Unit,
    onClickCourseExpandCollapse: (CourseBlockWithCompleteEntity) -> Unit,
    onClickTextBlock: (CourseBlockWithCompleteEntity) -> Unit,
    onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit,
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit,
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit,
){

    when(courseBlock.cbType){
        CourseBlock.BLOCK_MODULE_TYPE  -> {

            val trailingIcon = if(courseBlock.expanded)
                Icons.Default.KeyboardArrowUp
            else
                Icons.Default.KeyboardArrowDown
            ListItem(
                modifier = Modifier.paddingCourseBlockIndent(courseBlock.cbIndentLevel)
                    .clickable {
                        onClickCourseExpandCollapse(courseBlock)
                    },
                text = { Text(courseBlock.cbTitle ?: "") },
                secondaryText = { Text(courseBlock.cbDescription ?: "") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_folder_open_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                },
                trailing = {
                    Icon(trailingIcon, contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_DISCUSSION_TYPE -> {
            ListItem(
                modifier = Modifier.paddingCourseBlockIndent(courseBlock.cbIndentLevel)
                    .clickable {
                        onClickCourseDiscussion(courseBlock.courseDiscussion)
                    },
                text = { Text(courseBlock.cbTitle ?: "") },
                secondaryText = { Text(courseBlock.cbDescription ?: "") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_forum_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_TEXT_TYPE -> {
//            val cbDescription = if(courseBlock.cbDescription != null)
//                Html.fromHtml(courseBlock.cbDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
//            else
//                SpannedString.valueOf("")

            ListItem(
                modifier = Modifier.paddingCourseBlockIndent(courseBlock.cbIndentLevel)
                    .clickable {
                    onClickTextBlock(courseBlock)
                },
                text = { Text(courseBlock.cbTitle ?: "") },
                secondaryText = {  },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_title_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
            courseBlock.assignment?.let {
                UstadClazzAssignmentListItem(
                    assignment = it,
                    courseBlock = courseBlock,
                    onClickAssignment = onClickAssignment
                )
            }
        }
        CourseBlock.BLOCK_CONTENT_TYPE -> {
            courseBlock.entry?.let {
                UstadContentEntryListItem(
                    contentEntry = it,
                    onClickContentEntry = onClickContentEntry,
                    onClickDownloadContentEntry = onClickDownloadContentEntry
                )
            }
        }
    }
}

@Composable
@Preview
fun ClazzDetailOverviewScreenPreview() {
    val uiState = ClazzDetailOverviewUiState(
        clazz = ClazzWithDisplayDetails().apply {
            clazzDesc = "Description"
            clazzCode = "abc123"
            clazzSchoolUid = 1
            clazzStartTime = ((14 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
            clazzEndTime = System.currentTimeMillis()
            clazzSchool = School().apply {
                schoolName = "School Name"
            }
            clazzHolidayCalendar = HolidayCalendar().apply {
                umCalendarName = "Holiday Calendar"
            }
        },
        scheduleList = listOf(
            Schedule().apply {
                scheduleUid = 1
                sceduleStartTime = 0
                scheduleEndTime = 0
                scheduleFrequency = MessageID.yearly
                scheduleDay = MessageID.sunday
            },
            Schedule().apply {
                scheduleUid = 2
                sceduleStartTime = 0
                scheduleEndTime = 0
                scheduleFrequency = MessageID.yearly
                scheduleDay = MessageID.sunday
            }
        ),
        courseBlockList = listOf(
            CourseBlockWithCompleteEntity().apply {
                cbUid = 1
                cbTitle = "Module"
                cbDescription = "Description"
                cbType = CourseBlock.BLOCK_MODULE_TYPE
            },
            CourseBlockWithCompleteEntity().apply {
                cbUid = 2
                cbTitle = "Main discussion board"
                cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
            },
            CourseBlockWithCompleteEntity().apply {
                cbUid = 3
                cbDescription = "Description"
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                assignment = ClazzAssignmentWithMetrics().apply {
                    caTitle = "Assignment"
                    fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                    progressSummary = AssignmentProgressSummary().apply {
                        submittedStudents = 5
                        markedStudents = 10
                    }
                }
            },
            CourseBlockWithCompleteEntity().apply {
                cbUid = 4
                cbType = CourseBlock.BLOCK_CONTENT_TYPE
                entry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                    title = "Content Entry"
                    scoreProgress = ContentEntryStatementScoreProgress().apply {
                        success = StatementEntity.RESULT_SUCCESS
                        progress = 70
                    }
                }
            },
            CourseBlockWithCompleteEntity().apply {
                cbUid = 5
                cbTitle = "Text Block Module"
                cbDescription = "<pre>\n" +
                        "            GeeksforGeeks\n" +
                        "                         A Computer   Science Portal   For Geeks\n" +
                        "        </pre>"
                cbType = CourseBlock.BLOCK_TEXT_TYPE
            }
        ),
        clazzCodeVisible = true
    )
    MdcTheme {
        ClazzDetailOverviewScreen(uiState)
    }
}