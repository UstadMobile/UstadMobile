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
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SelQuestionSetListView
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class SelQuestionSetListFragment(): UstadListViewFragment<SelQuestionSet,
        SELQuestionSetWithNumQuestions>(), SelQuestionSetListView,
        MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SelQuestionSetListPresenter? = null

    class SelQuestionSetListViewHolder(val itemBinding: ItemSelquestionsetListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class SelQuestionSetListRecyclerAdapter(var presenter: SelQuestionSetListPresenter?)
        : SelectablePagedListAdapter<SELQuestionSetWithNumQuestions, SelQuestionSetListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : SelQuestionSetListViewHolder {
            val itemBinding = ItemSelquestionsetListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return SelQuestionSetListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: SelQuestionSetListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.selQuestionSet = item
            holder.itemBinding.presenter = presenter
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
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
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
         requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.sel_question_set)))
        mDataRecyclerViewAdapter = SelQuestionSetListRecyclerAdapter(mPresenter)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.sel_question_set)
    }

    override fun onClick(view: View?) {
        mPresenter?.handleClickCreateNewFab()
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

    }

    override val listPresenter: UstadListPresenter<*, in SELQuestionSetWithNumQuestions>?
        get() = mPresenter
}