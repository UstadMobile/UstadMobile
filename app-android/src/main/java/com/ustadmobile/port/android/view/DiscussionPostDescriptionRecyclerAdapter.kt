package com.ustadmobile.port.android.view

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionPostDetailBinding
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import me.saket.bettermovementmethod.BetterLinkMovementMethod
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

    var discussionTopic: DiscussionPostWithDetails? = null
        set(value){
            if(field == value) return
            field = value
            viewHolder?.itemBinding?.discussionPost = value
            viewHolder?.itemBinding?.itemDiscussionPostDetailLatestMessage?.text =
                value?.discussionPostMessage
            addMovement(viewHolder?.itemBinding?.itemDiscussionPostDetailLatestMessage)

        }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionPostViewHolder {

        viewHolder = DiscussionPostViewHolder(
            ItemDiscussionPostDetailBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))


        return viewHolder as DiscussionPostViewHolder
    }

    override fun onBindViewHolder(holder: DiscussionPostViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }




    private fun addMovement(textView: TextView?){

        if(textView == null){
            return
        }
        textView.linksClickable = true

        textView.movementMethod =
            BetterLinkMovementMethod.newInstance().apply {
                this.setOnLinkClickListener(onClickListener)
                this.setOnLinkLongClickListener(onLongClickListener)

            }

        Linkify.addLinks(textView, Linkify.ALL)

    }

    val onClickListener: BetterLinkMovementMethod.OnLinkClickListener =
        BetterLinkMovementMethod.OnLinkClickListener{
                _,url ->
            systemImpl.handleClickLink(url, context)

            true
        }

    private val onLongClickListener: BetterLinkMovementMethod.OnLinkLongClickListener =
        BetterLinkMovementMethod.OnLinkLongClickListener{
                _,_ ->
            true
        }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }
}