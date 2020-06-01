package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSelquestionsetEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionBinding
import com.ustadmobile.core.controller.SelQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.SelQuestionSetEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionSetEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity

interface SelQuestionSetEditActivityEventHandler {
    fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?)
    fun onClickNewSelQuestion()
    fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions)
}

class SelQuestionSetEditFragment : UstadEditFragment<SelQuestionSet>(), SelQuestionSetEditView,
    SelQuestionSetEditActivityEventHandler{

    private var mBinding: FragmentSelquestionsetEditBinding? = null

    private var mPresenter: SelQuestionSetEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SelQuestionSet>?
        get() = mPresenter

    private var selQuestionRecyclerAdapter: SelQuestionRecyclerAdapter? = null

    private var selQuestionRecyclerView: RecyclerView? = null

    private val selQuestionObserver = Observer<List<SelQuestionAndOptions>?> {
        t -> selQuestionRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()

    override var selQuestionList: DoorLiveData<List<SelQuestionAndOptions>>? = null
        get() = field
        set(value) {
            field?.removeObserver(selQuestionObserver)
            field = value
            value?.observe(this, selQuestionObserver)
        }

    class SelQuestionRecyclerAdapter(
            val activityEventHandler: SelQuestionSetEditActivityEventHandler,
            var presenter: SelQuestionSetEditPresenter?)
        : ListAdapter<SelQuestionAndOptions,
            SelQuestionRecyclerAdapter.SelQuestionViewHolder>(DIFF_CALLBACK_SELQUESTIONANDOPTIONS) {

        class SelQuestionViewHolder(val binding: ItemSelquestionBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelQuestionViewHolder {
            val viewHolder = SelQuestionViewHolder(ItemSelquestionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            viewHolder.binding.questionTypeList =
                    SelQuestionAndOptionsEditPresenter.QuestionOptions.values()
                            .map { MessageIdOption(it.messageId, parent.context, it.optionVal) }
            return viewHolder
        }

        override fun onBindViewHolder(holder: SelQuestionViewHolder, position: Int) {
            holder.binding.selQuestion = getItem(position)
        }
    }


    override var entity: SelQuestionSet? = null
        get() = field
        set(value) {
            mBinding?.selquestionset = value
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.loading = value
        }


    override fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(selQuestion, R.id.selquestionandoptions_edit_dest,
                SelQuestionAndOptions::class.java)
    }

    override fun onClickNewSelQuestion() =
            onClickEditSelQuestion(null)


    override fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions) {
        mPresenter?.handleRemoveSelQuestion(selQuestion)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView : View
        mBinding = FragmentSelquestionsetEditBinding.inflate(inflater, container, false)
                .also{
                    rootView = it.root
                    it.activityEventHandler = this
                }

        selQuestionRecyclerView = rootView.findViewById(R.id.activity_selquestion_recycleradapter)
        selQuestionRecyclerAdapter = SelQuestionRecyclerAdapter(this, null)
        selQuestionRecyclerView?.adapter = selQuestionRecyclerAdapter
        selQuestionRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = SelQuestionSetEditPresenter(requireContext(), arguments.toStringMap(),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))
        //mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        //After the presenter is created
        selQuestionRecyclerAdapter?.presenter = mPresenter

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        setEditFragmentTitle(R.string.sel_question_set)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SelQuestionAndOptions::class.java) {
            val selQuestionAndOptions = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSelQuestion(selQuestionAndOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.sel_question_set)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        selQuestionRecyclerView?.adapter = null
        selQuestionRecyclerView = null
        selQuestionRecyclerAdapter = null
        selQuestionList = null
    }

    companion object {

        val DIFF_CALLBACK_SELQUESTIONANDOPTIONS = object: DiffUtil.ItemCallback<SelQuestionAndOptions>() {
            override fun areItemsTheSame(oldItem: SelQuestionAndOptions, newItem: SelQuestionAndOptions): Boolean {
                return oldItem.selQuestion.selQuestionUid == newItem.selQuestion.selQuestionUid
            }

            override fun areContentsTheSame(oldItem: SelQuestionAndOptions, newItem: SelQuestionAndOptions): Boolean {
                return oldItem == newItem
            }
        }

    }


}
