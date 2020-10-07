package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemInviteWithLinkBinding
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class InviteWithLinkRecyclerViewAdapter(
        var itemListener: InviteWithLinkHandler?, var mPresenter: PersonListPresenter?)
    : SingleItemRecyclerViewAdapter<InviteWithLinkRecyclerViewAdapter.NewCommentViewHolder>() {

    var tableId: Int = 0
    var entityName: String? = null
    var code: String? = null

    class NewCommentViewHolder(var itemBinding: ItemInviteWithLinkBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewCommentViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemInviteWithLinkBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.inviteHandler = itemListener
                    it.mPresenter = mPresenter
                    it.code = code
                    it.entityName = entityName
                    it.tableId = tableId
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

}