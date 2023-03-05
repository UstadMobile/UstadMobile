package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItem as UstadCourseAssignmentMarkListItemUiState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailOverviewBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzAssignmentDetailOverviewPresenter
import com.ustadmobile.core.controller.FileSubmissionListItemListener
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import androidx.compose.material.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.view.ClazzAssignmentDetailOverviewFragment.Companion.SUBMISSION_POLICY_MAP
import com.ustadmobile.port.android.view.composable.*
import java.util.*


interface ClazzAssignmentDetailOverviewFragmentEventHandler {

    fun onSubmitButtonClicked()

    fun onAddFileClicked()

    fun onAddTextClicked()

}

class ClazzAssignmentDetailOverviewFragment : UstadDetailFragment<ClazzAssignmentWithCourseBlock>(),
        ClazzAssignmentDetailOverviewView, ClazzAssignmentDetailOverviewFragmentEventHandler,
        OpenSheetListener, FileSubmissionListItemListener {


    private var gradesHeaderAdapter: GradesHeaderAdapter? = null
    private var marksAdapter: GradesListAdapter? = null
    private var submitButtonAdapter: SubmitButtonAdapter? = null
    private var dbRepo: UmAppDatabase? = null
    private var mBinding: FragmentClazzAssignmentDetailOverviewBinding? = null

    private var mPresenter: ClazzAssignmentDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    val accountManager: UstadAccountManager by instance()

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null
    private var detailRecyclerAdapter: ClazzAssignmentBasicDetailRecyclerAdapter? = null

    private var submissionStatusHeaderAdapter: SubmissionStatusHeaderAdapter? = null
    private var addSubmissionButtonsAdapter: AddSubmissionButtonsAdapter? = null

    private var classCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var classCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var classCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newClassCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var classCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null

    private var submissionHeaderAdapter: SimpleHeadingRecyclerAdapter? = null

    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null

    private var submittedSubmissionAdapter: SubmissionAdapter? = null
    private var addSubmissionAdapter: AddSubmissionListAdapter? = null

    private var submissionAttachmentLiveDataCourse: LiveData<PagedList<CourseAssignmentSubmissionWithAttachment>>? = null

    private val courseSubmissionWithAttachmentObserver = Observer<PagedList<CourseAssignmentSubmissionWithAttachment>?> {
        t -> run{
            submissionHeaderAdapter?.visible = t.isNotEmpty()
            submittedSubmissionAdapter?.submitList(t)
        }
    }


    private var courseMarkLiveData: LiveData<PagedList<CourseAssignmentMarkWithPersonMarker>>? = null

    private val courseMarkObserver = Observer<PagedList<CourseAssignmentMarkWithPersonMarker>?> {
            t -> run{
        gradesHeaderAdapter?.visible = t.isNotEmpty()
        marksAdapter?.submitList(t)
    }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentDetailOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        detailMergerRecyclerView =
                rootView.findViewById(R.id.fragment_clazz_assignment_detail_overview)

        // 1
        detailRecyclerAdapter = ClazzAssignmentBasicDetailRecyclerAdapter()
        detailRecyclerAdapter?.timeZone = "UTC"

        // 2
        submissionStatusHeaderAdapter = SubmissionStatusHeaderAdapter()

        // 3
        addSubmissionButtonsAdapter = AddSubmissionButtonsAdapter(this)

        // 4
        addSubmissionAdapter = AddSubmissionListAdapter(fileSubmissionEditListener).also {
            it.isSubmitted = false
        }

        // 5 submit button adapter
        submitButtonAdapter = SubmitButtonAdapter(this)

        // 6 file submission header
        submissionHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.submissions).toString()).apply {
            visible = false
        }

        // 7 file submissions
        submittedSubmissionAdapter = SubmissionAdapter(this).also {
            it.isSubmitted = true
        }

        gradesHeaderAdapter = GradesHeaderAdapter(onFilterOptionSelected = mPresenter)

        marksAdapter = GradesListAdapter()

        // 8 class
        classCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.class_comments).toString()
        ).apply {
            visible = false
        }

        // 9 new class comment
        newClassCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_class_comment),
                true).apply {
            visible = false
        }

        // 10 Class comments list
        classCommentsRecyclerAdapter = CommentsRecyclerAdapter().also {
            this.classCommentsObserver = PagedListSubmitObserver(it)
        }

        // 11 - Private
        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        ).apply {
            visible = false
        }

        // 12 - New Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false).apply{
            visible = false
        }

        // 13 - Private comments list
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }


        mPresenter = ClazzAssignmentDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this, viewLifecycleOwner, di)
        gradesHeaderAdapter?.listener = mPresenter

        detailMergerRecyclerAdapter = ConcatAdapter(detailRecyclerAdapter, submissionStatusHeaderAdapter,
                addSubmissionButtonsAdapter, addSubmissionAdapter, submitButtonAdapter,
                submissionHeaderAdapter, submittedSubmissionAdapter, gradesHeaderAdapter,
                marksAdapter, classCommentsHeadingRecyclerAdapter,
                newClassCommentRecyclerAdapter, classCommentsRecyclerAdapter, privateCommentsHeadingRecyclerAdapter,
                newPrivateCommentRecyclerAdapter, privateCommentsRecyclerAdapter)
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null


        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null

        privateCommentsLiveData = null
        classCommentsLiveData = null
        newPrivateCommentRecyclerAdapter = null
        classCommentsRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null
        newClassCommentRecyclerAdapter = null
        classCommentsHeadingRecyclerAdapter = null
        privateCommentsHeadingRecyclerAdapter = null

        addSubmissionButtonsAdapter = null
        submittedSubmissionAdapter = null
        submissionStatusHeaderAdapter = null

    }


    override var submittedCourseAssignmentSubmission: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>? = null
        set(value) {
            val dvRepoVal = dbRepo?: return
            submissionAttachmentLiveDataCourse?.removeObserver(courseSubmissionWithAttachmentObserver)
            submissionAttachmentLiveDataCourse = value?.asRepositoryLiveData(dvRepoVal.courseAssignmentSubmissionDao)
            field = value
            submissionAttachmentLiveDataCourse?.observeIfFragmentViewIsReady(this, courseSubmissionWithAttachmentObserver)
        }

    override var markList: DataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>? = null
        set(value) {
            val dvRepoVal = dbRepo?: return
            courseMarkLiveData?.removeObserver(courseMarkObserver)
            courseMarkLiveData = value?.asRepositoryLiveData(dvRepoVal.courseAssignmentMarkDao)
            field = value
            courseMarkLiveData?.observeIfFragmentViewIsReady(this, courseMarkObserver)
        }

    override var gradeFilterChips: List<ListFilterIdOption>? = null
        set(value) {
            field = value
            gradesHeaderAdapter?.filterOptions = value
        }

    override var addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment>? = null
        set(value) {
            field = value
            submitButtonAdapter?.hasFilesToSubmit = value?.isNotEmpty() ?: false
            addSubmissionAdapter?.submitList(value)
            addSubmissionAdapter?.notifyDataSetChanged()
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
            field = value
            detailRecyclerAdapter?.timeZone = value
        }

    override var clazzAssignmentClazzComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            val dvRepoVal = dbRepo?: return
            val publicCommentsObserverVal = this.classCommentsObserver
                    ?:return
            classCommentsLiveData?.removeObserver(publicCommentsObserverVal)
            classCommentsLiveData = value?.asRepositoryLiveData(dvRepoVal.commentsDao)
            classCommentsLiveData?.observeIfFragmentViewIsReady(this, publicCommentsObserverVal)
            field = value
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

    override var showPrivateComments: Boolean = false
        set(value){
            field = value
            newPrivateCommentRecyclerAdapter?.visible = showPrivateComments
            privateCommentsHeadingRecyclerAdapter?.visible = showPrivateComments
        }

    override var showSubmission: Boolean = false
        set(value){
            field = value
            submittedSubmissionAdapter?.visible = value
            addSubmissionButtonsAdapter?.visible = value
            submitButtonAdapter?.visible = value
            addSubmissionAdapter?.visible = value
            submissionStatusHeaderAdapter?.visible = value
        }

    override var addTextSubmissionVisible: Boolean = false
        set(value) {
            field = value
            addSubmissionButtonsAdapter?.addTextVisible = value
        }

    override var addFileSubmissionVisible: Boolean = false
        set(value) {
            field = value
            addSubmissionButtonsAdapter?.addFileVisible = value
        }

    override var submissionMark: AverageCourseAssignmentMark? = null
        set(value) {
            field = value
            submissionStatusHeaderAdapter?.courseAssignmentMark = value
        }

    override var submissionStatus: Int = 0
        set(value) {
            field = value
            submissionStatusHeaderAdapter?.assignmentStatus = value
        }

    override var unassignedError: String? = null
        set(value) {
            field = value
            submitButtonAdapter?.unassignedError = value
        }

    override var entity: ClazzAssignmentWithCourseBlock? = null
        get() = field
        set(value) {
            field = value
            detailRecyclerAdapter?.clazzAssignment = value
            submissionStatusHeaderAdapter?.assignment = value
            submittedSubmissionAdapter?.assignment = value
            addSubmissionButtonsAdapter?.assignment = value
            addSubmissionAdapter?.assignment = value
            marksAdapter?.courseblock = value?.block

            detailRecyclerAdapter?.visible = true

            newClassCommentRecyclerAdapter?.visible = value?.caClassCommentEnabled ?: false
            classCommentsHeadingRecyclerAdapter?.visible = value?.caClassCommentEnabled ?: false

        }

    override fun onSubmitButtonClicked() {
        mPresenter?.handleSubmitButtonClicked()
        submitButtonAdapter?.hasFilesToSubmit = false
    }

    override fun onAddFileClicked() {
        mPresenter?.handleAddFileClicked()
    }

    override fun onAddTextClicked() {
        mPresenter?.handleAddTextClicked()
    }

    override fun open(publicComment: Boolean) {
        val hintText =   if(publicComment)  requireContext().getString(R.string.add_class_comment)
                        else requireContext().getString(R.string.add_private_comment)
        val listener = if(publicComment) mPresenter?.newClassCommentListener else mPresenter?.newPrivateCommentListener
        val sendCommentSheet = CommentsBottomSheet(publicComment, hintText,
                accountManager.activeAccount.personUid, listener)
        sendCommentSheet.show(childFragmentManager, sendCommentSheet.tag)
    }

    var fileSubmissionEditListener = object: FileSubmissionListItemListener {

        override fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
            mPresenter?.handleDeleteSubmission(submissionCourse)
        }

        override fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
            mPresenter?.handleEditSubmission(submissionCourse)
        }

    }


    override fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        mPresenter?.handleDeleteSubmission(submissionCourse)
    }

    override fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment){
        mPresenter?.handleOpenSubmission(submissionCourse)
    }

    companion object {

        @JvmField
        val ASSIGNMENT_STATUS_MAP = mapOf(
                CourseAssignmentSubmission.NOT_SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.MARKED to R.drawable.ic_baseline_done_all_24
        )

        @JvmField
        val SUBMISSION_POLICY_MAP = mapOf(
            ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to R.drawable.ic_baseline_task_alt_24,
            ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to R.drawable.ic_baseline_add_task_24,
        )


    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzAssignmentDetailOverviewScreen(
    uiState: ClazzAssignmentDetailOverviewUiState,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickMark: (CourseAssignmentMarkWithPersonMarker?) -> Unit = {},
    onClickNewPublicComment: () -> Unit = {},
    onClickNewPrivateComment: () -> Unit = {},
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {},
    onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = { },
    onClickAddTextSubmission: () -> Unit = { },
    onClickAddFileSubmission: () -> Unit = { },
    onClickSubmitSubmission: () -> Unit = { }
){

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = uiState.clazzAssignment?.block?.cbDeadlineDate ?: 0,
        timeZoneId = TimeZone.getDefault().id
    )

    val caSubmissionPolicyText = messageIdResource(
        SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS[
                uiState.clazzAssignment?.caSubmissionPolicy ?:
                ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE].messageId)

    val caFileType = messageIdMapResource(
        map = SubmissionConstants.FILE_TYPE_MAP,
        key = uiState.clazzAssignment?.caFileType ?: ClazzAssignment.FILE_TYPE_DOC
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        if (uiState.caDescriptionVisible){
            item {
                Text (
                    text =uiState.clazzAssignment?.caDescription ?: "",
                    modifier = Modifier.defaultItemPadding()
                )
            }
        }

        if (uiState.cbDeadlineDateVisible){
            item {
                UstadDetailField(
                    valueText = "$formattedDateTime (${TimeZone.getDefault().id})",
                    labelText = stringResource(id = R.string.deadline),
                    imageId = R.drawable.ic_event_available_black_24dp,
                    onClick = {  }
                )
            }
        }

        item {
            UstadDetailField(
                valueText = caSubmissionPolicyText,
                labelText = stringResource(id = R.string.submission_policy),
                imageId = SUBMISSION_POLICY_MAP[uiState.clazzAssignment?.caSubmissionPolicy]
                    ?: R.drawable.ic_baseline_task_alt_24,
                onClick = {  }
            )
        }

        item {
            UstadAssignmentSubmissionHeader(
                uiState = uiState.submissionHeaderUiState,
            )
        }

        item {
            Text("")
        }

        items(
            items = uiState.draftSubmissionList,
            key = { Pair(1, it.casUid) }
        ){ submission ->
            UstadAssignmentSubmissionListItem(
                submission = submission,
                onClickOpenSubmission = onClickOpenSubmission,
                onClickDeleteSubmission = onClickDeleteSubmission,
            )
        }

        if (uiState.addTextVisible){
            item {
                OutlinedButton(
                    onClick = onClickAddTextSubmission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    enabled = uiState.fieldsEnabled,
                ) {
                    Text(stringResource(R.string.add_text).uppercase())
                }
            }
        }

        if (uiState.addFileVisible){
            item {
                OutlinedButton(
                    onClick = onClickAddFileSubmission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    enabled = uiState.fieldsEnabled,
                ) {
                    Text(stringResource(R.string.add_file).uppercase())
                }
            }
        }

        if (uiState.addFileVisible) {
            item {
                Row (
                    modifier = Modifier.defaultItemPadding()
                ){
                    Text(stringResource(R.string.file_type_chosen))
                    Text(caFileType)

                    Box(modifier = Modifier.width(5.dp))

                    Text(stringResource(R.string.max_number_of_files,
                        uiState.clazzAssignment?.caNumberOfFiles ?: 0))
                }
            }
        }

        if (uiState.submitSubmissionButtonVisible){
            item {
                Button(
                    onClick = onClickSubmitSubmission,
                    enabled = uiState.fieldsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.secondaryColor)
                    )
                ) {
                    Text(stringResource(R.string.submit).uppercase(),
                        color = contentColorFor(
                            colorResource(id = R.color.secondaryColor)
                        )
                    )
                }
            }
        }

        items(
            items = uiState.submittedSubmissionList,
            key = { Pair(2, it.casUid) }
        ){ submission ->
            UstadAssignmentSubmissionListItem(
                submission = submission,
                onClickOpenSubmission = onClickOpenSubmission,
            )
        }

        item {
            Text(stringResource(R.string.grades_class_age))
        }

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.gradeFilterChips,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = { onClickFilterChip(it) },
            )
        }

        items(
            items = uiState.markList,
            key = { Pair(3, it.camUid) }
        ){ mark ->
            UstadCourseAssignmentMarkListItem(
                onClickMark = onClickMark,
                uiState = UstadCourseAssignmentMarkListItemUiState(
                    mark = mark,
                    block = uiState.clazzAssignment?.block ?: CourseBlock()
                ),
            )
        }

        item {
            ListItem(
                text = {Text(stringResource(R.string.class_comments))}
            )
        }

        item {
            UstadAddCommentListItem(
                text = stringResource(id = R.string.add_class_comment),
                enabled = uiState.fieldsEnabled,
                personUid = 0,
                onClickAddComment = { onClickNewPublicComment() }
            )
        }

        items(
            items = uiState.publicCommentList,
            key = { Pair(4, it.commentsUid) }
        ){ comment ->

            UstadCommentListItem(commentWithPerson = comment)
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
            items = uiState.privateCommentList,
            key = { Pair(5, it.commentsUid) }
        ){ comment ->
            UstadCommentListItem(commentWithPerson = comment)
        }
    }
}

