package com.ustadmobile.port.android.view;

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseBlockBinding
import com.toughra.ustadmobile.databinding.ItemCourseBlockEditBinding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import java.util.*

class CourseBlockRecyclerAdapter(var oneToManyEditListener: OneToManyJoinEditListener<CourseBlockWithEntity>?,
                                 var presenter: ClazzEdit2Presenter?,
                                 recylerView: RecyclerView?): ListAdapter<CourseBlockWithEntity,
        CourseBlockRecyclerAdapter.CourseBlockViewHolder>(DIFF_CALLBACK_BLOCK), ItemTouchHelperAdapter, OnStartDragListener{

    private var itemTouchHelper: ItemTouchHelper

    init{
        val callback = ReorderHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recylerView)
    }

    class CourseBlockViewHolder(val binding: ItemCourseBlockEditBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseBlockViewHolder {
        val viewHolder = CourseBlockViewHolder(ItemCourseBlockEditBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.mPresenter = presenter
        viewHolder.binding.oneToManyJoinListener = oneToManyEditListener
        viewHolder.binding.itemCourseBlockReorder.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onStartDrag(viewHolder)
            }
            return@setOnTouchListener true
        }

        return viewHolder
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        viewHolder?.let {
            itemTouchHelper.startDrag(it)
        }
    }

    override fun onBindViewHolder(holder: CourseBlockViewHolder, position: Int) {
        holder.binding.block = getItem(position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        oneToManyEditListener = null
        presenter = null
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(currentList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {

    }

    class ReorderHelperCallback(val adapter : ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags( dragFlags, swipeFlags)
        }

        override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onItemMove(source.absoluteAdapterPosition,
                    target.absoluteAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder.itemView.alpha = 1.0f
        }


    }

    companion object {

        val DIFF_CALLBACK_BLOCK: DiffUtil.ItemCallback<CourseBlockWithEntity> = object : DiffUtil.ItemCallback<CourseBlockWithEntity>() {
            override fun areItemsTheSame(oldItem: CourseBlockWithEntity, newItem: CourseBlockWithEntity): Boolean {
                return oldItem.cbUid == newItem.cbUid
            }

            override fun areContentsTheSame(oldItem: CourseBlockWithEntity, newItem: CourseBlockWithEntity): Boolean {
                return oldItem == newItem
            }
        }


    }


}
