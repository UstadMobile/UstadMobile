package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemRoleListItemBinding
import com.ustadmobile.core.controller.RoleListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RoleListView
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class RoleListFragment(): UstadListViewFragment<Role, Role>(),
        RoleListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    override val listPresenter: UstadListPresenter<*, in Role>?
        get() = mPresenter

    private var mPresenter: RoleListPresenter? = null


    class RoleListViewHolder(val itemBinding: ItemRoleListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class RoleListRecyclerAdapter(var presenter: RoleListPresenter?)
        : SelectablePagedListAdapter<Role, RoleListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : RoleListViewHolder {

            val itemBinding = ItemRoleListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return RoleListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: RoleListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.role = item
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
        mPresenter = RoleListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                requireContext().getString(R.string.create_new,
                        requireContext().getString(R.string.role)))
        mDataRecyclerViewAdapter = RoleListRecyclerAdapter(mPresenter)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.role)
    }

    override fun onClick(view: View?) {
        //TODOne: Uncomment when Edit stuff in there
        activity?.prepareRoleEditCall {
            if(it != null) {
                finishWithResult(it)
            }
        }?.launchRoleEdit(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(view: AdapterView<*>?,
                                           messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.roleDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Role> = object
            : DiffUtil.ItemCallback<Role>() {
            override fun areItemsTheSame(oldItem: Role,
                                         newItem: Role): Boolean {
                return oldItem.roleUid == newItem.roleUid
            }

            override fun areContentsTheSame(oldItem: Role,
                                            newItem: Role): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?) : RoleListFragment {
            return RoleListFragment().apply {
                arguments = bundle
            }
        }
    }
}