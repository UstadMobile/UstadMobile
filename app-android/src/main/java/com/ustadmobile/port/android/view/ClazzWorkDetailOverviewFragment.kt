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
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.controller.ClazzWorkDetailOverviewPresenter
import com.ustadmobile.core.controller.ClazzWorkEditPresenter
import com.ustadmobile.core.controller.ClazzWorkQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.android.synthetic.main.fragment_clazz_work_with_submission_detail.view.*

interface NewCommentHandler{
    fun addComment(view: View, comment: String?, public:Boolean?)
}

interface QuizQuestionHandler{
}
class ClazzWorkDetailOverviewFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkDetailOverviewView, NewCommentHandler, QuizQuestionHandler{

    private var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkDetailOverviewPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    //TODO: Build Content list when ready
    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentRecyclerView: RecyclerView? = null

    private var quizQuestionsRecyclerAdapter: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter? = null
    private var quizQuestionsRecyclerView: RecyclerView? = null
    private val quizQuestionAndResponseObserver = Observer<List<ClazzWorkQuestionAndOptionWithResponse>?> {
        t -> quizQuestionsRecyclerAdapter?.submitList(t)
    }

    private var publicCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var publicCommentsRecyclerView: RecyclerView? = null
    private var publicCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var publicCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPublicCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var publicCommentsMergerRecyclerAdapter: MergeAdapter? = null

    private var privateCommentsRecyclerAdapter: CommentsRecyclerAdapter? = null
    private var privateCommentsRecyclerView: RecyclerView? = null
    private var privateCommentsLiveData: LiveData<PagedList<CommentsWithPerson>>? = null
    private var privateCommentsObserver: Observer<PagedList<CommentsWithPerson>>? = null
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    private var privateCommentsMergerRecyclerAdapter: MergeAdapter? = null


    private var detailMergerRecyclerAdapter: MergeAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null

    private var detailRecyclerAdapter: ClazzWorkDetailRecyclerAdapter? = null


    class ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(
            val activityEventHandler: QuizQuestionHandler,
            var presenter: ClazzWorkDetailOverviewPresenter?)
        : ListAdapter<ClazzWorkQuestionAndOptionWithResponse,
            ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder>(
            DIFFUTIL_CLAZZWORK_QUESTION_AND_OPTION_WITH_RESPONSE) {

        class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionandoptionswithresponseBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
            val viewHolder = ClazzWorkQuestionViewHolder(ItemClazzworkquestionandoptionswithresponseBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
            holder.binding.clazzWorkQuestionAndOptionsWithResponse = getItem(position)
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
            holder.binding.commentwithperson = getItem(position)
            //holder.binding.mPresenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }


    class ClazzWorkDetailRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?)
        : ListAdapter<ClazzWorkWithSubmission,
            ClazzWorkDetailRecyclerAdapter.ClazzWorkDetailViewHolder>(DIFFUTIL_CLAZZWORKWITHSUBMISSION) {

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
            return 1
        }

        override fun onBindViewHolder(holder: ClazzWorkDetailViewHolder, position: Int) {

            holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
        }
    }


    override fun addComment(view: View, comment: String?, public: Boolean?) {
        //TODO: Fix this : Disable comment Text
        if(view.comment_text != null) {
            view.comment_text.setText("")
        }
        mPresenter?.addComment(comment?:"", public?:false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentClazzWorkWithSubmissionDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())

        publicCommentsRecyclerView = rootView.findViewById(R.id.public_comments_rv)
        privateCommentsRecyclerView = rootView.findViewById(R.id.private_comments_rv)
        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_with_submission_detail_rv)

