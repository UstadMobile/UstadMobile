package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;

/**
 * Table of Contents (ToC) list view component. This is used to show a hierarchical table of
 * contents where the user can expand or collapse items. It supports an unlimited number of levels.
 * An expand/collapse arrow appears to the end of the text. If the user taps on the arrow, the
 * group is expanded and the onItemClick method is not called. If the user taps on the view for the
 * item itself, the onItemClick event is fired and the view is not expanded.
 *
 * To create a TocListView a TocListViewAdapter needs to be provided.
 */

public class TocListView extends LinearLayout implements View.OnClickListener,
        TocItemView.OnClickExpandListener {

    private TocListViewAdapter adapter;

    /**
     * Adapter class used to provide items for the TocListView. This currently does not support
     * changing the content.
     */
    public static abstract class TocListViewAdapter {

        /**
         * Must return the root object, which should provide a List of child objects when the
         * getChildren method is called.
         *
         * @return Object representing the root node.
         */
        public abstract Object getRoot();

        /**
         * Must return a list of child objects for the given node.
         *
         * @param node Object representing the node to list children for.
         *
         * @return A list of objects representing the children for this node. If this object has no
         * children returning null or an empty list are both acceptable.
         */
        public abstract List getChildren(Object node);

        /**
         * Return a view representing this node
         *
         * @param node Object for which the view is being created
         * @param recycleView A leftover view that is to be re-used. Might be null
         * @param depth The depth of this child from the root node
         *
         * @return A View object representing the given node
         */
        public abstract View getNodeView(Object node, View recycleView, int depth);

        /**
         * Return the number of children for a specific node.
         *
         * @param node The node being queried
         *
         * @return The number of children for this node.
         */
        public abstract int getNumChildren(Object node);

    }

    private HashMap<TocItemView, Object> viewToNodeMap = new HashMap<>();

    /**
     * Listener for when a node itself is clicked.
     */
    public interface OnItemClickListener {

        /**
         * Fired when the view for a given node has been clicked
         *
         * @param item The node object that was clicked
         * @param view The view object that was clicked
         */
        void onClick(Object item, View view);
    }

    private OnItemClickListener onItemClickListener;

    public TocListView(Context context) {
        super(context);
    }

    public TocListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TocListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Set the adapter to use for this view
     *
     * @param adapter TocListViewAdapter to be used
     */
    public void setAdapter(TocListViewAdapter adapter) {
        this.adapter = adapter;
        addChildren(adapter.getRoot(), 0, 0);
    }

    /**
     * Adds children to the view when a node is expanded.
     *
     * @param node Node to add from
     * @param startPos Position to start adding from (inclusive)
     * @param depth depth of the node to add from
     */
    protected void addChildren(Object node, int startPos, int depth) {
        List children = adapter.getChildren(node);
        Object child;
        for(int i = 0; i < children.size(); i++) {
            TocItemView itemView = new TocItemView(getContext());
            child = children.get(i);

            View childView = adapter.getNodeView(child, null, depth);
            itemView.setItemView(childView);
            itemView.setOnClickExpandListener(this);
            itemView.setExpandable(adapter.getNumChildren(child) > 0);
            itemView.setOnClickListener(this);

            addView(itemView, startPos + i);
            viewToNodeMap.put(itemView, child);
        }
    }

    /**
     * Remove children (and their descendants) from the view when a node is collapsed
     *
     * @param node The node to remove children
     * @param startPos The position of the node from which to remove from
     */
    protected void removeChildren(Object node, int startPos) {
        List children = adapter.getChildren(node);
        TocItemView itemView;
        for(int i = 0; i < children.size(); i++) {
            itemView = (TocItemView)getChildAt(startPos);
            if(itemView.isExpanded()) {
                //remove it's child views
                removeChildren(children.get(i), startPos + 1);
            }

            removeView(itemView);
            viewToNodeMap.remove(itemView);
        }
    }

    @Override
    public void onClick(View view) {
        Object node = viewToNodeMap.get(view);
        if(onItemClickListener != null) {
            onItemClickListener.onClick(node, view);
        }
    }

    @Override
    public void onClickExpand(TocItemView itemView) {
        Object node = viewToNodeMap.get(itemView);
        if(adapter.getNumChildren(node) > 0) {
            int startPos = indexOfChild(itemView)+1;
            if(!itemView.isExpanded()) {
                addChildren(node, startPos, 0);
                itemView.setExpanded(true);
            }else {
                removeChildren(node, startPos);
                itemView.setExpanded(false);
            }
        }
    }

    /**
     * Set the listener for when an item is clicked
     *
     * @param onItemClickListener Listener for when an item itself is clicked.
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
