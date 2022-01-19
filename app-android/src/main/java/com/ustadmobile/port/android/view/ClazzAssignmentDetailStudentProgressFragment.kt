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
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
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

class ClazzAssignmentDetailStudentProgressFragment(): UstadDetailFragment<ClazzAssignment>(),
        ClazzAssignmentDetailStudentProgressView, ClazzAssignmentDetailStudentProgressFragmentEventHandler,
        OpenSheetListener, FileSubmissionListItemListener {

    private var dbRepo: UmAppDatabase? = null

    val accountManager: UstadAccountManager by instance()
    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var mBinding: FragmentClazzAssignmentDetailOverviewBinding? = null


    private var contentHeaderAdapter: SimpleHeadingRecyclerAdapter? = null
    private var fileSubmissionHeaderAdapter: SimpleHeadingRecyclerAdapter? = null
    private var contentRecyclerAdapter: ContentWithAttemptRecyclerAdapter? = null

    private var contentLiveData: LiveData<PagedList<
            ContentWithAttemptSummary>>? = null
    private val contentObserver = Observer<PagedList<
            ContentWithAttemptSummary>?> { t ->
        run {
            contentHeaderAdapter?.visible = t?.size ?: 0 > 0
            contentRecyclerAdapter?.submitList(t)
        }
    }


    private var fileSubmissionAdapter: FileSubmissionAdapter? = null

    private var fileSubmissionLiveData: LiveData<PagedList<AssignmentFileSubmission>>? = null
    private val fileSubmissionObserver = Observer<PagedList<AssignmentFileSubmission>?> {
        t -> run {
        fileSubmissionAdapter?.submitList(t)
    }
    }


    private var markSubmissionAdapter: MarkFileSubmissionAdapter? = null


    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null




    private var scoreRecyclerAdapter: ScoreRecyclerAdapter? = null

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
        contentHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.content).toString()).apply {
            visible = false
        }

        // 2
        contentRecyclerAdapter = ContentWithAttemptRecyclerAdapter(mPresenter)


        fileSubmissionHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.file_submission).toString()).apply {
            visible = false
        }

        // 3 file submission for student
        fileSubmissionAdapter = FileSubmissionAdapter(this).also{
            it.showDownload = true
        }

        // 4 mark grade
        markSubmissionAdapter = MarkFileSubmissionAdapter()

        // 5
        scoreRecyclerAdapter = ScoreRecyclerAdapter()


        // 4 - Private
        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        ).apply {
            visible = false
        }

        // 5 - New Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false).apply{
            visible = false
        }

        //6 - Private comments list
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        detailMergerRecyclerAdapter = ConcatAdapter(contentHeaderAdapter,
                contentRecyclerAdapter, fileSubmissionHeaderAdapter, fileSubmissionAdapter,
                markSubmissionAdapter, scoreRecyclerAdapter, privateCommentsHeadingRecyclerAdapter,
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
        contentHeaderAdapter = null
        contentRecyclerAdapter = null
        contentLiveData = null
        scoreRecyclerAdapter = null

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


    override var clazzAssignmentContent: DataSource.Factory<Int, ContentWithAttemptSummary>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            contentLiveData?.removeObserver(contentObserver)
            contentLiveData = value?.asRepositoryLiveData(dbRepoVal.clazzAssignmentDao)
            field = value
            contentLiveData?.observeIfFragmentViewIsReady(this, contentObserver)
        }


    override var clazzAssignmentFileSubmission: DoorDataSourceFactory<Int, AssignmentFileSubmission>? = null
        set(value) {
            val dbRepoVal = dbRepo?: return
            fileSubmissionLiveData?.removeObserver(fileSubmissionObserver)
            fileSubmissionLiveData = value?.asRepositoryLiveData(dbRepoVal.assignmentFileSubmissionDao)
            field = value
            fileSubmissionLiveData?.observeIfFragmentViewIsReady(this, fileSubmissionObserver)
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

    override var hasFileSubmission: Boolean = false
        set(value) {
            field = value
            fileSubmissionAdapter?.visible = value
            markSubmissionAdapter?.visible = value
            fileSubmissionHeaderAdapter?.visible = value
        }

    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.personFullName()
        }

    override var studentScore: ContentEntryStatementScoreProgress? = null
        get() = field
        set(value) {
            field = value
            scoreRecyclerAdapter?.score = value
            scoreRecyclerAdapter?.visible = value?.resultMax?: 0 > 0
        }


    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
            newPrivateCommentRecyclerAdapter?.visible = value?.caPrivateCommentsEnabled ?: false
            privateCommentsHeadingRecyclerAdapter?.visible = value?.caPrivateCommentsEnabled ?: false
            markSubmissionAdapter?.assignment = value
            fileSubmissionAdapter?.assignment = value
        }

    override fun onSubmitGradeClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        mPresenter?.onClickSubmitGrade(grade)
    }

    override fun onSubmitGradeAndMarkNextClicked() {
        val grade = markSubmissionAdapter?.grade ?: return
        mPresenter?.onClickSubmitGradeAndMarkNext(grade)
    }

    override fun onClickDeleteFileSubmission(fileSubmission: AssignmentFileSubmission) {
        // cant delete here
    }

    override fun onClickDownloadFileSubmission(fileSubmission: AssignmentFileSubmission) {
        mPresenter?.onDownloadFileClicked()
    }

}