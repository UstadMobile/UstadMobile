package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentScopedGrantDetailBinding
import com.toughra.ustadmobile.databinding.ItemBitmaskflagBinding
import com.ustadmobile.core.controller.ScopedGrantDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toBitmaskFlagList
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


interface ScopedGrantDetailFragmentEventHandler {

}

class ScopedGrantDetailFragment: UstadDetailFragment<ScopedGrantWithName>(

), ScopedGrantDetailView, ScopedGrantDetailFragmentEventHandler {

    class BitmaskFlagViewHolder(
        val itemBinding: ItemBitmaskflagBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    class BitmaskFlagViewRecyclerAdapter : ListAdapter<BitmaskFlag, BitmaskFlagViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmaskFlagViewHolder {
            return BitmaskFlagViewHolder(ItemBitmaskflagBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }

        override fun onBindViewHolder(holder: BitmaskFlagViewHolder, position: Int) {
            holder.itemBinding.bitmaskFlag = getItem(position)
        }
    }

    private var mRecyclerAdapter: BitmaskFlagViewRecyclerAdapter? = null

    private var mBinding: FragmentScopedGrantDetailBinding? = null

    private var mPresenter: ScopedGrantDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View
        mRecyclerAdapter = BitmaskFlagViewRecyclerAdapter()
        mBinding = FragmentScopedGrantDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentScopedGrantDetailRv.adapter = mRecyclerAdapter
            it.fragmentScopedGrantDetailRv.layoutManager = LinearLayoutManager(requireContext())
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = ScopedGrantDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentScopedGrantDetailRv?.adapter = null
        mRecyclerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ScopedGrantWithName? = null
        set(value) {
            field = value
            mRecyclerAdapter?.submitList(entity?.toBitmaskFlagList())
            ustadFragmentTitle = entity?.name ?: context?.getString(R.string.permission)
            mBinding?.scopedGrant = value
        }


    companion object {

        val DIFF_CALLBACK : DiffUtil.ItemCallback<BitmaskFlag> = object: DiffUtil.ItemCallback<BitmaskFlag>() {
            override fun areItemsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.flagVal == newItem.flagVal
            }

            override fun areContentsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.enabled == newItem.enabled
            }
        }

    }
}