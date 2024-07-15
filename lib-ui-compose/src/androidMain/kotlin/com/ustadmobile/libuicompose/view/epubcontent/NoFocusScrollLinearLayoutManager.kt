package com.ustadmobile.libuicompose.view.epubcontent

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * This provides a RecyclerView LinearLayout that will not scroll when the focus is changed. This
 * is needed for EpubContent so that it can change the focus to make sure views don't
 * jump around as WebViews load and their height changes.
 */
class NoFocusScrollLinearLayoutManager(
    context: Context
): LinearLayoutManager(context) {

    override fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ) = true

}