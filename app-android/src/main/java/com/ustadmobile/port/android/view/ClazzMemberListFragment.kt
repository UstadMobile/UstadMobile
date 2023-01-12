package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzmemberListItemBinding
import com.toughra.ustadmobile.databinding.ItemClazzmemberPendingListItemBinding
import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.viewmodel.ClazzMemberListUiState
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzMemberListFragment() : UstadListViewFragment<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails>(),
        ClazzMemberListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var mPresenter: ClazzMemberListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithClazzEnrolmentDetails>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var studentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>? = null
        get() = field
        set(value) {
            val studentObserverVal = mStudentListObserver ?: return
            val repoDao = displayTypeRepo ?: return
            mCurrentStudentListLiveData?.removeObserver(studentObserverVal)
            mCurrentStudentListLiveData = value?.asRepositoryLiveData(repoDao)
            mCurrentStudentListLiveData?.observe(viewLifecycleOwner, studentObserverVal)
        }

    private val pendingStudentsObserver = object : Observer<PagedList<PersonWithClazzEnrolmentDetails>> {
        override fun onChanged(t: PagedList<PersonWithClazzEnrolmentDetails>?) {
            mPendingStudentListRecyclerViewAdapter?.submitList(t)
            mPendingStudentsHeaderRecyclerViewAdapter?.headerLayoutId = if (t != null && !t.isEmpty()) {
                R.layout.item_simple_list_header
            } else {
                0
            }
        }
    }

    override var pendingStudentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>? = null
        get() = field
        set(value) {
            val repoDao = displayTypeRepo ?: return

            mCurrentPendingStudentListLiveData?.removeObserver(pendingStudentsObserver)
            mCurrentStudentListLiveData = value?.asRepositoryLiveData(repoDao)
            mCurrentStudentListLiveData?.observe(viewLifecycleOwner, pendingStudentsObserver)
            field = value
        }


    private var mNewStudentListRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var mStudentListRecyclerViewAdapter: ClazzMemberListRecyclerAdapter? = null

    private var mStudentListObserver: Observer<PagedList<PersonWithClazzEnrolmentDetails>>? = null

    private var mCurrentStudentListLiveData: LiveData<PagedList<PersonWithClazzEnrolmentDetails>>? = null

    private var mPendingStudentsHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var mPendingStudentListRecyclerViewAdapter: PendingClazzEnrolmentListRecyclerAdapter? = null

    //private var mPendingStudentListObserver: Observer<PagedList<ClazzMemberWithPerson>>? = null

    private var mCurrentPendingStudentListLiveData: LiveData<PagedList<PersonWithClazzEnrolmentDetails>>? = null

    private var filterByClazzUid: Long = 0

    private val mOnClickAddStudent: View.OnClickListener = View.OnClickListener {
        mPresenter?.handlePickNewMemberClicked(ClazzEnrolment.ROLE_STUDENT)
    }

    private val mOnClickAddTeacher: View.OnClickListener = View.OnClickListener {
        mPresenter?.handlePickNewMemberClicked(ClazzEnrolment.ROLE_TEACHER)
    }

    override var addTeacherVisible: Boolean = false
        set(value) {
            field = value
            mUstadListHeaderRecyclerViewAdapter?.newItemVisible = value
        }

    override var addStudentVisible: Boolean = false
        set(value) {
            field = value
            mNewStudentListRecyclerViewAdapter?.newItemVisible = value
        }

    override var termMap: Map<String, String>? = null
        set(value) {
            field = value
            mUstadListHeaderRecyclerViewAdapter?.createNewText = value?.get(TerminologyKeys.ADD_TEACHER_KEY)

            mUstadListHeaderRecyclerViewAdapter?.headerStringText = value?.get(TerminologyKeys.TEACHERS_KEY)

            mNewStudentListRecyclerViewAdapter?.createNewText = value?.get(TerminologyKeys.ADD_STUDENT_KEY)

            mNewStudentListRecyclerViewAdapter?.headerStringText = value?.get(TerminologyKeys.STUDENTS_KEY)
        }

    class ClazzMemberListViewHolder(val itemBinding: ItemClazzmemberListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class ClazzMemberListRecyclerAdapter(var presenter: ClazzMemberListPresenter?)
        : SelectablePagedListAdapter<PersonWithClazzEnrolmentDetails, ClazzMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzMemberListViewHolder {
            val itemBinding = ItemClazzmemberListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzMemberListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzMemberListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.personWithEnrolmentDetails = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    class PendingClazzEnrolmentListViewHolder(val itemBinding: ItemClazzmemberPendingListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class PendingClazzEnrolmentListRecyclerAdapter(var presenter: ClazzMemberListPresenter?) : PagedListAdapter<PersonWithClazzEnrolmentDetails, PendingClazzEnrolmentListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingClazzEnrolmentListViewHolder {
            val itemBinding = ItemClazzmemberPendingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return PendingClazzEnrolmentListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PendingClazzEnrolmentListViewHolder, position: Int) {
            holder.itemBinding.clazzEnrolment = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        filterByClazzUid = arguments?.getString(ARG_CLAZZUID)?.toLong() ?: 0
        mPresenter = ClazzMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = ClazzMemberListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_a_teacher)
        mStudentListRecyclerViewAdapter = ClazzMemberListRecyclerAdapter(mPresenter).also {
            mStudentListObserver = PagedListSubmitObserver(it)
        }
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(mOnClickAddTeacher, createNewText,
                headerStringId = R.string.teachers_literal,
                headerLayoutId = R.layout.item_simple_list_header,
                filterOptions = ClazzMemberListPresenter.FILTER_OPTIONS.toListFilterOptions(requireContext(), di),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0),
                onFilterOptionSelected = mPresenter)
        val addStudentText = requireContext().getString(R.string.add_a_student)
        mNewStudentListRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(mOnClickAddStudent,
                addStudentText, headerStringId = R.string.students,
                headerLayoutId = R.layout.item_simple_list_header)

        mPendingStudentListRecyclerViewAdapter = PendingClazzEnrolmentListRecyclerAdapter(mPresenter)
        mPendingStudentsHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(null,
                "", R.string.pending_requests, headerLayoutId = 0)

        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
                mDataRecyclerViewAdapter, mNewStudentListRecyclerViewAdapter,
                mStudentListRecyclerViewAdapter, mPendingStudentsHeaderRecyclerViewAdapter,
                mPendingStudentListRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.visible = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        super.onClick(view)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        mNewStudentListRecyclerViewAdapter = null
        mStudentListRecyclerViewAdapter = null
        mStudentListObserver = null
        mCurrentStudentListLiveData = null
        mPendingStudentsHeaderRecyclerViewAdapter = null
        mPendingStudentListRecyclerViewAdapter = null
        mCurrentPendingStudentListLiveData = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithClazzEnrolmentDetails> = object
            : DiffUtil.ItemCallback<PersonWithClazzEnrolmentDetails>() {
            override fun areItemsTheSame(oldItem: PersonWithClazzEnrolmentDetails,
                                         newItem: PersonWithClazzEnrolmentDetails): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithClazzEnrolmentDetails,
                                            newItem: PersonWithClazzEnrolmentDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzMemberListScreen(
    uiState: ClazzMemberListUiState = ClazzMemberListUiState(),
    onClickEntry: (PersonWithClazzEnrolmentDetails) -> Unit = {},
    onClickPendingRequest: (enrolment: PersonWithClazzEnrolmentDetails,
                            approved: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            UstadListFilterChipsHeader(
                filterOptions = listOf(
                    MessageIdOption2(MessageID.currently_enrolled,
                        ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
                    MessageIdOption2(MessageID.past_enrollments,
                        ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
                    MessageIdOption2(MessageID.all, 0),
                ),
                selectedChipId = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
            )
        }

        item {
            UstadListSortHeader(
                SortOrderOption(
                    MessageID.name,
                    ClazzDaoCommon.SORT_CLAZZNAME_ASC,
                    true
                )
            )
        }

        item {
            ListItem(
                text = { Text(text = stringResource(id = R.string.teachers_literal)) }
            )
        }

        item {
            if (uiState.addTeacherVisible){
                AddListItem(
                    text = stringResource(id = R.string.add_a_teacher),
                    onClick = {},
                )
            }
        }

        items(
            items = uiState.teacherList,
            key = { Pair(1, it.personUid) }
        ){ person ->
            ListItem (
                text = {
                    Text(text = "${person.firstNames} ${person.lastName}")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            ListItem(
                text = { Text(text = stringResource(id = R.string.students)) }
            )
        }

        item {
            if (uiState.addStudentVisible){
                AddListItem(
                    text = stringResource(id = R.string.add_a_student),
                    onClick = {},
                )
            }
        }

        items(
            items = uiState.studentList,
            key = { Pair(2, it.personUid) }
        ){ personItem ->
            StudentListItem(
                person = personItem,
                onClick = onClickEntry
            )
        }

        item {
            ListItem(
                text = { Text(text = stringResource(id = R.string.pending_requests)) }
            )
        }
        
        items(
            items = uiState.pendingStudentList,
            key = { Pair(3, it.personUid) }
        ){ pendingStudent ->
            PendingStudentListItem(
                person = pendingStudent,
                onClick = onClickPendingRequest
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddListItem(
    text: String,
    onClick: () -> Unit,
){
    ListItem(
        modifier = Modifier.clickable {
            onClick()
        },
        icon = {
               Icon(
                   Icons.Filled.PersonAdd,
                   contentDescription = ""
               )
        },
        text = { Text(text = text) }
    )
}

 @OptIn(ExperimentalMaterialApi::class)
 @Composable
 fun StudentListItem(
     person: PersonWithClazzEnrolmentDetails,
     onClick: (PersonWithClazzEnrolmentDetails) -> Unit,
 ){

     val statusColor = if ((person.attendance/100) >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_GOOD)
         R.color.successColor
     else if ((person.attendance/100)
         >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_WARNING)
         R.color.attendanceWarningColor
     else
         R.color.errorColor

     ListItem (
         modifier = Modifier.clickable {
             onClick(person)
         },
         text = {
             Text(text = "${person.firstNames} ${person.lastName}")
         },
         secondaryText = {
             Row(
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.spacedBy(5.dp)
             ) {
                 Icon(
                     Icons.Filled.Lens,
                     contentDescription = "",
                     tint = colorResource(id = statusColor),
                     modifier = Modifier.size(15.dp)
                 )
                 Text(stringResource(id = R.string.x_percent_attended, person.attendance))
             }
         },
         icon = {
             Icon(
                 imageVector = Icons.Filled.AccountCircle,
                 contentDescription = null
             )
         }
     )
 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PendingStudentListItem(
    person: PersonWithClazzEnrolmentDetails,
    onClick: (enrolment: PersonWithClazzEnrolmentDetails, approved: Boolean) -> Unit
){
    ListItem (
        text = {
            Text(text = "${person.firstNames} ${person.lastName}")
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null
            )
        },
        trailing = {
            Row {
                IconButton(
                    onClick = {
                        onClick(person, true)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.exo_ic_check),
                        contentDescription = stringResource(R.string.accept)
                    )
                }
                IconButton(
                    onClick = {
                        onClick(person, false)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_black_24dp),
                        contentDescription = stringResource(R.string.reject)
                    )
                }
            }
        }
    )
}

@Composable
@Preview
fun ClazzMemberListScreenPreview() {
    val uiStateVal = ClazzMemberListUiState(
        studentList = listOf(
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 1
                firstNames = "Student 1"
                lastName = "Name"
                attendance = 20F
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 2
                firstNames = "Student 2"
                lastName = "Name"
                attendance = 80F
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 3
                firstNames = "Student 3"
                lastName = "Name"
                attendance = 65F
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 4
                firstNames = "Student 4"
                lastName = "Name"
            }
        ),
        teacherList = listOf(
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 1
                firstNames = "Teacher 1"
                lastName = "Name"
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 2
                firstNames = "Teacher 2"
                lastName = "Name"
            }
        ),
        pendingStudentList = listOf(
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 1
                firstNames = "Student 1"
                lastName = "Name"
                attendance = 20F
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 2
                firstNames = "Student 2"
                lastName = "Name"
                attendance = 80F
            }
        ),
        teacherList = listOf(
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 1
                firstNames = "Teacher 1"
                lastName = "Name"
            },
            PersonWithClazzEnrolmentDetails().apply {
                personUid = 2
                firstNames = "Teacher 2"
                lastName = "Name"
            }
        ),
        addStudentVisible = true,
        addTeacherVisible = true
    )

    MdcTheme {
        ClazzMemberListScreen(uiStateVal, {},
            {enrolment: PersonWithClazzEnrolmentDetails,
             approved: Boolean ->  {}}
        )
    }
}