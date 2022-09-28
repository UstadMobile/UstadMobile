package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ClazzAssignmentDetailOverviewFragmentEventHandler {

    fun onSubmitButtonClicked()

    fun onAddFileClicked()

    fun onAddTextClicked()

}

class ClazzAssignmentDetailOverviewFragment : UstadDetailFragment<ClazzAssignmentWithCourseBlock>(),
        ClazzAssignmentDetailOverviewView, ClazzAssignmentDetailOverviewFragmentEventHandler,
        OpenSheetListener, FileSubmissionListItemListener {


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

        detailMergerRecyclerAdapter = ConcatAdapter(detailRecyclerAdapter, submissionStatusHeaderAdapter,
                addSubmissionButtonsAdapter, addSubmissionAdapter, submitButtonAdapter,
                submissionHeaderAdapter, submittedSubmissionAdapter,
                classCommentsHeadingRecyclerAdapter,
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

    override var submissionMark: CourseAssignmentMark? = null
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