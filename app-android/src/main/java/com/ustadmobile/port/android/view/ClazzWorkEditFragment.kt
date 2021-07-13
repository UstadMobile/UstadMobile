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
import com.toughra.ustadmobile.databinding.ItemContentEntrySimpleListBinding
import com.ustadmobile.core.controller.ClazzWorkEditPresenter
import com.ustadmobile.core.controller.ContentEntryListItemListener
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady

interface ClazzWorkEditFragmentEventHandler {
    fun onClickClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions?)
    fun onClickNewQuestion()
    fun onClickNewContent()
    fun handleRemoveClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions)
}

class ClazzWorkEditFragment: UstadEditFragment<ClazzWork>(), ClazzWorkEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>,
        ClazzWorkEditFragmentEventHandler, ContentEntryListItemListener {


    class ContentEntryListAdapterRA(
            val activityEventHandler: ClazzWorkEditFragmentEventHandler,
            var presenter: ClazzWorkEditPresenter?): ListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
            ContentEntryListAdapterRA.ContentEntryListAdapterRAViewHolder>(ContentEntryList2Fragment.DIFF_CALLBACK) {

        class ContentEntryListAdapterRAViewHolder(
                val binding: ItemContentEntrySimpleListBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : ContentEntryListAdapterRAViewHolder {
            val viewHolder = ContentEntryListAdapterRAViewHolder(
                    ItemContentEntrySimpleListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            return viewHolder
        }

        override fun onBindViewHolder(holder: ContentEntryListAdapterRAViewHolder, position: Int) {
            holder.binding.contentEntry = getItem(position)
        }
    }

    var mBinding: FragmentClazzWorkEditBinding? = null

    private var mPresenter: ClazzWorkEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWork>?
        get() = mPresenter


    private var questionRecyclerAdapter: ClazzWorkQuestionRecyclerAdapter? = null
    private var questionRecyclerView: RecyclerView? = null
    private val questionObserver = Observer<List<ClazzWorkQuestionAndOptions>?> {
        t -> questionRecyclerAdapter?.submitList(t)
    }

    private var contentRecyclerAdapter: ContentEntryListAdapterRA? = null
    private var contentRecyclerView: RecyclerView? = null
    private val contentObserver = Observer<List<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?>{
        t -> contentRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.typeSelectionListener = this
        }

        contentRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_edit_content_rv)
        contentRecyclerAdapter = ContentEntryListAdapterRA(this, null)
        contentRecyclerView?.adapter = contentRecyclerAdapter
        contentRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        questionRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_edit_questions_rv)
        questionRecyclerAdapter = ClazzWorkQuestionRecyclerAdapter(this, null)
        questionRecyclerView?.adapter = questionRecyclerAdapter
        questionRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ClazzWorkEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)

        questionRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.add_a_new_clazzwork, R.string.edit_clazzwork)

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

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java){
            val contentEntrySelected = it.firstOrNull()?:return@observeResult
            mPresenter?.handleAddOrEditContent(contentEntrySelected)
        }
    }

    override var timeZone: String = ""
        set(value) {
            mBinding?.fragmentClazzWorkEditTimezoneTv?.text =
                    getText(R.string.class_timezone).toString() + " " + value
            field = value
        }

    override var entity: ClazzWork? = null
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

    override var clazzWorkQuizQuestionsAndOptions:
            DoorMutableLiveData<List<ClazzWorkQuestionAndOptions>>? = null
        set(value) {
            field?.removeObserver(questionObserver)
            field = value
            value?.observe(this, questionObserver)
        }


    override var clazzWorkContent: DoorMutableLiveData<List<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
        set(value) {
            field?.removeObserver(contentObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, contentObserver)
        }

    override var submissionTypeOptions: List<
            ClazzWorkEditPresenter.SubmissionOptionsMessageIdOption>? = null

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.questionsVisibility = if(selectedOption.optionId ==
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
        navigateToPickEntityFromList(
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java,
                R.id.content_entry_list_dest,
                bundleOf(ContentEntryList2View.ARG_CLAZZWORK_FILTER to
                        entity?.clazzWorkUid.toString(),
                        ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to
                                ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT,
                UstadView.ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString()))

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
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_a_new_clazzwork, R.string.edit_clazzwork)
    }

    override fun handleRemoveClazzWorkQuestion(clazzWorkQuestion: ClazzWorkQuestionAndOptions) {
        mPresenter?.handleRemoveQuestionAndOptions(clazzWorkQuestion)
    }

    override fun onClickContentEntry(
            entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {

    }

    override fun onClickSelectContentEntry(
            entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {

    }

    override fun onClickDownloadContentEntry(
            entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {

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