        mPresenter = ClazzWorkDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        //Main Merger:
        detailRecyclerAdapter = ClazzWorkDetailRecyclerAdapter(entity)
        quizQuestionsRecyclerAdapter = ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter(this, mPresenter)
        detailMergerRecyclerAdapter = MergeAdapter(detailRecyclerAdapter, quizQuestionsRecyclerAdapter)
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        //Public comments:
        newPublicCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_class_comment), true
        )
        publicCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also {
            publicCommentsObserver = PagedListSubmitObserver(it)
        }

        publicCommentsMergerRecyclerAdapter = MergeAdapter(newPublicCommentRecyclerAdapter,
                publicCommentsRecyclerAdapter)
        publicCommentsRecyclerView?.adapter = publicCommentsMergerRecyclerAdapter
        publicCommentsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        //Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_private_comment), false
        )
        privateCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also{
            privateCommentsObserver = PagedListSubmitObserver(it)
        }
        privateCommentsMergerRecyclerAdapter = MergeAdapter(newPrivateCommentRecyclerAdapter,
                        privateCommentsRecyclerAdapter)
        privateCommentsRecyclerView?.adapter = privateCommentsMergerRecyclerAdapter
        privateCommentsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

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
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
        quizQuestionsRecyclerView?.adapter = null
        quizQuestionsRecyclerAdapter = null
        quizQuestionsRecyclerView = null
        publicCommentsRecyclerView?.adapter = null
        publicCommentsRecyclerAdapter = null
        publicCommentsRecyclerView = null
        privateCommentsRecyclerView?.adapter = null
        privateCommentsRecyclerAdapter = null
        privateCommentsRecyclerView = null

    }


    override var studentMode: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value){
                mBinding?.studentVisibility = View.VISIBLE
            }else{
                //Teacher
                mBinding?.studentVisibility = View.GONE
                mBinding?.quizVisibility = View.GONE
                mBinding?.attachmentVisibility = View.GONE
                mBinding?.freeTextVisibility = View.GONE
            }
        }

    override var entity: ClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkWithSubmission = value
            detailRecyclerAdapter?.submitList(listOf(entity))

            if(entity?.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT){
                mBinding?.quizVisibility = View.GONE
                mBinding?.attachmentVisibility = View.GONE
                mBinding?.freeTextVisibility = View.VISIBLE
            }else if(entity?.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ){
                mBinding?.quizVisibility = View.VISIBLE
                mBinding?.attachmentVisibility = View.GONE
                mBinding?.freeTextVisibility = View.GONE
            }else if(entity?.clazzWorkSubmissionType == ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE){
                mBinding?.quizVisibility = View.GONE
                mBinding?.attachmentVisibility = View.GONE
                mBinding?.freeTextVisibility = View.GONE
            }
        }

    //TODO: Content when ready.
    override var clazzWorkContent: DataSource.Factory<Int, ContentEntryWithMetrics>? = null
        get() = field
        set(value) {
            field = value
        }


    override var clazzWorkQuizQuestionsAndOptionsWithResponse: DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>? = null
        get() = field
        set(value) {
            field = value
        }

    override var timeZone: String = ""
        get() = field
        set(value) {
            field = value
            mBinding?.startDateTimezone?.text = "($value)"
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

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }
    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    companion object {
        val DIFF_CALLBACK_COMMENTS = object : DiffUtil.ItemCallback<CommentsWithPerson>() {
            override fun areItemsTheSame(oldItem: CommentsWithPerson, newItem: CommentsWithPerson): Boolean {
                return oldItem.commentsUid == newItem.commentsUid
            }

            override fun areContentsTheSame(oldItem: CommentsWithPerson,
                                            newItem: CommentsWithPerson): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_CLAZZWORKWITHSUBMISSION = object: DiffUtil.ItemCallback<ClazzWorkWithSubmission>() {
            override fun areItemsTheSame(oldItem: ClazzWorkWithSubmission, newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkWithSubmission, newItem: ClazzWorkWithSubmission): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_CLAZZWORK_QUESTION_AND_OPTION_WITH_RESPONSE = object: DiffUtil.ItemCallback<ClazzWorkQuestionAndOptionWithResponse>() {
            override fun areItemsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse, newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                return oldItem.clazzWorkQuestion.clazzWorkQuestionUid == newItem.clazzWorkQuestion.clazzWorkQuestionUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkQuestionAndOptionWithResponse, newItem: ClazzWorkQuestionAndOptionWithResponse): Boolean {
                return oldItem == newItem
            }
        }

    }



}