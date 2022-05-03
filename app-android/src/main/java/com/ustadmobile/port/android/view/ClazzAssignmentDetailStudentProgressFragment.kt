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
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressPresenter
import com.ustadmobile.core.controller.FileSubmissionListItemListener
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
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

    private var dbRepo: UmAppDatabase? = null

    val accountManager: UstadAccountManager by instance()
    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var mBinding: FragmentClazzAssignmentDetailOverviewBinding? = null

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
        mBinding = FragmentClazzAssignmentDetailOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }


        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)

        mPresenter = ClazzAssignmentDetailStudentProgressPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        detailMergerRecyclerView =
                rootView.findViewById(R.id.fragment_clazz_assignment_detail_overview)

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

        detailMergerRecyclerAdapter = ConcatAdapter(submissionsHeaderAdapter,
                submissionStatusHeaderAdapter, submissionAdapter,
                markSubmissionAdapter, privateCommentsHeadingRecyclerAdapter,
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
        mPresenter = null
        mBinding = null
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
        val sendCommentSheet = CommentsBottomSheet(publicComment, requireContext().getString(R.string.add_private_comment),
                accountManager.activeAccount.personUid,  mPresenter?.newPrivateCommentListener)
        sendCommentSheet.show(childFragmentManager, sendCommentSheet.tag)
    }


    override var clazzCourseAssignmentSubmissionAttachment: DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            submissionAttachmentLiveDataCourse?.removeObserver(fileSubmissionObserver)
            submissionAttachmentLiveDataCourse = value?.asRepositoryLiveData(dbRepoVal.courseAssignmentSubmissionDao)
            field = value
            submissionAttachmentLiveDataCourse?.observeIfFragmentViewIsReady(this, fileSubmissionObserver)
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

    override var submissionScore: CourseAssignmentMark? = null
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
        }

    override fun onSubmitGradeClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        mPresenter?.onClickSubmitGrade(grade)
    }

    override fun onSubmitGradeAndMarkNextClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        mPresenter?.onClickSubmitGradeAndMarkNext(grade)
    }

    override fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        // cant delete here
    }

    override fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment) {
        mPresenter?.onClickOpenSubmission(submissionCourse)
    }

}