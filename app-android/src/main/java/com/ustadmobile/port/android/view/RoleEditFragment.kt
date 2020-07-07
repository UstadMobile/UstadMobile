package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentRoleEditBinding
import com.toughra.ustadmobile.databinding.ItemBitmaskBinding
import com.ustadmobile.core.controller.RoleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RoleEditView
import com.ustadmobile.lib.db.entities.Role


class RoleEditFragment : UstadEditFragment<Role>(), RoleEditView {

    private var mBinding: FragmentRoleEditBinding? = null

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

    override var permissionList: LiveData<List<BitmaskFlag>>? = null
        get() = field
        set(value) {
            field?.removeObserver(roleObserver)
            field = value
            value?.observe(this, roleObserver)
        }

    private val roleObserver = Observer<List<BitmaskFlag>?>{
        t -> mRecyclerViewAdapter?.submitList(t)
    }


    class BitmaskViewHolder(val itemBinding: ItemBitmaskBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class BitmaskRecyclerViewAdapter()
        : ListAdapter<BitmaskFlag, BitmaskViewHolder>(DIFFUTIL_BITMASKFLAG) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmaskViewHolder {
            val viewHolder =  BitmaskViewHolder(ItemBitmaskBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false))
            return viewHolder
        }

        override fun onBindViewHolder(holder: BitmaskViewHolder, position: Int) {
            holder.itemBinding.bitmaskFlag = getItem(position)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View

        mBinding = FragmentRoleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.roleEditPermissionBitmaskEditRv
        }

        mPresenter = RoleEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mRecyclerViewAdapter = BitmaskRecyclerViewAdapter()
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())



        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.role)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        permissionList = null
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


    companion object {

        val DIFFUTIL_BITMASKFLAG = object: DiffUtil.ItemCallback<BitmaskFlag>() {
            override fun areItemsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.flagVal == newItem.flagVal
            }

            override fun areContentsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                //to ensure that the two-way binding saves the data to the latest reference of the object
                return oldItem === newItem
            }
        }
    }

}
