package com.ustadmobile.port.android.view

import android.text.Layout
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMessageListBinding
import com.ustadmobile.core.controller.ChatDetailPresenter
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


abstract class TextViewLinkHandler : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
//        if (event.action != MotionEvent.ACTION_UP) return super.onTouchEvent(widget, buffer, event)
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout: Layout = widget.layout
        val line: Int = layout.getLineForVertical(y)
        val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
        val link: Array<URLSpan> = buffer.getSpans(off, off, URLSpan::class.java)
        if (link.size != 0) {
            onLinkClick(link[0].getURL())
        }
        return true
    }

    abstract fun onLinkClick(url: String?)
}

class MessagesRecyclerAdapter(val loggedInPersonUid: Long)
    : SelectablePagedListAdapter<MessageWithPerson,
        MessagesRecyclerAdapter.MessageWithPersonViewHolder>(DIFF_CALLBACK_COMMENTS) {

    var presenter: ChatDetailPresenter? = null

    class MessageWithPersonViewHolder(val binding: ItemMessageListBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageWithPersonViewHolder {
        return MessageWithPersonViewHolder(ItemMessageListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MessageWithPersonViewHolder, position: Int) {
        if(itemCount > 0 ) {
            holder.binding.message = getItem(position)
            holder.binding.loggedInPersonUid = loggedInPersonUid
            holder.itemView.tag = getItem(position)?.messageUid
            holder.binding.itemCommentsListLine2Text.movementMethod = object : TextViewLinkHandler() {
                override fun onLinkClick(url: String?) {
                    presenter?.handleClickLink(url?:"")
                }
            }

        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
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
                                oldItem.messageTimestamp == newItem.messageTimestamp
                    }
                }

    }
}