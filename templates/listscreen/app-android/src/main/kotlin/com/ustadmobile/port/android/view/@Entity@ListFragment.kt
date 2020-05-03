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
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter

class @Entity@ListFragment(): UstadListViewFragment<@Entity@, @DisplayEntity@>(),
        @Entity@ListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: @Entity@ListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in @DisplayEntity@>?
        get() = mPresenter

    class @Entity@ListViewHolder(val itemBinding: Item@Entity_ViewBinding_VariableName@ListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    class @Entity@ListRecyclerAdapter(var presenter: @Entity@ListPresenter?)
        : SelectablePagedListAdapter<@DisplayEntity@, @Entity@ListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): @Entity@ListViewHolder {
            val itemBinding = Item@Entity_ViewBinding_VariableName@ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return @Entity@ListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: @Entity@ListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.@Entity_VariableName@ = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = @Entity@ListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = @Entity@ListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.@Entity_LowerCase@))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.@Entity_LowerCase@)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.@Entity_LowerCase@_edit_dest, @Entity@::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
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
    }
}