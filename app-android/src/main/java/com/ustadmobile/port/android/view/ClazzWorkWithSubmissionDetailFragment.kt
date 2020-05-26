package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkWithSubmissionDetailBinding
import com.toughra.ustadmobile.databinding.ItemCommetsListBinding
import com.ustadmobile.core.controller.ClazzWorkWithSubmissionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.dao.ClazzWorkQuestionOptionDao
import com.ustadmobile.core.db.dao.CommentsDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkWithSubmissionDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class ClazzWorkWithSubmissionDetailFragment: UstadDetailFragment<ClazzWorkWithSubmission>(),
        ClazzWorkWithSubmissionDetailView{

    private var mBinding: FragmentClazzWorkWithSubmissionDetailBinding? = null

    private var mPresenter: ClazzWorkWithSubmissionDetailPresenter? = null

    //TODO: Build Content list when ready
    private var contentRecyclerAdapter: ContentEntryListRecyclerAdapter? = null
    private var contentRecyclerView: RecyclerView? = null

    private var quizQuestionsRecyclerAdapter: ClazzWorkEditFragment.ClazzWorkQuestionRecyclerAdapter? = null
    private var quizQuestionsRecyclerView: RecyclerView? = null
    private var quizLiveData: LiveData<PagedList<ClazzWorkQuestionAndOptions>>? = null
    private val quizQuestionObserver = Observer<List<ClazzWorkQuestionAndOptions>> {
        t -> quizQuestionsRecyclerAdapter?.submitList(t)
    }

    private var publicCommentsRecyclerAdapter :CommentsRecyclerAdapter? = null
    private var publicCommentsRecyclerView: RecyclerView? = null
    private var publicCommentsLiveData : LiveData<PagedList<CommentsWithPerson>>? = null
    private val publicCommentsObserver = Observer<List<CommentsWithPerson>> {
        t -> publicCommentsRecyclerAdapter?.submitList(t)
    }

    private var privateCommentsRecyclerAdapter :CommentsRecyclerAdapter? = null
    private var privateCommentsRecyclerView: RecyclerView? = null
    private var privateCommentsLiveData : LiveData<PagedList<CommentsWithPerson>>? = null
    private val privateCommentsObserver = Observer<List<CommentsWithPerson>> {
        t -> privateCommentsRecyclerAdapter?.submitList(t)
    }

        class CommentsRecyclerAdapter(var presenter: ClazzWorkWithSubmissionDetailPresenter?)
            : ListAdapter<CommentsWithPerson,
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

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView: View
            mBinding = FragmentClazzWorkWithSubmissionDetailBinding.inflate(inflater, container,
                    false).also {
                rootView = it.root
            }

            mPresenter = ClazzWorkWithSubmissionDetailPresenter(requireContext(),
                    arguments.toStringMap(), this,
                    this, UstadMobileSystemImpl.instance,
                    UmAccountManager.getActiveDatabase(requireContext()),
                    UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                    UmAccountManager.activeAccountLiveData)

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

        override var entity: ClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkWithSubmission = value
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
                quizLiveData = value?.asRepositoryLiveData(ClazzWorkQuestionOptionDao)
                quizLiveData?.observe(this, quizQuestionObserver)

            }

        override var timeZone: String = ""
            get() = field
            set(value) {
                //TODO: Update mBinding
                field = value
            }

        override var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>? = null
            get() = field
            set(value) {
                publicCommentsLiveData?.removeObserver(publicCommentsObserver)
                publicCommentsLiveData = value?.asRepositoryLiveData(CommentsDao)
                publicCommentsLiveData?.observe(this, publicCommentsObserver)
            }

        override var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>? = null
            get() = field
            set(value) {
                privateCommentsLiveData?.removeObserver(privateCommentsObserver)
                privateCommentsLiveData = value?.asRepositoryLiveData(CommentsDao)
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


        companion object{
        val DIFF_CALLBACK_COMMENTS = object: DiffUtil.ItemCallback<CommentsWithPerson>() {
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

}