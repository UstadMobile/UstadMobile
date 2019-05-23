package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import java.util.*

/**
 * Table of Contents (ToC) list view component. This is used to show a hierarchical table of
 * contents where the user can expand or collapse items. It supports an unlimited number of levels.
 * An expand/collapse arrow appears to the end of the text. If the user taps on the arrow, the
 * group is expanded and the onItemClick method is not called. If the user taps on the view for the
 * item itself, the onItemClick event is fired and the view is not expanded.
 *
 * To create a TocListView a TocListViewAdapter needs to be provided.
 */

class TocListView : LinearLayout, View.OnClickListener, TocItemView.OnClickExpandListener {

    private var adapter: TocListViewAdapter? = null

    private val viewToNodeMap = HashMap<TocItemView, Any>()

    private var onItemClickListener: OnItemClickListener? = null

    /**
     * Adapter class used to provide items for the TocListView. This currently does not support
     * changing the content.
     */
    abstract class TocListViewAdapter {

        /**
         * Must return the root object, which should provide a List of child objects when the
         * getChildren method is called.
         *
         * @return Object representing the root node.
         */
        abstract val root: Any

        /**
         * Must return a list of child objects for the given node.
         *
         * @param node Object representing the node to list children for.
         *
         * @return A list of objects representing the children for this node. If this object has no
         * children returning null or an empty list are both acceptable.
         */
        abstract fun getChildren(node: Any?): List<*>?

        /**
         * Return a view representing this node
         *
         * @param node Object for which the view is being created
         * @param recycleView A leftover view that is to be re-used. Might be null
         * @param depth The depth of this child from the root node
         *
         * @return A View object representing the given node
         */
        abstract fun getNodeView(node: Any, recycleView: View?, depth: Int): View

        /**
         * Return the number of children for a specific node.
         *
         * @param node The node being queried
         *
         * @return The number of children for this node.
         */
        abstract fun getNumChildren(node: Any?): Int

    }

    /**
     * Listener for when a node itself is clicked.
     */
    interface OnItemClickListener {

        /**
         * Fired when the view for a given node has been clicked
         *
         * @param item The node object that was clicked
         * @param view The view object that was clicked
         */
        fun onClick(item: Any?, view: View)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    /**
     * Set the adapter to use for this view
     *
     * @param adapter TocListViewAdapter to be used
     */
    fun setAdapter(adapter: TocListViewAdapter) {
        this.adapter = adapter
        addChildren(adapter.root, 0, 0)
    }

    /**
     * Adds children to the view when a node is expanded.
     *
     * @param node Node to add from
     * @param startPos Position to start adding from (inclusive)
     * @param depth depth of the node to add from
     */
    protected fun addChildren(node: Any?, startPos: Int, depth: Int) {
        val children = adapter!!.getChildren(node)
        var child: Any
        for (i in children!!.indices) {
            val itemView = TocItemView(context)
            child = children[i]!!

            val childView = adapter!!.getNodeView(child, null, depth)
            itemView.setItemView(childView)
            itemView.setOnClickExpandListener(this)
            itemView.isExpandable = adapter!!.getNumChildren(child) > 0
            itemView.setOnClickListener(this)

            addView(itemView, startPos + i)
            viewToNodeMap[itemView] = child
        }
    }

    /**
     * Remove children (and their descendants) from the view when a node is collapsed
     *
     * @param node The node to remove children
     * @param startPos The position of the node from which to remove from
     */
    protected fun removeChildren(node: Any?, startPos: Int) {
        val children = adapter!!.getChildren(node)
        var itemView: TocItemView
        for (i in children!!.indices) {
            itemView = getChildAt(startPos) as TocItemView
            if (itemView.isExpanded) {
                //remove it's child views
                removeChildren(children[i], startPos + 1)
            }

            removeView(itemView)
            viewToNodeMap.remove(itemView)
        }
    }

    override fun onClick(view: View) {
        val node = viewToNodeMap[view]
        if (onItemClickListener != null) {
            onItemClickListener!!.onClick(node, view)
        }
    }

    override fun onClickExpand(itemView: TocItemView) {
        val node = viewToNodeMap[itemView]
        if (adapter!!.getNumChildren(node) > 0) {
            val startPos = indexOfChild(itemView) + 1
            if (!itemView.isExpanded) {
                addChildren(node, startPos, 0)
                itemView.isExpanded = true
            } else {
                removeChildren(node, startPos)
                itemView.isExpanded = false
            }
        }
    }

    /**
     * Set the listener for when an item is clicked
     *
     * @param onItemClickListener Listener for when an item itself is clicked.
     */
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}
