package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.toughra.ustadmobile.databinding.ItemDiscussionTopicSimpleDraggableBinding
import com.ustadmobile.core.controller.CourseDiscussionEditPresenter
import com.ustadmobile.lib.db.entities.DiscussionTopic

class DiscussionTopicDraggableViewHolder(val binding: ItemDiscussionTopicSimpleDraggableBinding)
    : DragDropSwipeAdapter.ViewHolder(binding.root)

class DiscussionTopicDraggableRecyclerAdapter(var presenter: CourseDiscussionEditPresenter?,
                                              recyclerView: DragDropSwipeRecyclerView?)
    : DragDropSwipeAdapter<DiscussionTopic,
        DiscussionTopicDraggableViewHolder>(),
    OnItemDragListener<DiscussionTopic> {


    private var viewHolder: DiscussionTopicDraggableViewHolder? = null

    init{
        recyclerView?.dragListener = this
    }

    override fun getViewHolder(itemView: View): DiscussionTopicDraggableViewHolder {
        // should never be called
        return viewHolder!!
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionTopicDraggableViewHolder {
        viewHolder = DiscussionTopicDraggableViewHolder(
            ItemDiscussionTopicSimpleDraggableBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ).apply {
            binding.mPresenter = presenter
        }
        return viewHolder as DiscussionTopicDraggableViewHolder
    }

    override fun getViewToTouchToStartDraggingItem(
        item: DiscussionTopic,
        viewHolder: DiscussionTopicDraggableViewHolder,
        position: Int
    ): View {
        return viewHolder.binding.itemDiscussionTopicSimpleDraggableDraggableIv
    }

    override fun onBindViewHolder(
        item: DiscussionTopic,
        viewHolder: DiscussionTopicDraggableViewHolder,
        position: Int
    ) {
        viewHolder.binding.discussionTopic = item
    }

    override fun canBeSwiped(
        item: DiscussionTopic,
        viewHolder: DiscussionTopicDraggableViewHolder,
        position: Int
    ): Boolean {
        return false
    }

    override fun onItemDragged(
        previousPosition: Int,
        newPosition: Int,
        item: DiscussionTopic
    ) {

    }

    override fun onItemDropped(
        initialPosition: Int,
        finalPosition: Int,
        item: DiscussionTopic
    ) {
        if(initialPosition != finalPosition){
            presenter?.onItemMove(initialPosition, finalPosition)
        }
    }

    override fun onDragStarted(item: DiscussionTopic, viewHolder: DiscussionTopicDraggableViewHolder) {
        viewHolder.itemView.alpha = 0.5f
    }

    override fun onDragFinished(item: DiscussionTopic, viewHolder: DiscussionTopicDraggableViewHolder) {
        viewHolder.itemView.alpha = 1f
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
        viewHolder = null
    }



}