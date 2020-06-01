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
import com.toughra.ustadmobile.databinding.FragmentClazzWorkWithSubmissionDetailBinding
import com.toughra.ustadmobile.databinding.ItemCommetsListBinding
import com.ustadmobile.core.controller.ClazzWorkWithSubmissionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkWithSubmissionDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.android.synthetic.main.item_comment_new.view.*


class ClazzWorkWithSubmissionDetailFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkWithSubmissionDetailView {

    private var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkWithSubmissionDetailPresenter? = null

    private lateinit var dbRepo : UmAppDatabase

    //TODO: Build Content list when ready
    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentRecyclerView: RecyclerView? = null

    private var quizQuestionsRecyclerAdapter: ClazzWorkEditFragment.ClazzWorkQuestionRecyclerAdapter? = null
    private var quizQuestionsRecyclerView: RecyclerView? = null
    private var quizLiveData: LiveData<PagedList<ClazzWorkQuestionAndOptions>>? = null
    private val quizQuestionObserver = Observer<List<ClazzWorkQuestionAndOptions>> { t ->
        quizQuestionsRecyclerAdapter?.submitList(t)
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
    private var privateCommentsObserver = Observer<PagedList<CommentsWithPerson>> { t ->
        privateCommentsRecyclerAdapter?.submitList(t)
    }
    private var newPrivateCommentRecyclerAdapter: NewCommentRecyclerViewAdapter? = null
    protected var privateCommentsMergerRecyclerAdapter: MergeAdapter? = null


    class CommentsRecyclerAdapter(var presenter: ClazzWorkWithSubmissionDetailPresenter?)
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

    private val onNewPublicComment: View.OnClickListener = View.OnClickListener {
        //TODO: check
        mPresenter?.addComment(it.comment_text.text.toString(), true)

    }
    private val onNewPrivateComment: View.OnClickListener = View.OnClickListener {
        //TODO: check
        mPresenter?.addComment(it.comment_text.text.toString(), false)
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

        mPresenter = ClazzWorkWithSubmissionDetailPresenter(requireContext(),
                arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)


        //Public comments:
        newPublicCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(onNewPublicComment,
                requireContext().getString(R.string.add_class_comment)
        )
        publicCommentsRecyclerAdapter = CommentsRecyclerAdapter(mPresenter).also {
            publicCommentsObserver = PagedListSubmitObserver(it)
        }

        publicCommentsMergerRecyclerAdapter = MergeAdapter(newPublicCommentRecyclerAdapter,
                publicCommentsRecyclerAdapter)
        publicCommentsRecyclerView?.adapter = publicCommentsMergerRecyclerAdapter
        publicCommentsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        //Private comments section:
        newPrivateCommentRecyclerAdapter = NewCommentRecyclerViewAdapter(onNewPrivateComment,
                requireContext().getString(R.string.add_private_comment)
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


    override var clazzWorkQuizQuestionsAndOptions: DataSource.Factory<Int, ClazzWorkQuestionAndOptions>? = null
        get() = field
        set(value) {
            quizLiveData?.removeObserver(quizQuestionObserver)
            quizLiveData = value?.asRepositoryLiveData(dbRepo.clazzWorkQuestionOptionDao)
            quizLiveData?.observe(this, quizQuestionObserver)

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
            publicCommentsLiveData?.observe(this, publicCommentsObserverVal)

        }

    override var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
        get() = field
        set(value) {
            privateCommentsLiveData?.removeObserver(privateCommentsObserver)
            field = value
            privateCommentsLiveData = value?.asRepositoryLiveData(dbRepo.commentsDao)
            privateCommentsLiveData?.observe(this, privateCommentsObserver)
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

    }

}