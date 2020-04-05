package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.Item@Entity_ViewBinding_VariableName@ListItemBinding
import com.ustadmobile.core.controller.@Entity@ListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@Entity@ListView
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.lib.db.entities.@DisplayEntity@
import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.port.android.view.util.getDataItemViewHolder


class @Entity@ListFragment(): UstadListViewFragment<@Entity@, @DisplayEntity@>(),
        @Entity@ListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: @Entity@ListPresenter? = null

    private var dbRepo: UmAppDatabase? = null

    class @Entity@ListRecyclerAdapter(var presenter: @Entity@ListPresenter?, newItemVisible: Boolean,
                                      onClickNewItem: View.OnClickListener, createNewText: String)
        : PagedListAdapterWithNewItem<@DisplayEntity@>(DIFF_CALLBACK, newItemVisible, onClickNewItem, createNewText) {

        class @Entity@ListViewHolder(val itemBinding: Item@Entity_ViewBinding_VariableName@ListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEMVIEWTYPE_NEW) {
                return super.onCreateViewHolder(parent, viewType)
            }else {
                val itemBinding = Item@Entity_ViewBinding_VariableName@ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return@Entity@ListViewHolder(itemBinding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder.getDataItemViewHolder()
            if(itemHolder is @Entity@ListViewHolder) {
                itemHolder.itemBinding.@Entity_VariableName@ = getItem(position)
                itemHolder.itemBinding.presenter = presenter
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = @Entity@ListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.@Entity_LowerCase@))
        mRecyclerViewAdapter = @Entity@ListRecyclerAdapter(mPresenter, false, this,
            createNewText)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onClick(view: View?) {
        activity?.prepare@Entity@EditCall {
            if(it != null) {
                finishWithResult(it)
            }
        }?.launch@Entity@Edit(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.@Entity@Dao")

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<@DisplayEntity@> = object
            : DiffUtil.ItemCallback<@DisplayEntity@>() {
            override fun areItemsTheSame(oldItem: @DisplayEntity@,
                                         newItem: @DisplayEntity@): Boolean {
                TODO("e.g. insert primary keys here return oldItem.@Entity_VariableName@ == newItem.@Entity_VariableName@")
            }

            override fun areContentsTheSame(oldItem: @DisplayEntity@,
                                            newItem: @DisplayEntity@): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?) : @Entity@ListFragment {
            return @Entity@ListFragment().apply {
                arguments = bundle
            }
        }
    }
}