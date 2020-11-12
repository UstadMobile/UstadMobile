package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkSubmissionMarkingBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzWorkSubmissionMarkingPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


class ClazzWorkSubmissionMarkingFragment: UstadEditFragment<ClazzMemberAndClazzWorkWithSubmission>(),
        ClazzWorkSubmissionMarkingView, NewCommentHandler, SimpleButtonHandler,
        SimpleTwoButtonHandler{

    internal var mBinding: FragmentClazzWorkSubmissionMarkingBinding? = null

    private var mPresenter: ClazzWorkSubmissionMarkingPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzMemberAndClazzWorkWithSubmission>?
        get() = mPresenter

    private lateinit var dbRepo : UmAppDatabase

    override val viewContext: Any
        get() = requireContext()

    private var submissionHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null

    private var markingEditRecyclerAdapter
            : ClazzWorkSubmissionScoreEditRecyclerAdapter? = null

    private var shortTextSubmissionRecyclerAdapter:
            ClazzWorkShortTextSubmissionRecyclerAdapter? = null

    private var shortTextResultRecyclerAdapter:
            ClazzWorkShortTextResultRecyclerAdapter? = null

    private var markingHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var questionsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var quizViewRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseViewRecyclerAdapter? = null
    private val quizQuestionAndResponseObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizViewRecyclerAdapter?.submitList(t)
    }
    private var quizEditRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter? = null

    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsMergerRecyclerAdapter: MergeAdapter? = null
    private var submitWithMetricsRecyclerAdapter: ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter ? = null
    private var recordForStudentButtonRecyclerAdapter: SimpleButtonRecyclerAdapter? = null
    private var simpleTwoButtonRecyclerAdapter: SimpleTwoButtonRecyclerAdapter? = null

    private var detailMergerRecyclerAdapter: MergeAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkSubmissionMarkingBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)


        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_submission_marking_rv)

        mPresenter = ClazzWorkSubmissionMarkingPresenter(requireContext(), arguments.toStringMap(),
                this, di, this)

        val clazzWorkWithSubmission: ClazzWorkWithSubmission =
                ClazzWorkWithSubmission().generateWithClazzWorkAndClazzWorkSubmission(
                        entity?.clazzWork?: ClazzWork(), entity?.submission
                )

        submitWithMetricsRecyclerAdapter =
                ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter(
                        clazzWorkMetrics, mPresenter, isMarkingFinished)


        quizEditRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter()

        recordForStudentButtonRecyclerAdapter =
                SimpleButtonRecyclerAdapter(getText(R.string.record_for_student).toString(),
                        this)
        recordForStudentButtonRecyclerAdapter?.isOutline = true
        recordForStudentButtonRecyclerAdapter?.visible = true

        simpleTwoButtonRecyclerAdapter = SimpleTwoButtonRecyclerAdapter(
                getText(R.string.submit).toString(),getText(R.string.cancel).toString(),
                this)
        simpleTwoButtonRecyclerAdapter?.visible = false


        markingEditRecyclerAdapter = ClazzWorkSubmissionScoreEditRecyclerAdapter(entity)

        shortTextSubmissionRecyclerAdapter = ClazzWorkShortTextSubmissionRecyclerAdapter(entity)
        shortTextSubmissionRecyclerAdapter?.visible = false

        shortTextResultRecyclerAdapter = ClazzWorkShortTextResultRecyclerAdapter(entity)
        shortTextResultRecyclerAdapter?.visible = false

        submissionHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.submission).toString())
        submissionHeadingRecyclerAdapter?.visible = false

        questionsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.questions).toString())
        questionsHeadingRecyclerAdapter?.visible = true

        markingHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.marking).toString())

        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        )
        privateCommentsHeadingRecyclerAdapter?.visible = true


        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWork?.clazzWorkUid?:0L, entity?.clazzMemberPersonUid?:0L
        )
        newPrivateCommentRecyclerAdapter?.visible = true

        quizViewRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseViewRecyclerAdapter()

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        privateCommentsMergerRecyclerAdapter = MergeAdapter(
                privateCommentsHeadingRecyclerAdapter, privateCommentsRecyclerAdapter,
                newPrivateCommentRecyclerAdapter)

        detailMergerRecyclerAdapter = MergeAdapter(
                submissionHeadingRecyclerAdapter, shortTextSubmissionRecyclerAdapter,
                shortTextResultRecyclerAdapter,
                quizViewRecyclerAdapter, quizEditRecyclerAdapter,
                recordForStudentButtonRecyclerAdapter, simpleTwoButtonRecyclerAdapter,
                markingHeadingRecyclerAdapter, markingEditRecyclerAdapter,
                privateCommentsMergerRecyclerAdapter, submitWithMetricsRecyclerAdapter
        )
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        submissionHeadingRecyclerAdapter = null
        markingEditRecyclerAdapter = null
        shortTextSubmissionRecyclerAdapter = null
        shortTextResultRecyclerAdapter = null
        markingHeadingRecyclerAdapter = null
        questionsHeadingRecyclerAdapter = null
        quizViewRecyclerAdapter = null
        quizEditRecyclerAdapter = null
        privateCommentsHeadingRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null
        newPrivateCommentRecyclerAdapter = null
        privateCommentsMergerRecyclerAdapter = null
        submitWithMetricsRecyclerAdapter = null
        recordForStudentButtonRecyclerAdapter = null
        simpleTwoButtonRecyclerAdapter = null
        detailMergerRecyclerAdapter = null
        detailMergerRecyclerView = null

    }

    override var showRecordForStudent: Boolean = false
        set(value) {
            field = value
            recordForStudentButtonRecyclerAdapter?.visible = value
        }

    override var showSubmissionHeading: Boolean = false
        set(value) {
            field = value
            submissionHeadingRecyclerAdapter?.visible = value
        }

    override var showSimpleTwoButton: Boolean = false
        set(value) {
            field = value
            simpleTwoButtonRecyclerAdapter?.visible = value
        }


    override var setQuizEditList: Boolean = false
        set(value) {
            field = value
            if(value){
                quizEditRecyclerAdapter?.submitList(editableQuizQuestions?.value)
            }else{
                quizEditRecyclerAdapter?.submitList(listOf())
            }
        }

    override var showShortTextResult: Boolean = false
        set(value) {
            field = value
            shortTextResultRecyclerAdapter?.visible = value
        }

    override var showShortTextSubmission: Boolean = false
        set(value) {
            field = value
            shortTextSubmissionRecyclerAdapter?.visible = value
        }

    override var entity: ClazzMemberAndClazzWorkWithSubmission? = null
        set(value) {
            field = value

            newPrivateCommentRecyclerAdapter?.entityUid = value?.clazzWork?.clazzWorkUid?:0L
            newPrivateCommentRecyclerAdapter?.commentTo = value?.clazzMemberPersonUid?:0L
            newPrivateCommentRecyclerAdapter?.commentFrom = 0L
            newPrivateCommentRecyclerAdapter?.visible = true

            ustadFragmentTitle = value?.person?.fullName()?:""

            //Don't show the button if submission exists or submission is not required.
            markingEditRecyclerAdapter?.clazzWorkVal = value
            markingEditRecyclerAdapter?.visible = true

            shortTextSubmissionRecyclerAdapter?.clazzWorkWithSubmission = value
            shortTextResultRecyclerAdapter?.clazzWorkWithSubmission = value


            //If already submitted.
            if(entity?.submission != null && entity?.submission?.clazzWorkSubmissionUid != 0L){
                markingHeadingRecyclerAdapter?.visible = true
                markingEditRecyclerAdapter?.visible = true
                markingEditRecyclerAdapter?.clazzWorkVal = entity
                markingEditRecyclerAdapter?.notifyDataSetChanged()
                shortTextResultRecyclerAdapter?.visible = true
            }

        }

    override var privateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observeIfFragmentViewIsReady(this, privateCommentsObserverVal)
        }


    override var viewOnlyQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        set(value) {
            field?.removeObserver(quizQuestionAndResponseObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, quizQuestionAndResponseObserver)
        }


    override var editableQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null

    override var isMarkingFinished: Boolean = false

    override var clazzWorkMetrics: ClazzWorkWithMetrics? = null
        set(value) {

            field = value
            submitWithMetricsRecyclerAdapter?.showNext = isMarkingFinished
            submitWithMetricsRecyclerAdapter?.submitList(listOf(clazzWorkMetrics))

        }

    override var fieldsEnabled: Boolean = true

    override fun addNewComment2(view: View, entityType: Int, entityUid: Long, comment: String,
                                public: Boolean, to: Long, from: Long) {
        (view.parent as View).findViewById<EditText>(R.id.item_comment_new_comment_et).setText("")
        mPresenter?.addComment(entityType, entityUid, comment, public, to, from)
    }

    //On click "Record for student" button
    override fun onClickButton(view: View) {
        mPresenter?.handleClickRecordForStudent()
        //Scroll to top:
        detailMergerRecyclerView?.smoothScrollToPosition(0);
    }


    //On click 'Submit' on Recording on behalf
    override fun onClickPrimary(view: View) {
        simpleTwoButtonRecyclerAdapter?.visible = false


        markingHeadingRecyclerAdapter?.visible = true
        markingEditRecyclerAdapter?.visible = true

        quizEditRecyclerAdapter?.submitList(listOf())

        mPresenter?.handleClickSubmitOnBehalf()
        recordForStudentButtonRecyclerAdapter?.visible = false
        quizViewRecyclerAdapter?.submitList(editableQuizQuestions?.value)

        shortTextSubmissionRecyclerAdapter?.showSubmissionEdit = false
        shortTextSubmissionRecyclerAdapter?.notifyDataSetChanged()
        shortTextSubmissionRecyclerAdapter?.visible = false
        submissionHeadingRecyclerAdapter?.visible = true

        shortTextResultRecyclerAdapter?.visible = true
    }

    //On click cancel for student recording on their behalf
    override fun onClickSecondary(view: View) {
        quizEditRecyclerAdapter?.submitList(listOf())
        simpleTwoButtonRecyclerAdapter?.visible = false
        recordForStudentButtonRecyclerAdapter?.visible = true
        shortTextSubmissionRecyclerAdapter?.showSubmissionEdit = false

        shortTextSubmissionRecyclerAdapter?.visible = false
    }
}