package com.ustadmobile.port.android.view

import android.accounts.AccountManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionPostDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import org.kodein.di.DI
import org.kodein.di.instance

class DiscussionPostDescriptionRecyclerAdapter(
    di: DI,
    val context: Any
): SingleItemRecyclerViewAdapter<
        DiscussionPostDescriptionRecyclerAdapter.DiscussionPostViewHolder>(true) {

    class DiscussionPostViewHolder(var itemBinding: ItemDiscussionPostDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    private var viewHolder: DiscussionPostViewHolder? = null

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    private val accountManager: UstadAccountManager by di.instance()

    var discussionTopic: DiscussionPostWithDetails? = null
        set(value){
            if(field == value) return
            field = value
            viewHolder?.itemBinding?.discussionPost = value
            viewHolder?.itemBinding?.itemDiscussionPostDetailLatestMessage?.text =
                value?.discussionPostMessage

            val listener = BetterLinkMovementLinkClickListener(systemImpl, accountManager, context)
            listener.addMovement(viewHolder?.itemBinding?.itemDiscussionPostDetailLatestMessage)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionPostViewHolder {

        viewHolder = DiscussionPostViewHolder(
            ItemDiscussionPostDetailBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))


        return viewHolder as DiscussionPostViewHolder
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }
}