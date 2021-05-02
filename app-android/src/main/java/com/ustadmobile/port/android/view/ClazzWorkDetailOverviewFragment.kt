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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkWithSubmissionDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzWorkDetailOverviewPresenter
import com.ustadmobile.core.controller.DefaultContentEntryListItemListener
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

interface NewCommentHandler{
    fun addNewComment2(view: View, entityType: Int, entityUid: Long, comment: String,
                       public: Boolean, to: Long, from: Long)
}

interface SimpleButtonHandler{
    fun onClickButton(view: View)
}

interface SimpleTwoButtonHandler{
    fun onClickPrimary(view:View)
    fun onClickSecondary(view:View)
}

class ClazzWorkDetailOverviewFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkDetailOverviewView, NewCommentHandler, SimpleButtonHandler{

    internal var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkDetailOverviewPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    val accountManager: UstadAccountManager by instance()

    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentLiveData: LiveData<PagedList<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
    private val contentObserver = Observer<PagedList<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?> {
        t ->
        run {
            if (t?.size ?: 0 > 0) {
                contentHeadingRecyclerAdapter?.visible = true
            }else{
                contentHeadingRecyclerAdapter?.visible = false
            }
            contentRecyclerAdapter?.submitList(t)
        }
    }


    private var quizSubmissionEditRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter? = null
    private val quizQuestionAndResponseEditObserver = Observer<List<
            ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizSubmissionEditRecyclerAdapter?.submitList(t)
    }

    private var quizSubmissionViewRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseViewRecyclerAdapter? = null
    private val quizQuestionAndResponseViewObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizSubmissionViewRecyclerAdapter?.submitList(t)
    }

    private var contentHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var submissionMarkingResultRecyclerAdapter: SubmissionResultRecyclerAdapter? = null
    private var submissionFreeTextRecyclerAdapter: SubmissionTextEntryWithResultRecyclerAdapter ? = null

    private var submissionHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var questionsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var submissionButtonRecyclerAdapter: SimpleButtonRecyclerAdapter? = null
    private var classCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null

    private var classCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var publicCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var publicCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPublicCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var classCommentsMergerRecyclerAdapter: ConcatAdapter? = null

    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsMergerRecyclerAdapter: ConcatAdapter? = null

    private var detailRecyclerAdapter: ClazzWorkBasicDetailsRecyclerAdapter? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null

    override fun addNewComment2(view: View, entityType: Int, entityUid: Long, comment: String,
                                public: Boolean, to: Long, from: Long) {
        (view.parent as View).findViewById<EditText>(R.id.item_comment_new_comment_et).setText("")
        mPresenter?.addComment(entityType, entityUid, comment, public, to, from)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        fabManagementEnabled = true

        mBinding = FragmentClazzWorkWithSubmissionDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)

        //0 - Overall recycler view on the view xml
        detailMergerRecyclerView =
                rootView.findViewById(R.id.fragment_clazz_work_with_submission_detail_rv)

        //1 - Clazz Work detail
        detailRecyclerAdapter = ClazzWorkBasicDetailsRecyclerAdapter()

        //2 - Content heading
        contentHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.content).toString()
        )
        contentHeadingRecyclerAdapter?.visible = false

        //3 - Content list
        contentRecyclerAdapter = ContentEntryListRecyclerAdapter(
                DefaultContentEntryListItemListener(context = requireContext(), di = di),
                ListViewMode.BROWSER.toString(), viewLifecycleOwner, di)

        //4 - Question heading
        questionsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.questions).toString())
        questionsHeadingRecyclerAdapter?.visible = false

        //5 - Question edit view for students that have not marked
        quizSubmissionEditRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter()

        //6 - Submission button for students who see the question edit list
        submissionButtonRecyclerAdapter = SimpleButtonRecyclerAdapter(
                getText(R.string.submitliteral).toString(), this)
        submissionButtonRecyclerAdapter?.visible = false

        //7 - Submission heading for students post submission.
        submissionHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.submission).toString())
        submissionHeadingRecyclerAdapter?.visible = false

        //8 - Questions list for teachers/others and students that have submitted answers
        quizSubmissionViewRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseViewRecyclerAdapter()

        //9 - Submission Free text result (Short text type)
        submissionFreeTextRecyclerAdapter = SubmissionTextEntryWithResultRecyclerAdapter()
        submissionFreeTextRecyclerAdapter?.visible = false

        //10 - Submission Result with marking
        submissionMarkingResultRecyclerAdapter = SubmissionResultRecyclerAdapter(entity)
        submissionMarkingResultRecyclerAdapter?.visible = false

        //11 - Class comments heading
        classCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.class_comments).toString()
        )
        classCommentsHeadingRecyclerAdapter?.visible = true

        //12 - Class comments list
        classCommentsRecyclerAdapter = CommentsRecyclerAdapter().also {
            publicCommentsObserver = PagedListSubmitObserver(it)
        }

        //13 - New class comment component
        newPublicCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_class_comment), true, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWorkUid?:0L, 0,
                accountManager.activeAccount.personUid)
        newPublicCommentRecyclerAdapter?.visible = true

        //14 - Merger for the comments and new class comment component
        classCommentsMergerRecyclerAdapter = ConcatAdapter(classCommentsRecyclerAdapter,
                newPublicCommentRecyclerAdapter)

        //15 - Private comments heading
        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        )
        privateCommentsHeadingRecyclerAdapter?.visible = false

        //16 - Private comments list
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }

        //17 - New Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWorkUid?:0L, 0,
                accountManager.activeAccount.personUid)
        newPrivateCommentRecyclerAdapter?.visible = false

        //18 - Merger for the private comments and new private comment component
        privateCommentsMergerRecyclerAdapter = ConcatAdapter(newPrivateCommentRecyclerAdapter,
                privateCommentsRecyclerAdapter)


        mPresenter = ClazzWorkDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, this)

        detailMergerRecyclerAdapter = ConcatAdapter(
                detailRecyclerAdapter, contentHeadingRecyclerAdapter,
                contentRecyclerAdapter, submissionHeadingRecyclerAdapter,
                submissionMarkingResultRecyclerAdapter, submissionFreeTextRecyclerAdapter,
                questionsHeadingRecyclerAdapter, quizSubmissionViewRecyclerAdapter,
                quizSubmissionEditRecyclerAdapter, submissionButtonRecyclerAdapter,
                classCommentsHeadingRecyclerAdapter, classCommentsMergerRecyclerAdapter,
                privateCommentsHeadingRecyclerAdapter, privateCommentsMergerRecyclerAdapter
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


    //On Click Submit
    override fun onClickButton(view: View) {
        mPresenter?.handleClickSubmit()
        submissionFreeTextRecyclerAdapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        contentLiveData = null
        detailMergerRecyclerView?.adapter = null

        privateCommentsLiveData = null
        publicCommentsLiveData = null
        newPrivateCommentRecyclerAdapter = null
        classCommentsRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null
        newPublicCommentRecyclerAdapter = null
        detailRecyclerAdapter = null
        contentHeadingRecyclerAdapter = null
        contentRecyclerAdapter = null
        submissionHeadingRecyclerAdapter = null
        submissionMarkingResultRecyclerAdapter = null
        submissionFreeTextRecyclerAdapter = null
        questionsHeadingRecyclerAdapter = null
        quizSubmissionEditRecyclerAdapter = null
        submissionButtonRecyclerAdapter = null
        classCommentsHeadingRecyclerAdapter = null
        classCommentsMergerRecyclerAdapter = null
        privateCommentsHeadingRecyclerAdapter = null
        privateCommentsMergerRecyclerAdapter = null
    }

    override var isStudent: Boolean = false
        set(value) {
            if(field == value){
                return
            }
            field = value
            submissionFreeTextRecyclerAdapter?.visible = value
            when {
                entity?.clazzWorkCommentsEnabled == false -> {
                    privateCommentsHeadingRecyclerAdapter?.visible = isStudent
                    newPrivateCommentRecyclerAdapter?.visible = false
                }
                isStudent -> {
                    privateCommentsHeadingRecyclerAdapter?.visible = true
                    newPrivateCommentRecyclerAdapter?.visible = true
                }
                entity?.clazzWorkSubmissionType ==
                        ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ -> {
                    questionsHeadingRecyclerAdapter?.visible = true
                    newPrivateCommentRecyclerAdapter?.visible = false
                }
                else -> {
                    newPrivateCommentRecyclerAdapter?.visible = false
                }
            }
        }

    override var showMarking: Boolean = false
        set(value) {
            field = value
            submissionMarkingResultRecyclerAdapter?.visible = value
        }

    override var showFreeTextSubmission: Boolean = false
        set(value) {
            field = value
            submissionFreeTextRecyclerAdapter?.visible = value
        }

    override var showSubmissionButton: Boolean = false
        set(value) {
            field = value
            submissionButtonRecyclerAdapter?.visible = value
        }

    override var showQuestionHeading: Boolean = false
        set(value) {
            field = value
            questionsHeadingRecyclerAdapter?.visible = value
        }

    override var showSubmissionHeading: Boolean = false
        set(value) {
            field = value
            submissionHeadingRecyclerAdapter?.visible = value
        }

    override var showPrivateComments: Boolean = false
        set(value) {
            field = value
            privateCommentsHeadingRecyclerAdapter?.visible = value
        }

    override var showNewPrivateComment: Boolean = false
        set(value) {
            field = value
            newPrivateCommentRecyclerAdapter?.visible = value
        }


    override var entity: ClazzWorkWithSubmission? = null
        set(value) {
            field = value
            detailRecyclerAdapter?.clazzWork = entity
            detailRecyclerAdapter?.visible = true

            submissionMarkingResultRecyclerAdapter?._clazzWork = entity

            if(submissionFreeTextRecyclerAdapter?.visible == true){
                submissionFreeTextRecyclerAdapter?.submitList(listOf(entity))
            }else{
                submissionFreeTextRecyclerAdapter?.submitList(listOf())
            }

            newPublicCommentRecyclerAdapter?.entityUid = entity?.clazzWorkUid?:0L
            newPublicCommentRecyclerAdapter?.entityUid = entity?.clazzWorkUid?:0L

        }

    override var clazzWorkContent: DataSource.Factory<Int,
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null
        set(value) {
            contentLiveData?.removeObserver(contentObserver)
            contentLiveData = value?.asRepositoryLiveData(ClazzWorkDao)
            field = value
            contentLiveData?.observeIfFragmentViewIsReady(this, contentObserver)
        }


    override var editableQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        set(value) {
            field?.removeObserver(quizQuestionAndResponseEditObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, quizQuestionAndResponseEditObserver)
        }

    override var viewOnlyQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        set(value) {
            field?.removeObserver(quizQuestionAndResponseViewObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, quizQuestionAndResponseViewObserver)
        }

    override var timeZone: String = ""

    override var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val publicCommentsObserverVal = publicCommentsObserver?:return
            publicCommentsLiveData?.removeObserver(publicCommentsObserverVal)
            publicCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            publicCommentsLiveData?.observeIfFragmentViewIsReady(this, publicCommentsObserverVal)

        }

    override var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observeIfFragmentViewIsReady(this, privateCommentsObserverVal)
        }


    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    companion object {
        val DIFF_CALLBACK_COMMENTS =
                object : DiffUtil.ItemCallback<CommentsWithPerson>() {
            override fun areItemsTheSame(oldItem: CommentsWithPerson,
                                         newItem: CommentsWithPerson): Boolean {
                return oldItem.commentsUid == newItem.commentsUid
            }

            override fun areContentsTheSame(oldItem: CommentsWithPerson,
                                            newItem: CommentsWithPerson): Boolean {
                return oldItem.commentsPersonUid == newItem.commentsPersonUid &&
                        oldItem.commentsText == newItem.commentsText &&
                        oldItem.commentsDateTimeUpdated == newItem.commentsDateTimeUpdated
            }
        }

        val DU_CLAZZWORKWITHSUBMISSION =
                object: DiffUtil.ItemCallback<ClazzWorkWithSubmission>() {
            override fun areItemsTheSame(oldItem: ClazzWorkWithSubmission,
                                         newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkWithSubmission,
                                            newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
                        && oldItem.clazzWorkInstructions == newItem.clazzWorkInstructions
                        && oldItem.clazzWorkCommentsEnabled == newItem.clazzWorkCommentsEnabled
                        && oldItem.clazzWorkSubmissionType == newItem.clazzWorkSubmissionType
                        && oldItem.clazzWorkCreatedDate == newItem.clazzWorkCreatedDate
                        && oldItem.clazzWorkDueDateTime == newItem.clazzWorkDueDateTime
                        && oldItem.clazzWorkSubmission?.clazzWorkSubmissionInactive ==
                        newItem.clazzWorkSubmission?.clazzWorkSubmissionInactive
                        && oldItem.clazzWorkSubmission?.clazzWorkSubmissionUid ==
                        newItem.clazzWorkSubmission?.clazzWorkSubmissionUid
            }
        }

        val DU_CLAZZMEMBERANDCLAZZWORKWITHSUBMISSION =
                object: DiffUtil.ItemCallback<PersonWithClazzWorkAndSubmission>() {
                    override fun areItemsTheSame(oldItem: PersonWithClazzWorkAndSubmission,
                                                 newItem: PersonWithClazzWorkAndSubmission): Boolean {
                        return oldItem.clazzWork?.clazzWorkUid == newItem.clazzWork?.clazzWorkUid
                    }

                    override fun areContentsTheSame(oldItem: PersonWithClazzWorkAndSubmission,
                                                    newItem: PersonWithClazzWorkAndSubmission): Boolean {
                        return oldItem.clazzWork?.clazzWorkUid == newItem.clazzWork?.clazzWorkUid
                                && oldItem.clazzWork?.clazzWorkInstructions == newItem.clazzWork?.clazzWorkInstructions
                                && oldItem.clazzWork?.clazzWorkCommentsEnabled == newItem.clazzWork?.clazzWorkCommentsEnabled
                                && oldItem.clazzWork?.clazzWorkSubmissionType == newItem.clazzWork?.clazzWorkSubmissionType
                                && oldItem.clazzWork?.clazzWorkCreatedDate == newItem.clazzWork?.clazzWorkCreatedDate
                                && oldItem.clazzWork?.clazzWorkDueDateTime == newItem.clazzWork?.clazzWorkDueDateTime
                                && oldItem.submission?.clazzWorkSubmissionInactive ==
                                newItem.submission?.clazzWorkSubmissionInactive
                                && oldItem.submission?.clazzWorkSubmissionScore ==
                                newItem.submission?.clazzWorkSubmissionScore
                                && oldItem.submission?.clazzWorkSubmissionUid ==
                                newItem.submission?.clazzWorkSubmissionUid
                    }
                }

        val DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE_EDIT =
                object: DiffUtil.ItemCallback<ClazzWorkQuestionAndOptionWithResponse>() {
                    override fun areItemsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse, newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                        return oldItem.clazzWorkQuestion.clazzWorkQuestionUid ==
                                newItem.clazzWorkQuestion.clazzWorkQuestionUid
                    }

                    override fun areContentsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse, newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                        return oldItem === newItem
                    }
                }

        val DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE =
                object: DiffUtil.ItemCallback<ClazzWorkQuestionAndOptionWithResponse>() {
            override fun areItemsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse,
                                         newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                return oldItem.clazzWorkQuestion.clazzWorkQuestionUid ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse,
                                            newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {

                return oldItem.clazzWork.clazzWorkUid == newItem.clazzWork.clazzWorkUid &&
                        oldItem.clazzWorkQuestion.clazzWorkQuestionUid ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionUid
                        && oldItem.clazzWorkQuestion.clazzWorkQuestionText ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionText
                        && oldItem.clazzWorkQuestion.clazzWorkQuestionType ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionType
                        && oldItem.clazzWorkQuestion.clazzWorkQuestionIndex ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionIndex
                        && oldItem.clazzWorkQuestion.clazzWorkQuestionActive ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionActive
                        && oldItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseInactive ==
                        newItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseInactive
                        && oldItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseUid ==
                        newItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseUid
                        && oldItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseText ==
                        newItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseText
                        && oldItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseOptionSelected ==
                        newItem.clazzWorkQuestionResponse.clazzWorkQuestionResponseOptionSelected
            }
        }
    }
}