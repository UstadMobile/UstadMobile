package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseBlockEditBinding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.view.ItemTouchHelperListener
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

class CourseBlockRecyclerAdapter(var presenter: ClazzEdit2Presenter?,
                                 recylerView: RecyclerView?): ListAdapter<CourseBlockWithEntity,
        CourseBlockRecyclerAdapter.CourseBlockViewHolder>(DIFF_CALLBACK_BLOCK), OnStartDragListener{

    private var itemTouchHelper: ItemTouchHelper

    init{

        val callback = ReorderHelperCallback(presenter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recylerView)
    }

    class CourseBlockViewHolder(val binding: ItemCourseBlockEditBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseBlockViewHolder {
        val viewHolder = CourseBlockViewHolder(ItemCourseBlockEditBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.mPresenter = presenter
        viewHolder.binding.oneToManyJoinListener = presenter
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
        presenter = null
    }

    class ReorderHelperCallback(val presenter : ItemTouchHelperListener?) : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags,0)
        }

        override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            presenter?.onItemMove(source.absoluteAdapterPosition,
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
            val block = (viewHolder as CourseBlockViewHolder).binding.block
            viewHolder.itemView.alpha = if(block?.cbHidden == true) 0.5f else 1f
        }

    }

    companion object {

        val DIFF_CALLBACK_BLOCK: DiffUtil.ItemCallback<CourseBlockWithEntity> = object : DiffUtil.ItemCallback<CourseBlockWithEntity>() {
            override fun areItemsTheSame(oldItem: CourseBlockWithEntity, newItem: CourseBlockWithEntity): Boolean {
                return oldItem.cbUid == newItem.cbUid
            }

            /*
             * added due to two-way databind conflict when updating the list for indenting or hiding
             */
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: CourseBlockWithEntity, newItem: CourseBlockWithEntity): Boolean {
                return oldItem === newItem
            }
        }


    }


}
