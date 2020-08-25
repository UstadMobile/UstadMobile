package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemRoleListItemBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.RoleEditPresenter
import com.ustadmobile.core.controller.RoleListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.RoleListView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


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
            holder.itemBinding.bitMaskFlags = RoleEditPresenter.FLAGS_AVAILABLE
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
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        mPresenter = RoleListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this,  di, viewLifecycleOwner)
        mDataBinding?.presenter = mPresenter
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_a_new,
                        requireContext().getString(R.string.role)),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        mDataRecyclerViewAdapter = RoleListRecyclerAdapter(mPresenter)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.role)
    }

    override fun onClick(v: View?) {

        if (v?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
        else {
            super.onClick(v)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
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
    }
}