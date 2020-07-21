package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzworkquestionBinding
import com.ustadmobile.core.controller.ClazzWorkEditPresenter
import com.ustadmobile.core.controller.ClazzWorkQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

interface ClazzWorkEditFragmentEventHandler {
    fun onClickClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions?)
    fun onClickNewQuestion()
    fun onClickNewContent()
    fun handleRemoveClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions)
}

class ClazzWorkEditFragment: UstadEditFragment<ClazzWork>(), ClazzWorkEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption>,
        ClazzWorkEditFragmentEventHandler {

    private var mBinding: FragmentClazzWorkEditBinding? = null

    private var mPresenter: ClazzWorkEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWork>?
        get() = mPresenter

    private var questionRecyclerAdapter: ClazzWorkQuestionRecyclerAdapter? = null

    private var questionRecyclerView: RecyclerView? = null

    private val questionObserver = Observer<List<ClazzWorkQuestionAndOptions>?> {
        t -> questionRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()

    override var clazzWorkQuizQuestionsAndOptions: DoorMutableLiveData<List<ClazzWorkQuestionAndOptions>>? = null
        get() = field
        set(value) {
            field?.removeObserver(questionObserver)
            field = value
            value?.observe(this, questionObserver)
        }

    class ClazzWorkQuestionRecyclerAdapter(
            val activityEventHandler: ClazzWorkEditFragmentEventHandler,
            var presenter: ClazzWorkEditPresenter?)
        : ListAdapter<ClazzWorkQuestionAndOptions,
            ClazzWorkQuestionRecyclerAdapter.ClazzWorkQuestionViewHolder>(DIFF_CALLBACK_CLAZZ_WORK_QUESTION_OPTION) {

        class ClazzWorkQuestionViewHolder(val binding: ItemClazzworkquestionBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkQuestionViewHolder {
            val viewHolder = ClazzWorkQuestionViewHolder(ItemClazzworkquestionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            viewHolder.binding.questionTypeList =
                    ClazzWorkQuestionAndOptionsEditPresenter.ClazzWorkQuestionOptions.values()
                            .map { MessageIdOption(it.messageId, parent.context, it.optionVal) }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzWorkQuestionViewHolder, position: Int) {
            holder.binding.clazzWorkQuestionAndOptions = getItem(position)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.typeSelectionListener = this
        }

        questionRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_edit_questions_rv)
        questionRecyclerAdapter = ClazzWorkQuestionRecyclerAdapter(this, null)
        questionRecyclerView?.adapter = questionRecyclerAdapter
        questionRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ClazzWorkEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)

        questionRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.clazz_work)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ClazzWorkQuestionAndOptions::class.java) {
            val questionAndOptions = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditClazzQuestionAndOptions(questionAndOptions)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java){
            val contentEntrySelected = it.firstOrNull()?:return@observeResult
            mPresenter?.handleAddOrEditContent(contentEntrySelected)
        }
    }

    override var timeZone: String = ""
        get() = field
        set(value) {
            mBinding?.fragmentClazzWorkEditTimezoneTv?.text =
                    getText(R.string.class_timezone).toString() + " " + value
            field = value
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        questionRecyclerView?.adapter = null
        questionRecyclerAdapter = null
        questionRecyclerView = null
        clazzWorkQuizQuestionsAndOptions = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.clazz_work)
    }

    override var entity: ClazzWork? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWork = value
            mBinding?.typeOptions = this.submissionTypeOptions
            mBinding?.questionsVisibility = if(value?.clazzWorkSubmissionType ==
                    ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ){
                View.VISIBLE
            }else{
                View.GONE
            }

        }

    private val contentRecyclerAdapter: ContentEntryList2Fragment.ContentEntryListRecyclerAdapter? = null

    private val contentObserver = Observer<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?>{
        t -> contentRecyclerAdapter?.selectedItemsLiveData?.setVal(t?:listOf())
    }


    override var clazzWorkContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
        get() = field
        set(value) {
            field?.removeObserver(contentObserver)
            field = value
            value?.observe(this, contentObserver)
        }

    override var submissionTypeOptions: List<ClazzWorkEditPresenter.SubmissionOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        mBinding?.questionsVisibility = if(selectedOption.code ==
                ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ) View.VISIBLE else View.GONE
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {   }

    override fun onClickClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(clazzWorkQuestion, R.id.clazzworkquestionandoptions_edit_dest,
                ClazzWorkQuestionAndOptions::class.java)
    }

    override fun onClickNewQuestion() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(null, R.id.clazzworkquestionandoptions_edit_dest,
                ClazzWorkQuestionAndOptions::class.java)
    }

    override fun onClickNewContent() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(ContentEntry::class.java,
                R.id.content_entry_list_dest,
                bundleOf(ContentEntryList2View.ARG_CLAZZWORK_FILTER to
                        entity?.clazzWorkUid.toString()))

    }

    override fun handleRemoveClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions) {
        mPresenter?.handleRemoveQuestionAndOptions(clazzWorkQuestion)
    }

    companion object {
        val DIFF_CALLBACK_CLAZZ_WORK_QUESTION_OPTION = object
            : DiffUtil.ItemCallback<ClazzWorkQuestionAndOptions>() {
            override fun areItemsTheSame(oldItem: ClazzWorkQuestionAndOptions,
                                         newItem: ClazzWorkQuestionAndOptions): Boolean {
                return oldItem.clazzWorkQuestion.clazzWorkQuestionUid ==
                        newItem.clazzWorkQuestion.clazzWorkQuestionUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkQuestionAndOptions,
                                            newItem: ClazzWorkQuestionAndOptions): Boolean {
                return oldItem === newItem
            }
        }
    }
}