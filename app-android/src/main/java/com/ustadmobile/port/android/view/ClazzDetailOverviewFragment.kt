package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
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
        ClazzDetailOverviewView, ClazzDetailFragmentEventHandler, Observer<PagedList<Schedule>>,
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

        private val appDatabase: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

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
        repo = di.direct.on(accountManager.activeAccount).instance(tag = TAG_REPO)
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
                                && newMark?.camPenalty == oldMark?.camPenalty
                                && newMark?.camMark == oldMark?.camMark
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