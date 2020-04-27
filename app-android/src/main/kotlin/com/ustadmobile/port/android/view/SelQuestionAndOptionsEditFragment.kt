package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSelquestionandoptionsEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionoptionBinding
import com.ustadmobile.core.controller.SelQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.dao.SelQuestionDao.Companion.SEL_QUESTION_TYPE_MULTI_CHOICE
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionAndOptionsEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle


interface QuestionAndOptionsEditEventHandler {
    fun handleRemoveOption(option: SelQuestionOption, view: View)
}

class SelQuestionAndOptionsEditFragment : UstadEditFragment<SelQuestionAndOptions>(),
        SelQuestionAndOptionsEditView, DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption>,
        QuestionAndOptionsEditEventHandler{

    private var mBinding: FragmentSelquestionandoptionsEditBinding? = null

    private var mPresenter: SelQuestionAndOptionsEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SelQuestionAndOptions>?
        get() = mPresenter

    private var entityClassRecyclerAdapter: EntityClassRecyclerAdapter? = null

    private var entityClassRecyclerView: RecyclerView? = null

    private val entityClassObserver = Observer<List<SelQuestionOption>?> {
        t -> entityClassRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()


    override var selQuestionOptionList: DoorMutableLiveData<List<SelQuestionOption>>? = null
        get() = field
        set(value) {
            field?.removeObserver(entityClassObserver)
            field = value
            value?.observe(this, entityClassObserver)
        }
    override var selQuestionOptionDeactivateList: DoorMutableLiveData<List<SelQuestionOption>>? = null
        get() = field
        set(value) {
            field = value
        }

    class EntityClassViewHolder(val binding: ItemSelquestionoptionBinding)
        : RecyclerView.ViewHolder(binding.root)

    class EntityClassRecyclerAdapter(
            var presenter: SelQuestionAndOptionsEditPresenter?, var fragment: QuestionAndOptionsEditEventHandler)
        : ListAdapter<SelQuestionOption,
            EntityClassViewHolder>(DIFF_CALLBACK_SEL_QUESTION_OPTION) {



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityClassViewHolder {
            val viewHolder = EntityClassViewHolder(ItemSelquestionoptionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))

            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mFragment = fragment
            return viewHolder
        }

        override fun onBindViewHolder(holder: EntityClassViewHolder, position: Int) {
            val option = getItem(position)
            holder.binding.selQuestionOption = option
        }
    }

    override fun handleRemoveOption(option: SelQuestionOption, view: View) {
        /**
         * IMPORTANT: If the item being deleted from a recyclerview has focus, the entire app will crash.
         */
        mBinding?.activitySelquestionandoptionsEditQuestionsetNameText?.requestFocus()
        Handler().post {
            mPresenter?.removeQuestionOption(option)
        }
    }

    override var entity: SelQuestionAndOptions? = null
        get() = field
        set(value) {
            field = value
            mBinding?.selQuestion = value?.selQuestion
            mBinding?.optionsVisibility = if(value?.selQuestion?.questionType == SEL_QUESTION_TYPE_MULTI_CHOICE) {
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


    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.loading = value
        }

    override var typeOptions
            : List<SelQuestionAndOptionsEditPresenter.OptionTypeMessageIdOption>? = null
        get() = field
        set(value) {
            mBinding?.typeOptions = value
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView : View
        mBinding = FragmentSelquestionandoptionsEditBinding.inflate(inflater, container,
                false).also{
                    rootView = it.root
                    it.questionTypeSelectionListener = this
                }

        entityClassRecyclerView = rootView.findViewById(R.id.activity_selquestionandoptions_edit_rv)
        entityClassRecyclerAdapter = EntityClassRecyclerAdapter(null, this)
        entityClassRecyclerView?.adapter = entityClassRecyclerAdapter
        entityClassRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = SelQuestionAndOptionsEditPresenter(requireContext(), arguments.toStringMap(),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))

        //After the presenter is created
        entityClassRecyclerAdapter?.presenter = mPresenter
        mBinding?.presenter = mPresenter

        setEditFragmentTitle(R.string.question)

        return rootView
    }


    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        mBinding?.optionsVisibility = if(selectedOption.code == SEL_QUESTION_TYPE_MULTI_CHOICE) View.VISIBLE else View.GONE
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.question)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entityClassRecyclerView?.adapter = null
        entityClassRecyclerView = null
        entityClassRecyclerAdapter = null
        selQuestionOptionList = null
    }

    companion object {

        val DIFF_CALLBACK_SEL_QUESTION_OPTION = object
            : DiffUtil.ItemCallback<SelQuestionOption>() {
            override fun areItemsTheSame(oldItem: SelQuestionOption,
                                         newItem: SelQuestionOption): Boolean {
                return oldItem.selQuestionOptionUid == newItem.selQuestionOptionUid
            }

            override fun areContentsTheSame(oldItem: SelQuestionOption,
                                            newItem: SelQuestionOption): Boolean {
                return oldItem === newItem
            }
        }
    }
}