@Composable
@Preview
fun ClazzAssignmentDetailOverviewScreenPreview(){
    ClazzAssignmentDetailOverviewScreen(
        uiState = ClazzAssignmentDetailOverviewUiState(
            addFileVisible = true,
            addTextVisible = true,
            clazzAssignment = ClazzAssignmentWithCourseBlock().apply {
                caDescription = "Read the stories and describe the main characters."
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
                caFileType =  ClazzAssignment.FILE_TYPE_DOC
                block = CourseBlock().apply {
                    cbDeadlineDate = 1677063785
                }
            },
            markList = listOf(
                CourseAssignmentMarkWithPersonMarker().apply {
                    marker = Person().apply {
                        firstNames = "John"
                        lastName = "Smith"
                        isGroup = true
                        camMarkerSubmitterUid = 2
                        camMarkerComment = "Comment"
                    }
                }
            ),
            publicCommentList = listOf(
                CommentsWithPerson().apply {
                    commentsUid = 1
                    commentsPerson = Person().apply {
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                }
            ),
            privateCommentList = listOf(
                CommentsWithPerson().apply {
                    commentsUid = 1
                    commentsPerson = Person().apply {
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                }
            ),
            submissionHeaderUiState = UstadAssignmentSubmissionHeaderUiState(
                assignmentStatus = CourseAssignmentSubmission.MARKED,
                assignmentMark = AverageCourseAssignmentMark().apply {
                    averagePenalty = 12
                }
            ),
            submittedSubmissionList = listOf(
                CourseAssignmentSubmissionWithAttachment().apply {
                    casUid = 1
                    casTimestamp = 1677744388299
                    casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                    attachment = CourseAssignmentSubmissionAttachment().apply {
                        casaFileName = "Content Title"
                    }
                },
            ),
            draftSubmissionList = listOf(
                CourseAssignmentSubmissionWithAttachment().apply {
                    casUid = 1
                    casTimestamp = 1677744388299
                    casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                    attachment = CourseAssignmentSubmissionAttachment().apply {
                        casaFileName = "Content Title"
                    }
                },
            )
        )
    )
}