package com.ustadmobile.port.android.umeditor

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Class which handles [RecyclerView] item spacing on a list/grid.
 * Created by Kileha3
 */

class UmGridSpacingItemDecoration
/**
 * Creating new instance of UmGridSpacingItemDecoration with:-
 * @param spanCount number of columns to be displayed
 * @param spacing spacing between items
 * @param includeEdge True if spacing include edges otherwise edges are excluded.
 */
(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}
