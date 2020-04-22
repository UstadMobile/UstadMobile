package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivitySelquestionandoptionsEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionoptionBinding
import com.ustadmobile.core.controller.SelQuestionAndOptionsEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionAndOptionsEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract


fun ComponentActivity.prepareSelQuestionAndOptionsEditCall(
        callback: (List<SelQuestionAndOptions>?) -> Unit) = prepareCall(
            CrudEditActivityResultContract(this, SelQuestionAndOptions::class.java,
                                            SelQuestionAndOptionsEditFragment::class.java,
                    {it.selQuestion.selQuestionUid})
) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<SelQuestionAndOptions>>
        .launchSelQuestionAndOptionsEdit(schedule: SelQuestionAndOptions?,
                                         extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface SelQuestionAndOptionsEditActivityEventHandler {
    fun onClickNewQuestionOption()
    fun updateQuestionOptionText(questionOption: SelQuestionOption)
    fun handleRemoveSelQuestionOption(questionOption: SelQuestionOption)
}

class SelQuestionAndOptionsEditFragment : UstadEditFragment<SelQuestionAndOptions>(),
        SelQuestionAndOptionsEditView, SelQuestionAndOptionsEditActivityEventHandler {

    private var mBinding: ActivitySelquestionandoptionsEditBinding? = null

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


    override var selQuestionOptionList: DoorLiveData<List<SelQuestionOption>>? = null
        get() = field
        set(value) {
            field?.removeObserver(entityClassObserver)
            field = value
            value?.observe(this, entityClassObserver)
        }

    class EntityClassRecyclerAdapter(
            val activityEventHandler: SelQuestionAndOptionsEditActivityEventHandler,
            var presenter: SelQuestionAndOptionsEditPresenter?)
        : ListAdapter<SelQuestionOption,
            EntityClassRecyclerAdapter.EntityClassViewHolder>(DIFF_CALLBACK_SEL_QUESTION_OPTION) {

        class EntityClassViewHolder(val binding: ItemSelquestionoptionBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityClassViewHolder {
            val viewHolder = EntityClassViewHolder(ItemSelquestionoptionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivityEventHandler = activityEventHandler
            return viewHolder
        }


        override fun onBindViewHolder(holder: EntityClassViewHolder, position: Int) {
            val option = getItem(position)
            holder.binding.selQuestionOption = option

            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    presenter?.updateQuestionOptionTitle(option, s.toString())
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //Nothing
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //Nothing
                }
            }
            holder.binding.itemSelQuestionOptionText.addTextChangedListener(textWatcher)
        }
    }

    override var entity: SelQuestionAndOptions? = null
        get() = field
        set(value) {
            field = value
            mBinding?.selquestionandoptions = value
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

    override fun onClickNewQuestionOption() {
        mPresenter?.addNewBlankQuestionOption()
    }

    override fun updateQuestionOptionText(questionOption: SelQuestionOption) {
        //TODO Check, resolved already?
    }

    override fun handleRemoveSelQuestionOption(questionOption: SelQuestionOption) {
        //TODO
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView : View
        mBinding = ActivitySelquestionandoptionsEditBinding.inflate(inflater, container,
                false).also{
                    rootView = it.root
                    it.activityEventHandler = this
                }

        mBinding?.multichoicevalue = SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE

        entityClassRecyclerView = rootView.findViewById(R.id.activity_selquestionandoptions_edit_rv)
        entityClassRecyclerAdapter = EntityClassRecyclerAdapter(this, null)
        entityClassRecyclerView?.adapter = entityClassRecyclerAdapter
        entityClassRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = SelQuestionAndOptionsEditPresenter(requireContext(), arguments.toStringMap(),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))

        //After the presenter is created
        entityClassRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.question)

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SelQuestionOption::class.java) {
            val selQuestionOption = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSelQuestionOption(selQuestionOption)
        }
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

        val DIFF_CALLBACK_SEL_QUESTION_OPTION = object : DiffUtil.ItemCallback<SelQuestionOption>() {
            override fun areItemsTheSame(oldItem: SelQuestionOption, newItem: SelQuestionOption): Boolean {
                return oldItem.selQuestionOptionUid == newItem.selQuestionOptionUid
            }

            override fun areContentsTheSame(oldItem: SelQuestionOption, newItem: SelQuestionOption): Boolean {
                return oldItem == newItem
            }
        }
    }





}
