package com.ustadmobile.port.android.view

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

import com.toughra.ustadmobile.R

/**
 * View to hold items for the TocListView. This wraps the view provided by the adapter and puts an
 * expand/collapse arrow on the side if required.
 */
class TocItemView : LinearLayout, View.OnClickListener {

    private var itemViewLayout: FrameLayout? = null

    /**
     * Set whether or not the node is expanded. This will only change the expand/collapse icon.
     *
     * @param expanded True to show that this item has been expanded (show up arrow), false otherwise
     * (down arrow)
     */
    var isExpanded = false
        set(expanded) {
            field = expanded
            mDropDownImageView!!.setImageResource(if (expanded)
                R.drawable.ic_arrow_drop_up_black_24dp
            else
                R.drawable.ic_arrow_drop_down_black_24dp)
        }

    /**
     * Set whether or not expand/collapse arrows should be shown on this view
     *
     * @param expandable true to show expand/collapse arrows, false otherwise
     */
    var isExpandable = false
        set(expandable) {
            mDropDownImageView!!.visibility = if (expandable) View.VISIBLE else View.INVISIBLE
        }

    private var mDropDownImageView: ImageView? = null

    private var clickExpandListener: OnClickExpandListener? = null

    interface OnClickExpandListener {

        fun onClickExpand(itemView: TocItemView)

    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.item_toclistview_itemcontainer, this)
        itemViewLayout = findViewById(R.id.item_toclistview_itemcontainer_frame_layout)
        mDropDownImageView = findViewById(R.id.item_toclistview_expand_img)
        mDropDownImageView!!.setOnClickListener(this)
    }

    /**
     * Set the view to be shown
     *
     * @param view
     */
    fun setItemView(view: View) {
        itemViewLayout!!.removeAllViews()
        itemViewLayout!!.addView(view)
    }

    fun setOnClickExpandListener(clickExpandListener: OnClickExpandListener) {
        this.clickExpandListener = clickExpandListener
    }

    override fun onClick(view: View) {
        if (clickExpandListener != null)
            clickExpandListener!!.onClickExpand(this)
    }
}
