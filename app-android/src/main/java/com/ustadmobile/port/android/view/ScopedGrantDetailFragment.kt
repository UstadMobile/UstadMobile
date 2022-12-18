package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toBitmaskFlagList
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.viewmodel.ScopedGrantDetailUiState
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.port.android.util.compose.messageIdResource
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantDetailScreen(
    uiState: ScopedGrantDetailUiState,
    onItemClick: (BitmaskFlag) -> Unit = {}
){

    Column (
        modifier = Modifier
            .fillMaxSize()
    ){
        uiState.bitmaskList.forEach { bitmask ->
            ListItem(
                modifier = Modifier.clickable {
                    onItemClick(bitmask)
                },
                text = {
                    Text(messageIdResource(id = bitmask.messageId))
                }
            )
        }
    }

}

@Composable
@Preview
fun ScopedGrantDetailScreenPreview(){
    ScopedGrantDetailScreen(
        uiState = ScopedGrantDetailUiState(
            bitmaskList = listOf(
                BitmaskFlag(
                    messageId = MessageID.incident_id,
                    flagVal = 0
                ),
                BitmaskFlag(
                    messageId = MessageID.message,
                    flagVal = 0
                )
            )
        )
    )
}