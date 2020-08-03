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
import androidx.recyclerview.widget.MergeAdapter
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

class ClazzWorkDetailOverviewFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkDetailOverviewView, NewCommentHandler, SimpleButtonHandler{

    internal var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkDetailOverviewPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    val accountManager: UstadAccountManager by instance()

    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentLiveData: LiveData<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
    private val contentObserver = Observer<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?> {
        t ->
        run {
            if (t?.size ?: 0 > 0) {
                contentHeadingRecyclerAdapter?.visible = true
            }
            contentRecyclerAdapter?.submitList(t)
        }
    }

    private var quizQuestionsRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseRA? = null
    private val quizQuestionAndResponseObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizQuestionsRecyclerAdapter?.submitList(t)
    }

    private var contentHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var submissionResultRecyclerAdapter: SubmissionResultRecyclerAdapter? = null
    private var submissionFreeTextRecyclerAdapter: SubmissionTextEntryWithResultRecyclerAdapter ? = null

    private var submissionHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var questionsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter?= null
    private var submissionButtonRecyclerAdapter: SimpleButtonRecyclerAdapter? = null
    private var publicCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null
    private var privateCommentsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null

    private var publicCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var publicCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var publicCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPublicCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var publicCommentsMergerRecyclerAdapter: MergeAdapter? = null

    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsMergerRecyclerAdapter: MergeAdapter? = null

    private var detailRecyclerAdapter: ClazzWorkBasicDetailsRecyclerAdapter? = null
    private var detailMergerRecyclerAdapter: MergeAdapter? = null
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

        //fragment_list_recyclerview
        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_with_submission_detail_rv)

        
        //Main Merger:PP
        detailRecyclerAdapter = ClazzWorkBasicDetailsRecyclerAdapter(entity)
        detailRecyclerAdapter?.visible = false

        contentRecyclerAdapter = ContentEntryListRecyclerAdapter(
                DefaultContentEntryListItemListener(context = requireContext(), di = di),
                ListViewMode.BROWSER.toString())

        quizQuestionsRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseRA(
                isStudent)
        submissionHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.submission).toString())
        submissionHeadingRecyclerAdapter?.visible = false

        questionsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.questions).toString())
        questionsHeadingRecyclerAdapter?.visible = false

        submissionButtonRecyclerAdapter = SimpleButtonRecyclerAdapter(
                getText(R.string.submitliteral).toString(), this)
        submissionButtonRecyclerAdapter?.visible = false

        publicCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.class_comments).toString()
        )
        publicCommentsHeadingRecyclerAdapter?.visible = true

        privateCommentsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.private_comments).toString()
        )
        privateCommentsHeadingRecyclerAdapter?.visible = false

        submissionResultRecyclerAdapter = SubmissionResultRecyclerAdapter(entity)
        submissionResultRecyclerAdapter?.visible = false

        submissionFreeTextRecyclerAdapter = SubmissionTextEntryWithResultRecyclerAdapter()
        submissionFreeTextRecyclerAdapter?.visible = false


        //Public comments:
        newPublicCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_class_comment), true, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWorkUid?:0L, 0,
                accountManager.activeAccount.personUid)

        newPublicCommentRecyclerAdapter?.visible = true

        publicCommentsRecyclerAdapter = CommentsRecyclerAdapter().also {
            publicCommentsObserver = PagedListSubmitObserver(it)
        }

        publicCommentsMergerRecyclerAdapter = MergeAdapter(publicCommentsRecyclerAdapter,
                newPublicCommentRecyclerAdapter)

        //Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false, ClazzWork.CLAZZ_WORK_TABLE_ID,
                entity?.clazzWorkUid?:0L, 0,
                accountManager.activeAccount.personUid)
        newPrivateCommentRecyclerAdapter?.visible = false

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter().also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }
        privateCommentsMergerRecyclerAdapter = MergeAdapter(newPrivateCommentRecyclerAdapter,
                        privateCommentsRecyclerAdapter)

        contentHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.content).toString()
        )
        contentHeadingRecyclerAdapter?.visible = false


        mPresenter = ClazzWorkDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, this)

        detailMergerRecyclerAdapter = MergeAdapter(
                detailRecyclerAdapter,
                contentHeadingRecyclerAdapter, contentRecyclerAdapter,
                submissionHeadingRecyclerAdapter,
                submissionResultRecyclerAdapter, submissionFreeTextRecyclerAdapter,
                questionsHeadingRecyclerAdapter,
                quizQuestionsRecyclerAdapter, submissionButtonRecyclerAdapter,
                publicCommentsHeadingRecyclerAdapter, publicCommentsMergerRecyclerAdapter,
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

    override fun onClickButton(view: View) {
        //TODO: Disable Submission views.
        mPresenter?.handleClickSubmit()
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
        publicCommentsRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null
        newPublicCommentRecyclerAdapter = null
        detailRecyclerAdapter = null
        contentHeadingRecyclerAdapter = null
        contentRecyclerAdapter = null
        submissionHeadingRecyclerAdapter = null
        submissionResultRecyclerAdapter = null
        submissionFreeTextRecyclerAdapter = null
        questionsHeadingRecyclerAdapter = null
        quizQuestionsRecyclerAdapter = null
        submissionButtonRecyclerAdapter = null
        publicCommentsHeadingRecyclerAdapter = null
        publicCommentsMergerRecyclerAdapter = null
        privateCommentsHeadingRecyclerAdapter = null
        privateCommentsMergerRecyclerAdapter = null

    }

    override var isStudent: Boolean = false
        set(value) {
            field = value
            quizQuestionsRecyclerAdapter?.studentMode = value
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

            submissionButtonRecyclerAdapter?.visible = isStudent &&
                    (entity?.clazzWorkSubmission?.clazzWorkSubmissionUid == 0L || entity?.clazzWorkSubmission == null)
                    &&
                    (entity?.clazzWorkSubmission == null || entity?.clazzWorkSubmissionType !=
                            ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE)

            submissionResultRecyclerAdapter?.visible = isStudent &&
                    entity?.clazzWorkSubmission?.clazzWorkSubmissionMarkerPersonUid != 0L


            submissionHeadingRecyclerAdapter?.visible = isStudent &&
                    (submissionResultRecyclerAdapter?.visible?:false ||
                            submissionFreeTextRecyclerAdapter?.visible?:false ||
                            submissionButtonRecyclerAdapter?.visible?:false)

        }

    override var entity: ClazzWorkWithSubmission? = null
        set(value) {
            field = value
            detailRecyclerAdapter?._clazzWork = entity
            detailRecyclerAdapter?.visible = true


            submissionResultRecyclerAdapter?._clazzWork = entity
            submissionResultRecyclerAdapter?.visible = isStudent &&
                    value?.clazzWorkSubmission?.clazzWorkSubmissionMarkerPersonUid != 0L

            submissionFreeTextRecyclerAdapter?.visible = value?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT && isStudent

            if(submissionFreeTextRecyclerAdapter?.visible == true){
                submissionFreeTextRecyclerAdapter?.submitList(listOf(entity))
            }else{
                submissionFreeTextRecyclerAdapter?.submitList(listOf())
            }

            if(value?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ && !isStudent){
                questionsHeadingRecyclerAdapter?.visible = true
            }

            submissionButtonRecyclerAdapter?.visible = isStudent &&
                    value?.clazzWorkSubmission?.clazzWorkSubmissionUid == 0L &&
                    value.clazzWorkSubmissionType != ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE


            submissionHeadingRecyclerAdapter?.visible = isStudent &&
                    (submissionResultRecyclerAdapter?.visible?:false ||
                    submissionFreeTextRecyclerAdapter?.visible?:false ||
                    submissionButtonRecyclerAdapter?.visible?:false)


            newPublicCommentRecyclerAdapter?.entityUid = entity?.clazzWorkUid?:0L
            newPublicCommentRecyclerAdapter?.entityUid = entity?.clazzWorkUid?:0L

            if(entity?.clazzWorkCommentsEnabled == false && isStudent ){
                privateCommentsHeadingRecyclerAdapter?.visible = true
                newPrivateCommentRecyclerAdapter?.visible = false
            }else if (isStudent){
                privateCommentsHeadingRecyclerAdapter?.visible = true
                newPrivateCommentRecyclerAdapter?.visible = true
            }else{
                privateCommentsHeadingRecyclerAdapter?.visible = false
                newPrivateCommentRecyclerAdapter?.visible = false
            }
        }

    override var clazzWorkContent: DataSource.Factory<Int,
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null
        set(value) {
            contentLiveData?.removeObserver(contentObserver)
            contentLiveData = value?.asRepositoryLiveData(ClazzWorkDao)
            field = value
            contentLiveData?.observe(viewLifecycleOwner, contentObserver)
        }


    override var clazzWorkQuizQuestionsAndOptionsWithResponse
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        set(value) {
            field?.removeObserver(quizQuestionAndResponseObserver)
            field = value
            value?.observe(viewLifecycleOwner, quizQuestionAndResponseObserver)
        }

    override var timeZone: String = ""

    override var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val publicCommentsObserverVal = publicCommentsObserver?:return
            publicCommentsLiveData?.removeObserver(publicCommentsObserverVal)
            publicCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            publicCommentsLiveData?.observe(viewLifecycleOwner, publicCommentsObserverVal)

        }

    override var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observe(viewLifecycleOwner, privateCommentsObserverVal)
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
                return oldItem == newItem
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
                return oldItem == newItem
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
                return oldItem == newItem
            }
        }
    }
}