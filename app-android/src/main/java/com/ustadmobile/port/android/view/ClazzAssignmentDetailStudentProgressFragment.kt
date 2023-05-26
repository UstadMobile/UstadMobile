package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressPresenter
import com.ustadmobile.core.controller.FileSubmissionListItemListener
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailStudentProgressUiState
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ClazzAssignmentDetailStudentProgressFragmentEventHandler {

    fun onSubmitGradeClicked()

    fun onSubmitGradeAndMarkNextClicked()

}

class ClazzAssignmentDetailStudentProgressFragment(): UstadDetailFragment<ClazzAssignmentWithCourseBlock>(),
        ClazzAssignmentDetailStudentProgressView, ClazzAssignmentDetailStudentProgressFragmentEventHandler,
        OpenSheetListener, FileSubmissionListItemListener {

    private var marksAdapter: GradesListAdapter? = null
    private var gradesHeaderAdapter: GradesHeaderAdapter? = null
    private var dbRepo: UmAppDatabase? = null

    val accountManager: UstadAccountManager by instance()
    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    private var submissionsHeaderAdapter: SimpleHeadingRecyclerAdapter? = null
    private var submissionStatusHeaderAdapter: SubmissionStatusHeaderAdapter? = null

    private var submissionAdapter: SubmissionAdapter? = null

    private var submissionAttachmentLiveDataCourse: LiveData<PagedList<CourseAssignmentSubmissionWithAttachment>>? = null
    private val fileSubmissionObserver = Observer<PagedList<CourseAssignmentSubmissionWithAttachment>?> {
        t -> run {
        markSubmissionAdapter?.markStudentVisible = !t.isEmpty()
        submissionAdapter?.submitList(t)
    }
    }

    private var courseMarkLiveData: LiveData<PagedList<CourseAssignmentMarkWithPersonMarker>>? = null
    private val courseMarkObserver = Observer<PagedList<CourseAssignmentMarkWithPersonMarker>?> {
            t -> run {
        gradesHeaderAdapter?.visible = t.isNotEmpty()
        marksAdapter?.submitList(t)
    }
    }


    private var markSubmissionAdapter: MarkFileSubmissionAdapter? = null

    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
//        mBinding = FragmentClazzAssignmentDetailOverviewBinding.inflate(inflater, container, false).also {
//            rootView = it.root
//        }


        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        mPresenter = ClazzAssignmentDetailStudentProgressPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

//        detailMergerRecyclerView =
//                rootView.findViewById(R.id.fragment_clazz_assignment_detail_overview)

        // 1
        submissionsHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.submissions).toString()).apply {
            visible = true
        }

        // 2
        submissionStatusHeaderAdapter = SubmissionStatusHeaderAdapter().apply {
            visible = true
        }

        // 3 file submission for student
        submissionAdapter = SubmissionAdapter(this).also{
            it.isSubmitted = true
            it.visible = true
        }

        // 4 mark grade
        markSubmissionAdapter = MarkFileSubmissionAdapter(this)

        gradesHeaderAdapter = GradesHeaderAdapter(
            onFilterOptionSelected = mPresenter)

        marksAdapter = GradesListAdapter()

        // 5 - Private
        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        ).apply {
            visible = false
        }

        // 6 - New Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false).apply{
            visible = false
        }

        //7 - Private comments list
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        detailMergerRecyclerAdapter = ConcatAdapter(
            submissionsHeaderAdapter, submissionStatusHeaderAdapter, submissionAdapter,
            markSubmissionAdapter, gradesHeaderAdapter, marksAdapter,
            privateCommentsHeadingRecyclerAdapter, newPrivateCommentRecyclerAdapter,
            privateCommentsRecyclerAdapter
        )
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        TODO("Will be removed")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mPresenter = null
        entity = null

        detailMergerRecyclerView?.adapter = null
        submissionsHeaderAdapter = null

        privateCommentsLiveData = null
        newPrivateCommentRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null
        privateCommentsHeadingRecyclerAdapter = null
        detailMergerRecyclerView = null
    }

    override fun open(publicComment: Boolean) {
//        val sendCommentSheet = CommentsBottomSheet(publicComment, requireContext().getString(R.string.add_private_comment),
//                accountManager.activeAccount.personUid,  mPresenter?.newPrivateCommentListener)
//        sendCommentSheet.show(childFragmentManager, sendCommentSheet.tag)
    }

    override var gradeFilterChips: List<ListFilterIdOption>? = null
        set(value) {
            field = value
            gradesHeaderAdapter?.filterOptions = value
        }


    override var clazzCourseAssignmentSubmissionAttachment: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            submissionAttachmentLiveDataCourse?.removeObserver(fileSubmissionObserver)
            submissionAttachmentLiveDataCourse = value?.asRepositoryLiveData(dbRepoVal.courseAssignmentSubmissionDao)
            field = value
            submissionAttachmentLiveDataCourse?.observeIfFragmentViewIsReady(this, fileSubmissionObserver)
        }


    override var markList: DataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            courseMarkLiveData?.removeObserver(courseMarkObserver)
            courseMarkLiveData = value?.asRepositoryLiveData(dbRepoVal.courseAssignmentMarkDao)
            field = value
            courseMarkLiveData?.observeIfFragmentViewIsReady(this, courseMarkObserver)
        }


    override var clazzAssignmentPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepoVal.commentsDao)
            privateCommentsLiveData?.observeIfFragmentViewIsReady(this, privateCommentsObserverVal)
            field = value
        }

    override var markNextStudentVisible: Boolean = false
        set(value) {
            field = value
            markSubmissionAdapter?.markNextStudentVisible = value
        }

    override var submitButtonVisible: Boolean = false
        set(value) {
            field = value
            markSubmissionAdapter?.markStudentVisible = value
        }

    override var submitMarkError: String? = null
        set(value) {
            field = value
            markSubmissionAdapter?.submitMarkError = value
        }

    override var submitterName: String? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value
        }

    override var submissionScore: AverageCourseAssignmentMark? = null
        get() = field
        set(value) {
            field = value
            submissionStatusHeaderAdapter?.courseAssignmentMark = value
            markSubmissionAdapter?.mark = value
        }

    override var submissionStatus: Int = 0
        set(value) {
            field = value
            submissionStatusHeaderAdapter?.assignmentStatus = value
        }

    override var entity: ClazzAssignmentWithCourseBlock? = null
        get() = field
        set(value) {
            field = value
            newPrivateCommentRecyclerAdapter?.visible = value?.caPrivateCommentsEnabled ?: false
            privateCommentsHeadingRecyclerAdapter?.visible = value?.caPrivateCommentsEnabled ?: false
            markSubmissionAdapter?.assignment = value
            submissionAdapter?.assignment = value
            submissionStatusHeaderAdapter?.assignment = value
            marksAdapter?.courseblock = value?.block
        }

    override fun onSubmitGradeClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        val comment = markSubmissionAdapter?.comment
        mPresenter?.onClickSubmitGrade(grade, comment)
    }

    override fun onSubmitGradeAndMarkNextClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        val comment = markSubmissionAdapter?.comment
        mPresenter?.onClickSubmitGradeAndMarkNext(grade, comment)
    }

    override fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        // cant delete here
    }

    override fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        mPresenter?.onClickOpenSubmission(submissionCourse)
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzAssignmentDetailStudentProgressScreen(
    uiState: ClazzAssignmentDetailStudentProgressUiState,
    onClickSubmitGrade: () -> Unit = {},
    onClickSubmitGradeAndMarkNext: () -> Unit = {},
    onAddComment: (String) -> Unit = {},
    onAddMark: (String) -> Unit = {},
    onClickNewPrivateComment: () -> Unit = {},
    onClickGradeFilterChip: (MessageIdOption2) -> Unit = {},
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {},
){

    val markFileSubmissionSubmitGradeAndNextText = if (uiState.submissionScore == null)
        R.string.submit_grade_and_mark_next
    else
        R.string.update_grade_and_mark_next

    val markFileSubmissionSubmitGradeText = if (uiState.submissionScore == null)
        R.string.submit_grade
    else
        R.string.update_grade

    LazyColumn (
        modifier = Modifier
            .defaultScreenPadding()
            .fillMaxSize()
    ){

        item {
            Text(
                text = stringResource(id = R.string.submissions),
                modifier = Modifier.defaultItemPadding()
            )
        }

        item {
            UstadAssignmentSubmissionHeader(
                uiState = uiState.submissionHeaderUiState
            )
        }

        items(
            items = uiState.submissionList,
            key = { submission -> submission.casUid }
        ){ submission ->
            UstadAssignmentSubmissionListItem(
                submission = submission,
                onClickOpenSubmission = onClickOpenSubmission
            )
        }


        item {
            Text(
                text = stringResource(id = R.string.grades_class_age),
                modifier = Modifier.defaultItemPadding()
            )
        }

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.gradeFilterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickGradeFilterChip
            )
        }

        if (uiState.markStudentVisible){
            item {
                UstadTextEditField(
                    modifier = Modifier.defaultItemPadding(),
                    value = "",
                    label = stringResource(id = R.string.comment),
                    enabled = uiState.fieldsEnabled,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    onValueChange = { comment ->
                        onAddComment(comment)
                    }
                )
            }
        }


        if (uiState.markStudentVisible){
            item {
                UstadTextEditField(
                    modifier = Modifier.defaultItemPadding(),
                    value = "",
                    label = stringResource(id = R.string.points).capitalizeFirstLetter()
                            + (uiState.assignment?.block?.cbMaxPoints ?: 0),
                    error = uiState.submitMarkError,
                    enabled = uiState.fieldsEnabled,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    onValueChange = { mark ->
                        onAddMark(mark)
                    }
                )
            }
        }

        if (uiState.markStudentVisible){
            item {
                Button(
                    onClick = onClickSubmitGrade,
                    enabled = uiState.markNextStudentVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.primaryColor)
                    )
                ) {
                    Text(
                        stringResource(markFileSubmissionSubmitGradeText).uppercase(),
                        color = contentColorFor(
                            colorResource(id = R.color.primaryColor)
                        )
                    )

                }
            }
        }

        if (uiState.markNextStudentVisible){
            item {
                OutlinedButton(
                    onClick = onClickSubmitGradeAndMarkNext,
                    enabled = uiState.markNextStudentVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                ) {
                    Text(stringResource(markFileSubmissionSubmitGradeAndNextText).uppercase())
                }
            }
        }

        items(
            items = uiState.markList,
            key = { mark -> mark.camUid }
        ){ mark ->
            UstadCourseAssignmentMarkListItem(
                uiState = UstadCourseAssignmentMarkListItemUiState(
                    mark = mark,
                    block = uiState.assignment?.block ?: CourseBlock()
                )
            )
        }

        item {
            ListItem(
                text = {Text(stringResource(R.string.private_comments))}
            )
        }

        item {
            UstadAddCommentListItem(
                text = stringResource(id = R.string.add_private_comment),
                enabled = uiState.fieldsEnabled,
                personUid = 0,
                onClickAddComment = { onClickNewPrivateComment() }
            )
        }

        items(
            items = uiState.privateCommentsList,
            key = { Pair(5, it.commentsUid) }
        ){ comment ->
            UstadCommentListItem(commentWithPerson = comment)
        }

    }
}

@Composable
@Preview
fun ClazzAssignmentDetailStudentProgressScreenPreview(){

    val uiState = ClazzAssignmentDetailStudentProgressUiState(
        submissionHeaderUiState = UstadAssignmentSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.MARKED,
            assignmentMark = AverageCourseAssignmentMark().apply {
                averagePenalty = 12
            }
        ),
        submissionList = listOf(
            CourseAssignmentSubmissionWithAttachment().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                attachment = CourseAssignmentSubmissionAttachment().apply {
                    casaFileName = "Content Title"
                }
            },
        ),
        markList = listOf(
            CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3
                }
            }
        ),
        privateCommentsList = listOf(
            CommentsWithPerson().apply {
                commentsUid = 1
                commentsPerson = Person().apply {
                    firstNames = "Bob"
                    lastName = "Dylan"
                }
                commentsText = "I like this activity. Shall we discuss this in our next meeting?"
            }
        ),
    )


    MdcTheme {
        ClazzAssignmentDetailStudentProgressScreen(uiState)
    }
}