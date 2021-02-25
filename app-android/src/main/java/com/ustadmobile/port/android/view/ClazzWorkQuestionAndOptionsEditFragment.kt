package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkQuestionAndOptionsEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzWorkQuestionOptionBinding
import com.ustadmobile.core.controller.ClazzWorkQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkQuestionAndOptionsEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion.Companion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


interface ClazzWorkQuestionAndOptionsEditFragmentEventHandler {
    fun handleRemoveOption(option: ClazzWorkQuestionOption, view: View)
}

class ClazzWorkQuestionAndOptionsEditFragment: UstadEditFragment<ClazzWorkQuestionAndOptions>(),
        ClazzWorkQuestionAndOptionsEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>,
        ClazzWorkQuestionAndOptionsEditFragmentEventHandler {

    private var mBinding: FragmentClazzWorkQuestionAndOptionsEditBinding? = null

    private var mPresenter: ClazzWorkQuestionAndOptionsEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWorkQuestionAndOptions>?
        get() = mPresenter

    private var optionsRecyclerAdapter: OptionsRecyclerAdapter? = null
    private var optionsRecyclerView: RecyclerView? = null
    private val optionsObserver = Observer<List<ClazzWorkQuestionOption?>?> {
        t-> optionsRecyclerAdapter?.submitList(t)
    }

    class EntityClassViewHolder(val binding: ItemClazzWorkQuestionOptionBinding)
        : RecyclerView.ViewHolder(binding.root)

    class OptionsRecyclerAdapter(
            var presenter: ClazzWorkQuestionAndOptionsEditPresenter?,
            var fragment: ClazzWorkQuestionAndOptionsEditFragmentEventHandler)
        : ListAdapter<ClazzWorkQuestionOption,
            EntityClassViewHolder>(DIFF_CALLBACK_CLAZZ_WORK_QUESTION_OPTION) {



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityClassViewHolder {
            val viewHolder = EntityClassViewHolder(ItemClazzWorkQuestionOptionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))

            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mFragment = fragment
            return viewHolder
        }

        override fun onBindViewHolder(holder: EntityClassViewHolder, position: Int) {
            val option = getItem(position)
            holder.binding.clazzWorkQuestionOption = option
        }
    }

    override val viewContext: Any
        get() = requireContext()

    override var clazzWorkQuestionOptionList: DoorMutableLiveData<List<ClazzWorkQuestionOption>>? = null
        get() = field
        set(value) {
            field?.removeObserver(optionsObserver)
            field = value
            value?.observe(this, optionsObserver)
        }

    override var clazzWorkQuestionOptionDeactivateList: DoorMutableLiveData<List<ClazzWorkQuestionOption>>? = null
        get() = field
        set(value) {
            field = value
        }
    override var typeOptions
            : List<ClazzWorkQuestionAndOptionsEditPresenter.ClazzWorkQuestionOptionTypeMessageIdOption>? = null
        get() = field
        set(value) {
            mBinding?.typeOptions = value
            field = value
        }

    override fun handleRemoveOption(option: ClazzWorkQuestionOption, view: View) {
        /**
         * IMPORTANT: If the item being deleted from a recyclerview has focus, the entire app will crash.
         */
        mBinding?.fragmentClazzWorkQuestionAndOptionsEditEditQuestionNameTiet?.requestFocus()
        Handler().post {
            mPresenter?.removeQuestionOption(option)
        }
    }

    override var errorMessage: String? = null
        set(value) {
            field = value

            mBinding?.errorText = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkQuestionAndOptionsEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.questionTypeSelectionListener = this
        }

        optionsRecyclerView = rootView.findViewById(R.id.fragment_clazz_work_question_and_options_edit_rv)
        optionsRecyclerAdapter = OptionsRecyclerAdapter(null, this)
        optionsRecyclerView?.adapter = optionsRecyclerAdapter
        optionsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mPresenter = ClazzWorkQuestionAndOptionsEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)

        //After the presenter is created
        optionsRecyclerAdapter?.presenter = mPresenter
        mBinding?.presenter = mPresenter

        setEditFragmentTitle(R.string.add_question, R.string.edit_question)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.optionsVisibility = if(selectedOption.optionId == CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE) View.VISIBLE else View.GONE
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        optionsRecyclerView?.adapter = null
        optionsRecyclerAdapter = null
        optionsRecyclerView = null
        clazzWorkQuestionOptionList = null

    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_question, R.string.edit_question)
    }

    override var entity: ClazzWorkQuestionAndOptions? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkQuestion = value?.clazzWorkQuestion
            mBinding?.optionsVisibility = if(value?.clazzWorkQuestion?.clazzWorkQuestionType ==
                    CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE) {
                View.VISIBLE
            }else {
                View.GONE
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        val DIFF_CALLBACK_CLAZZ_WORK_QUESTION_OPTION = object
            : DiffUtil.ItemCallback<ClazzWorkQuestionOption>() {
            override fun areItemsTheSame(oldItem: ClazzWorkQuestionOption,
                                         newItem: ClazzWorkQuestionOption): Boolean {
                return oldItem.clazzWorkQuestionOptionUid == newItem.clazzWorkQuestionOptionUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkQuestionOption,
                                            newItem: ClazzWorkQuestionOption): Boolean {
                return oldItem === newItem
            }
        }
    }
}