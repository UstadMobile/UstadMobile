package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.toughra.ustadmobile.databinding.ActivitySelquestionsetEditBinding
import com.toughra.ustadmobile.databinding.ItemSelquestionBinding
import com.ustadmobile.core.controller.SelQuestionSetEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionSetEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract


fun ComponentActivity.prepareSelQuestionSetEditCall(callback: (List<SelQuestionSet>?) -> Unit) =
        prepareCall(CrudEditActivityResultContract(this, SelQuestionSet::class.java,
        SelQuestionSetEditFragment::class.java,
        SelQuestionSet::selQuestionSetUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<SelQuestionSet>>
        .launchSelQuestionSetEdit(schedule: SelQuestionSet?,
                                  extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.DB, extraArgs))
}

interface SelQuestionSetEditActivityEventHandler {
    fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?)
    fun onClickNewSelQuestion()
    fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions)
}

class SelQuestionSetEditFragment : UstadEditFragment<SelQuestionSet>(), SelQuestionSetEditView,
    SelQuestionSetEditActivityEventHandler {

    private var mBinding: ActivitySelquestionsetEditBinding? = null

    private var mPresenter: SelQuestionSetEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SelQuestionSet>?
        get() = mPresenter

    class SelQuestionRecyclerAdapter(val activityEventHandler: SelQuestionSetEditActivityEventHandler,
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
                return viewHolder
            }

            override fun onBindViewHolder(holder: SelQuestionViewHolder, position: Int) {
                holder.binding.selQuestion = getItem(position)
            }
        }

    override var selQuestionList: DoorLiveData<List<SelQuestionAndOptions>>? = null
        get() = field
        set(value) {
            field?.removeObserver(selQuestionObserver)
            field = value
            value?.observe(this, selQuestionObserver)
        }

    private var selQuestionRecyclerAdapter: SelQuestionRecyclerAdapter? = null

    private var selQuestionRecyclerView: RecyclerView? = null

    private val selQuestionObserver = Observer<List<SelQuestionAndOptions>?> {
        t -> selQuestionRecyclerAdapter?.submitList(t)
    }

    override fun onClickEditSelQuestion(selQuestion: SelQuestionAndOptions?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(selQuestion, R.id.selquestionandoptions_edit_dest, SelQuestionAndOptions::class.java)
    }

    override fun onClickNewSelQuestion() =
            onClickEditSelQuestion(null)


    override fun handleRemoveSelQuestion(selQuestion: SelQuestionAndOptions) {
        mPresenter?.handleRemoveSelQuestion(selQuestion)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View
        mBinding = ActivitySelquestionsetEditBinding.inflate(inflater, container, false)
                .also{
                    rootView = it.root
                    it.activityEventHandler = this
                }

        selQuestionRecyclerView = rootView.findViewById(R.id.activity_selquestion_recycleradapter)
        selQuestionRecyclerAdapter = SelQuestionRecyclerAdapter(this, null)
        selQuestionRecyclerView?.adapter = selQuestionRecyclerAdapter
        selQuestionRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = SelQuestionSetEditPresenter(this, arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        //After the presenter is created
        selQuestionRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.sel_question_set)

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SelQuestionAndOptions::class.java) {
            val selQuestionAndOptions = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSelQuestion(selQuestionAndOptions)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply {
            mPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: SelQuestionSet? = null
        get() = field
        set(value) {
            field = value
            mBinding?.selquestionset = value
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
