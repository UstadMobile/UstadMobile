package com.ustadmobile.port.android.umeditor

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager


/**
 * Custom view pager which enables webview to be scrolled horizontally
 */
class UmEditorViewPager : ViewPager {

    private var pagingEnabled = true

    /**
     * Constructor called when creating new instance using context
     * @param context Application context
     */
    constructor(context: Context) : super(context) {}

    /**
     * Constructor used when creating new instance using attrs
     * @param context Application context
     * @param attrs attrs values
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.pagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        try {
            return super.onInterceptTouchEvent(event) && this.pagingEnabled
        } catch (exception: IllegalArgumentException) {
            exception.printStackTrace()
        }

        return false

    }

    /**
     * Set paging control flag
     * @param enabled True when paging is enabled otherwise false.
     * i.e this flag will be changes when you scroll horizontally
     */
    fun setPagingEnabled(enabled: Boolean) {
        this.pagingEnabled = enabled
    }

    override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        return this.pagingEnabled && super.canScroll(v, checkV, dx, x, y)
    }

}
