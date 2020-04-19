package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSelquestionsetListItemBinding
import com.ustadmobile.core.controller.SelQuestionSetListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionSetListView
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.view.util.PagedListAdapterWithNewItem
import com.ustadmobile.port.android.view.util.getDataItemViewHolder


class SelQuestionSetListFragment(): UstadListViewFragment<SelQuestionSet,
        SELQuestionSetWithNumQuestions>(), SelQuestionSetListView,
        MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SelQuestionSetListPresenter? = null

    private var dbRepo: UmAppDatabase? = null

    class SelQuestionSetListRecyclerAdapter(var presenter: SelQuestionSetListPresenter?,
                                            newItemVisible: Boolean,
                                            onClickNewItem: View.OnClickListener,
                                            createNewText: String)
        : PagedListAdapterWithNewItem<SELQuestionSetWithNumQuestions>(DIFF_CALLBACK,
            newItemVisible, onClickNewItem, createNewText) {

        class SelQuestionSetListViewHolder(val itemBinding: ItemSelquestionsetListItemBinding)
            : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : RecyclerView.ViewHolder {
            if(viewType == ITEMVIEWTYPE_NEW) {
                return super.onCreateViewHolder(parent, viewType)
            }else {
                val itemBinding = ItemSelquestionsetListItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)
                return SelQuestionSetListViewHolder(itemBinding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder.getDataItemViewHolder()
            if(itemHolder is SelQuestionSetListViewHolder) {
                itemHolder.itemBinding.selQuestionSet = getItem(position)
                itemHolder.itemBinding.presenter = presenter
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = SelQuestionSetListPresenter(requireContext(),
                UMAndroidUtil.bundleToMap(arguments), this, this,
                UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.sel_question_set))
        mRecyclerViewAdapter = SelQuestionSetListRecyclerAdapter(mPresenter,
                false, this, createNewText)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.sel_question_set)
    }

    override fun onClick(view: View?) {
        activity?.prepareSelQuestionSetEditCall {
            if(it != null) {
                finishWithResult(it)
            }
        }?.launchSelQuestionSetEdit(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(
            view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.selQuestionSetDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions> = object
            : DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions>() {
            override fun areItemsTheSame(oldItem: SELQuestionSetWithNumQuestions,
                                         newItem: SELQuestionSetWithNumQuestions): Boolean {
                return oldItem.selQuestionSetUid == newItem.selQuestionSetUid
            }

            override fun areContentsTheSame(oldItem: SELQuestionSetWithNumQuestions,
                                            newItem: SELQuestionSetWithNumQuestions): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?) : SelQuestionSetListFragment {
            return SelQuestionSetListFragment().apply {
                arguments = bundle
            }
        }
    }

    override val listPresenter: UstadListPresenter<*, in SELQuestionSetWithNumQuestions>?
        get() = mPresenter
}