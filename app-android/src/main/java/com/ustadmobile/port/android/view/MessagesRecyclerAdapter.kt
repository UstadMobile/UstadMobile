package com.ustadmobile.port.android.view

import android.content.Context
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMessageListBinding
import com.ustadmobile.core.controller.ChatDetailPresenter
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import me.saket.bettermovementmethod.BetterLinkMovementMethod


class MessagesRecyclerAdapter(val loggedInPersonUid: Long)
    : SelectablePagedListAdapter<MessageWithPerson,
        MessagesRecyclerAdapter.MessageWithPersonViewHolder>(DIFF_CALLBACK_COMMENTS) {

    var presenter: ChatDetailPresenter? = null

    class MessageWithPersonViewHolder(val binding: ItemMessageListBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageWithPersonViewHolder {
        val vh = MessageWithPersonViewHolder(ItemMessageListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))

        addMovement(vh.binding.itemCommentsListLine2Text, parent.context)

        return vh
    }


    override fun onBindViewHolder(holder: MessageWithPersonViewHolder, position: Int) {
        if(itemCount > 0 ) {

            val message = getItem(position)
            holder.binding.loggedInPersonUid = loggedInPersonUid
            holder.itemView.tag = message?.messageUid
            holder.binding.message = message

            holder.binding.itemCommentsListLine2Text.text = message?.messageText
            addMovement(holder.binding.itemCommentsListLine2Text, holder.itemView.context)

        }
    }


    private fun addMovement(textView: TextView, context: Context){

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
        presenter?.handleClickLink(url)

        true
    }

    val onLongClickListener: BetterLinkMovementMethod.OnLinkLongClickListener =
        BetterLinkMovementMethod.OnLinkLongClickListener{
            _,_ ->
        true
    }

    companion object{

        val DIFF_CALLBACK_COMMENTS =
                object : DiffUtil.ItemCallback<MessageWithPerson>() {
                    override fun areItemsTheSame(oldItem: MessageWithPerson,
                                                 newItem: MessageWithPerson): Boolean {
                        return oldItem.messageUid == newItem.messageUid
                    }

                    override fun areContentsTheSame(oldItem: MessageWithPerson,
                                                    newItem: MessageWithPerson): Boolean {
                        return oldItem.messageSenderPersonUid == newItem.messageSenderPersonUid &&
                            oldItem.messageText == newItem.messageText &&
                            oldItem.messagePerson?.firstNames == newItem.messagePerson?.firstNames &&
                            oldItem.messagePerson?.lastName == newItem.messagePerson?.lastName
                    }
                }

    }
}