package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityRoleEditBinding
import com.toughra.ustadmobile.databinding.ItemBitmaskBinding
import com.ustadmobile.core.controller.RoleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RoleEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract


fun ComponentActivity.prepareRoleEditCall(callback: (List<Role>?) -> Unit) =
        prepareCall(CrudEditActivityResultContract(this, Role::class.java,
        RoleEditFragment::class.java, Role::roleUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<Role>>
        .launchRoleEdit(schedule: Role?, extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}


class RoleEditFragment : UstadEditFragment<Role>(), RoleEditView, Observer<List<BitmaskFlag>?> {

    private var mBinding: ActivityRoleEditBinding? = null

    private var mRecyclerViewAdapter: BitmaskRecyclerViewAdapter? = null

    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: RoleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Role>?
        get() = mPresenter

    override var entity: Role? = null
        get() = field
        set(value) {
            field = value
            mBinding?.role = value

        }

    override var permissionList: DoorLiveData<List<BitmaskFlag>>? = null
        get() = field
        set(value) {
            field?.removeObserver(this)
            field = value
            value?.observe(this, this)
        }

    override fun onChanged(t: List<BitmaskFlag>?) {
        mRecyclerViewAdapter?.submitList(t)
    }

    class BitmaskViewHolder(val itemBinding: ItemBitmaskBinding): RecyclerView.ViewHolder(itemBinding.root)

    class BitmaskRecyclerViewAdapter: ListAdapter<BitmaskFlag, BitmaskViewHolder>(DIFFUTIL_BITMASKFLAG) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmaskViewHolder {
            return BitmaskViewHolder(ItemBitmaskBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
        }

        override fun onBindViewHolder(holder: BitmaskViewHolder, position: Int) {
            holder.itemBinding.bitmaskFlag = getItem(position)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View
        mBinding = ActivityRoleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.roleEditPermissionBitmaskEditRv
        }

        //mBinding?.role = entity

        mPresenter = RoleEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        mRecyclerViewAdapter = BitmaskRecyclerViewAdapter()
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())



        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
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

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.new_role)
    }


    companion object {

        val DIFFUTIL_BITMASKFLAG = object: DiffUtil.ItemCallback<BitmaskFlag>() {
            override fun areItemsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.flagVal == newItem.flagVal
            }

            override fun areContentsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem == newItem
            }
        }
    }

}
