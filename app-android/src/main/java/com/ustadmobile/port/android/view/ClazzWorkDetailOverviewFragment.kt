package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.controller.ClazzWorkDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

interface NewCommentHandler{
    fun addComment(view: View, comment: String?, public:Boolean?)
}

interface SimpleButtonHandler{
    fun onClickButton(view: View)
}

class ClazzWorkDetailOverviewFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkDetailOverviewView, NewCommentHandler, SimpleButtonHandler{

    internal var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkDetailOverviewPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    //TODO: Build Content list when ready
    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentRecyclerView: RecyclerView? = null

    private var quizQuestionsRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter? = null
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


    class ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(
            var presenter: ClazzWorkDetailOverviewPresenter?, var studentMode: Boolean)
        : ListAdapter<ClazzWorkQuestionAndOptionWithResponse,
            ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder>(
            DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE) {

        class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionandoptionswithresponseBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
            val viewHolder = ClazzWorkQuestionViewHolder(ItemClazzworkquestionandoptionswithresponseBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.freeTextType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT
            viewHolder.binding.quizType = ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
            viewHolder.binding.clazzWorkQuizType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
            viewHolder.binding.studentMode = studentMode?:false
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
            holder.binding.clazzWorkQuestionAndOptionsWithResponse = getItem(position)
            holder.binding.studentMode = studentMode
        }
    }


    class CommentsRecyclerAdapter(var presenter: ClazzWorkDetailOverviewPresenter?)
        : SelectablePagedListAdapter<CommentsWithPerson,
            CommentsRecyclerAdapter.CommentsWithPersonViewHolder>(DIFF_CALLBACK_COMMENTS) {

        class CommentsWithPersonViewHolder(val binding: ItemCommetsListBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsWithPersonViewHolder {
            return CommentsWithPersonViewHolder(ItemCommetsListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: CommentsWithPersonViewHolder, position: Int) {
            if(itemCount > 0 ) {
                holder.binding.commentwithperson = getItem(position)
                //holder.binding.mPresenter = presenter
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    class SubmissionResultRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                          visible: Boolean = false)
        : ListAdapter<ClazzWorkWithSubmission,
            SubmissionResultRecyclerAdapter.SubmissionResultViewHolder>(DU_CLAZZWORKWITHSUBMISSION) {

        var visible: Boolean = visible
            set(value) {
                if(field == value)
                    return

                field = value
            }

        class SubmissionResultViewHolder(var itemBinding: ItemClazzworkSubmissionResultBinding)
            : RecyclerView.ViewHolder(itemBinding.root)

        private var viewHolder: SubmissionResultViewHolder? = null
        private var clazzWorkVal : ClazzWorkWithSubmission? = clazzWork

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionResultViewHolder {
            return SubmissionResultViewHolder(
                    ItemClazzworkSubmissionResultBinding.inflate(LayoutInflater.from(parent.context),
                            parent, false).also {
                    })
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            viewHolder = null
        }

        override fun getItemCount(): Int {
            return if(visible) 1 else 0
        }

        override fun onBindViewHolder(holder: SubmissionResultViewHolder, position: Int) {

            if(currentList.size > 0){
                holder.itemBinding.clazzWorkWithSubmission = getItem(0)
            }else {
                holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
            }
        }
    }

    class SubmissionTextEntryWithResultRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
        visible: Boolean = false)
        : ListAdapter<ClazzWorkWithSubmission,
            SubmissionTextEntryWithResultRecyclerAdapter.SubmissionTextEntryWithResultViewHolder>(DU_CLAZZWORKWITHSUBMISSION) {

        var visible: Boolean = visible
            set(value) {
                if(field == value)
                    return

                field = value
            }

        class SubmissionTextEntryWithResultViewHolder(var itemBinding: ItemClazzworkSubmissionTextEntryBinding)
            : RecyclerView.ViewHolder(itemBinding.root)

        private var viewHolder: SubmissionTextEntryWithResultViewHolder? = null
        private var clazzWorkVal : ClazzWorkWithSubmission? = clazzWork

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionTextEntryWithResultViewHolder {
            return SubmissionTextEntryWithResultViewHolder(
                    ItemClazzworkSubmissionTextEntryBinding.inflate(LayoutInflater.from(parent.context),
                            parent, false).also {
                        it.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
                    })
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            viewHolder = null
        }

        override fun getItemCount(): Int {
            return if(visible) 1 else 0
        }

        override fun onBindViewHolder(holder: SubmissionTextEntryWithResultViewHolder, position: Int) {

            if(currentList.size > 0){
                holder.itemBinding.clazzWorkWithSubmission = getItem(0)
            }else {
                holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
            }
        }
    }


    class ClazzWorkBasicDetailsRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                               visible: Boolean = false)
        : ListAdapter<ClazzWorkWithSubmission,
            ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder>(DU_CLAZZWORKWITHSUBMISSION) {

        var visible: Boolean = visible
            set(value) {
                if(field == value)
                    return

                field = value
            }

        class ClazzWorkDetailViewHolder(var itemBinding: ItemClazzworkDetailDescriptionBinding)
            : RecyclerView.ViewHolder(itemBinding.root)

        private var viewHolder: ClazzWorkDetailViewHolder? = null
        private var clazzWorkVal : ClazzWorkWithSubmission? = clazzWork

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkDetailViewHolder {
            return ClazzWorkDetailViewHolder(
                    ItemClazzworkDetailDescriptionBinding.inflate(LayoutInflater.from(parent.context),
                            parent, false).also {
                    })
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            viewHolder = null
        }

        override fun getItemCount(): Int {
            return if(visible) 1 else 0
        }

        override fun onBindViewHolder(holder: ClazzWorkDetailViewHolder, position: Int) {

            if(currentList.size > 0){
                holder.itemBinding.clazzWorkWithSubmission = getItem(0)
            }else {
                holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
            }
        }
    }


    override fun addComment(view: View, comment: String?, public: Boolean?) {
        (view.parent as View).findViewById<EditText>(R.id.item_comment_new_comment_et).setText("")
        mPresenter?.addComment(comment?:"", public?:false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentClazzWorkWithSubmissionDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())

        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_with_submission_detail_rv)

        mPresenter = ClazzWorkDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        //Main Merger:PP
        detailRecyclerAdapter = ClazzWorkBasicDetailsRecyclerAdapter(entity)
        detailRecyclerAdapter?.visible = false

        quizQuestionsRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(
                mPresenter, studentMode)
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
        privateCommentsHeadingRecyclerAdapter?.visible = true

        submissionResultRecyclerAdapter = SubmissionResultRecyclerAdapter(entity)
        submissionResultRecyclerAdapter?.visible = true

        submissionFreeTextRecyclerAdapter = SubmissionTextEntryWithResultRecyclerAdapter(entity)
        submissionFreeTextRecyclerAdapter?.visible = false


        //Public comments:
        newPublicCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_class_comment), true
        )
        newPublicCommentRecyclerAdapter?.visible = true

        publicCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also {
            publicCommentsObserver = PagedListSubmitObserver(it)
        }

        publicCommentsMergerRecyclerAdapter = MergeAdapter(newPublicCommentRecyclerAdapter,
                publicCommentsRecyclerAdapter)

        //Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false
        )
        newPrivateCommentRecyclerAdapter?.visible = true

        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }
        privateCommentsMergerRecyclerAdapter = MergeAdapter(newPrivateCommentRecyclerAdapter,
                        privateCommentsRecyclerAdapter)

        contentHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(
                getText(R.string.content).toString()
        )
        contentHeadingRecyclerAdapter?.visible = true

        detailMergerRecyclerAdapter = MergeAdapter(
                detailRecyclerAdapter,
                contentHeadingRecyclerAdapter,
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
        mPresenter?.handleClickSubmit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
        quizQuestionsRecyclerAdapter = null
        publicCommentsRecyclerAdapter = null
        privateCommentsRecyclerAdapter = null

    }

    override var studentMode: Boolean = false
        get() = field
        set(value) {
            field = value
            newPrivateCommentRecyclerAdapter?.visible = value
            privateCommentsHeadingRecyclerAdapter?.visible = value

            submissionButtonRecyclerAdapter?.visible = value
            quizQuestionsRecyclerAdapter?.studentMode = value
            submissionFreeTextRecyclerAdapter?.visible = value
            if(entity?.clazzWorkCommentsEnabled == false){
                privateCommentsHeadingRecyclerAdapter?.visible = false
                newPrivateCommentRecyclerAdapter?.visible = false
            }else if (studentMode){
                privateCommentsHeadingRecyclerAdapter?.visible = true
                newPrivateCommentRecyclerAdapter?.visible = true
            }else if(entity?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ){
                questionsHeadingRecyclerAdapter?.visible = true
            }

        }

    override var entity: ClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkWithSubmission = value
            detailRecyclerAdapter?.submitList(listOf(entity))
            detailRecyclerAdapter?.visible = true

            submissionResultRecyclerAdapter?.submitList(listOf(entity))

            if(value?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT && studentMode){
                submissionFreeTextRecyclerAdapter?.submitList(listOf(entity))
                submissionFreeTextRecyclerAdapter?.visible = true
            }else{
                submissionFreeTextRecyclerAdapter?.visible = false
            }

            if(value?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ && !studentMode){
                questionsHeadingRecyclerAdapter?.visible = true
            }

            submissionButtonRecyclerAdapter?.visible = studentMode &&
                    value?.clazzWorkSubmission?.clazzWorkSubmissionUid == 0L &&
                    value?.clazzWorkSubmissionType != ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE

            submissionHeadingRecyclerAdapter?.visible =
                    submissionButtonRecyclerAdapter?.visible?:false

            if(entity?.clazzWorkCommentsEnabled == false){
                privateCommentsHeadingRecyclerAdapter?.visible = false
                newPrivateCommentRecyclerAdapter?.visible = false
            }else if (studentMode){
                privateCommentsHeadingRecyclerAdapter?.visible = true
                newPrivateCommentRecyclerAdapter?.visible = true
            }
        }

    //TODO: Content when ready.
    override var clazzWorkContent: DataSource.Factory<Int, ContentEntryWithMetrics>? = null
        get() = field
        set(value) {
            field = value
        }


    override var clazzWorkQuizQuestionsAndOptionsWithResponse
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        get() = field
        set(value) {
            field?.removeObserver(quizQuestionAndResponseObserver)
            field = value
            value?.observe(viewLifecycleOwner, quizQuestionAndResponseObserver)
        }

    override var timeZone: String = ""
        get() = field
        set(value) {
            field = value
        }

    override var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        get() = field
        set(value) {
            val publicCommentsObserverVal = publicCommentsObserver?:return
            publicCommentsLiveData?.removeObserver(publicCommentsObserverVal)
            publicCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            publicCommentsLiveData?.observe(viewLifecycleOwner, publicCommentsObserverVal)

        }

    override var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        get() = field
        set(value) {
            val privateCommentsObserverVal = privateCommentsObserver?:return
            privateCommentsLiveData?.removeObserver(privateCommentsObserverVal)
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observe(viewLifecycleOwner, privateCommentsObserverVal)
        }


    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    companion object {
        val DIFF_CALLBACK_COMMENTS = object : DiffUtil.ItemCallback<CommentsWithPerson>() {
            override fun areItemsTheSame(oldItem: CommentsWithPerson,
                                         newItem: CommentsWithPerson): Boolean {
                return oldItem.commentsUid == newItem.commentsUid
            }

            override fun areContentsTheSame(oldItem: CommentsWithPerson,
                                            newItem: CommentsWithPerson): Boolean {
                return oldItem == newItem
            }
        }

        val DU_CLAZZWORKWITHSUBMISSION = object: DiffUtil.ItemCallback<ClazzWorkWithSubmission>() {
            override fun areItemsTheSame(oldItem: ClazzWorkWithSubmission,
                                         newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkWithSubmission,
                                            newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem == newItem
            }
        }

        val DU_CLAZZWORKQUESTIONANDOPTIONWITHRESPONSE = object
            : DiffUtil.ItemCallback<ClazzWorkQuestionAndOptionWithResponse>() {
            override fun areItemsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse,
                                         newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                return oldItem.clazzWorkQuestion.clazzWorkQuestionUid ===
                        newItem.clazzWorkQuestion.clazzWorkQuestionUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse,
                                            newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                return oldItem == newItem
            }
        }
    }
}