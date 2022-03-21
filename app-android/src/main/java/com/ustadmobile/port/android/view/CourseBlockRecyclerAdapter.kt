package com.ustadmobile.port.android.view;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.toughra.ustadmobile.databinding.ItemCourseBlockEditBinding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

class CourseBlockRecyclerAdapter(var presenter: ClazzEdit2Presenter?,
                                 recylerView: DragDropSwipeRecyclerView?):
    DragDropSwipeAdapter<CourseBlockWithEntity, CourseBlockRecyclerAdapter.CourseBlockViewHolder>(),
    OnItemDragListener<CourseBlockWithEntity> {

    private var viewHolder: CourseBlockViewHolder? = null

    init{
        recylerView?.dragListener = this
    }

    class CourseBlockViewHolder(val binding: ItemCourseBlockEditBinding): DragDropSwipeAdapter.ViewHolder(binding.root)

    override fun getViewHolder(itemView: View): CourseBlockViewHolder {
        // should never be called
        return viewHolder!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseBlockViewHolder {
        viewHolder = CourseBlockViewHolder(ItemCourseBlockEditBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)).apply {
            binding.mPresenter = presenter
            binding.oneToManyJoinListener = presenter
        }
        return viewHolder as CourseBlockViewHolder
    }

    override fun getViewToTouchToStartDraggingItem(
        item: CourseBlockWithEntity,
        viewHolder: CourseBlockViewHolder,
        position: Int
    ): View {
        return viewHolder.binding.itemCourseBlockReorder
    }

    override fun onBindViewHolder(
        item: CourseBlockWithEntity,
        viewHolder: CourseBlockViewHolder,
        position: Int
    ) {
        viewHolder.binding.block = item
    }

    override fun canBeSwiped(
        item: CourseBlockWithEntity,
        viewHolder: CourseBlockViewHolder,
        position: Int
    ): Boolean {
        return false
    }

    override fun onItemDragged(
        previousPosition: Int,
        newPosition: Int,
        item: CourseBlockWithEntity
    ) {

    }

    override fun onItemDropped(
        initialPosition: Int,
        finalPosition: Int,
        item: CourseBlockWithEntity
    ) {
        if(initialPosition != finalPosition){
            presenter?.onItemMove(initialPosition, finalPosition)
        }
    }

    override fun onDragStarted(item: CourseBlockWithEntity, viewHolder: CourseBlockViewHolder) {
        viewHolder.itemView.alpha = 0.5f
    }

    override fun onDragFinished(item: CourseBlockWithEntity, viewHolder: CourseBlockViewHolder) {
        viewHolder.itemView.alpha = if(item.cbHidden) 0.5f else 1f
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
        viewHolder = null
    }

}
