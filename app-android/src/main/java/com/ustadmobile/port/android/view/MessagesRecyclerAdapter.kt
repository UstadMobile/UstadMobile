package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMessageListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.MessagesPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.MessageRead
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance


class MessagesRecyclerAdapter(
    val loggedInPersonUid: Long,
    private var presenterScope: CoroutineScope?,
    private var presenter: MessagesPresenter?,
    di: DI,
    context: Any
): SelectablePagedListAdapter<MessageWithPerson,
        MessagesRecyclerAdapter.MessageWithPersonViewHolder>(DIFF_CALLBACK_COMMENTS) {


    private val systemImpl: UstadMobileSystemImpl by di.instance()

    private val accountManager: UstadAccountManager by di.instance()

    private var context: Any? = context

    class MessageWithPersonViewHolder(val binding: ItemMessageListBinding,
                                      var messageReadJob: Job? = null)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageWithPersonViewHolder {
        return MessageWithPersonViewHolder(ItemMessageListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: MessageWithPersonViewHolder, position: Int) {
        holder.messageReadJob?.cancel()

        val message = getItem(position)
        holder.binding.loggedInPersonUid = loggedInPersonUid
        holder.itemView.tag = message?.messageUid
        holder.binding.message = message

        holder.binding.itemCommentsListLine2Text.text = message?.messageText
        val contextVal = context ?: return
        val listener = BetterLinkMovementLinkClickListener(systemImpl, accountManager, contextVal)
        listener.addMovement(holder.binding.itemCommentsListLine2Text)

        //if message is unread
        holder.takeIf {
            message != null && message.messageRead == null
        }?.messageReadJob = presenterScope?.launch {
            delay(1000)
            //GO and run the insert
            presenter?.updateMessageRead(
                MessageRead(loggedInPersonUid, message?.messageUid?:0,
                    message?.messageEntityUid?:0L)
            )

            holder.messageReadJob = null
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        context = null
        presenter = null
        presenterScope = null
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