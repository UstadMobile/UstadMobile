package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.paging.DataSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkspaceDetailBinding
import com.toughra.ustadmobile.databinding.ItemWorkspaceBinding
import com.ustadmobile.core.controller.WorkspaceDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.port.android.util.ext.*


interface WorkspaceDetailFragmentEventHandler {

}

class WorkspaceDetailFragment: UstadDetailFragment<WorkSpace>(), WorkspaceDetailView, WorkspaceDetailFragmentEventHandler {

    class WorkspaceViewHolder(val mBinding: ItemWorkspaceBinding): RecyclerView.ViewHolder(mBinding.root)

    class WorkspaceRecyclerViewAdapter : ListAdapter<WorkSpace, WorkspaceViewHolder>(DIFFUTIL_WORKSPACE) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
            return WorkspaceViewHolder(ItemWorkspaceBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }

        override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
            holder.mBinding.workspace = getItem(position)
        }
    }

    private var mBinding: FragmentWorkspaceDetailBinding? = null

    private var mPresenter: WorkspaceDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var workspaceRecyclerViewAdapter: WorkspaceRecyclerViewAdapter? = null


    override var entity: WorkSpace? = null
        get() = field
        set(value) {
            field = value

            workspaceRecyclerViewAdapter?.submitList(if(value != null) {
                listOf(value)
            }else {
                listOf()
            })
        }


    override var workspaceTermsList: DataSource.Factory<Int, WorkspaceTerms>? = null
        get() = field
        set(value) {
            field = value

        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkspaceDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        workspaceRecyclerViewAdapter = WorkspaceRecyclerViewAdapter()
        mBinding?.fragmentListRecyclerview?.apply {
            adapter = workspaceRecyclerViewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = WorkspaceDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }


    companion object {
        val DIFFUTIL_WORKSPACE = object: DiffUtil.ItemCallback<WorkSpace>() {
            override fun areItemsTheSame(oldItem: WorkSpace, newItem: WorkSpace): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: WorkSpace, newItem: WorkSpace): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.registrationAllowed == newItem.registrationAllowed &&
                        oldItem.guestLogin == newItem.guestLogin
            }
        }
    }

}