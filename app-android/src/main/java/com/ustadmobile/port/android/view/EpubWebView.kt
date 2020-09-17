package com.ustadmobile.port.android.view

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat

/**
 * This modified WebView is designed to work with the Javascript and CSS that implement pagination
 * using CSS columns. It will disable vertical scroll, and ensure that flicks are from column to
 * column.
 *
 * This is not going to be used for children's story EPUBs, but will be reintroduced later to handle
 * longer content.
 */
class EpubWebView : WebView, GestureDetector.OnGestureListener {

    private lateinit var mScroller: OverScroller

    private lateinit var mGestureDetector: GestureDetectorCompat

    private var isScrolling = false

    private var scrollEvt1: MotionEvent? = null

    private var scrollEvt2: MotionEvent? = null

    private var mDownY: Float = 0.toFloat()

    private var columnIndex: Int = 0


    constructor(context: Context) : super(context) {
        initScrolling(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initScrolling(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initScrolling(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initScrolling(context)
    }

    private fun initScrolling(context: Context) {
        mScroller = OverScroller(context)
        mGestureDetector = GestureDetectorCompat(context, this)

        isVerticalScrollBarEnabled = false
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }
        mGestureDetector.onTouchEvent(event)

        var endOfScroll = false

        if (isScrolling && event.action == MotionEvent.ACTION_UP) {
            endOfScroll = true
            isScrolling = false
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            mDownY = event.y
        }

        if (isScrolling) {
            when (event.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> event.setLocation(event.x, mDownY)
            }
        }



        Log.i("EpubWebView", "isScrolling: $isScrolling end of scroll: $endOfScroll")

        if (endOfScroll) {
            Log.i("EpubWebView", "End of scroll: no fling")
            return handleFlick(scrollEvt1!!, scrollEvt2!!, 1f, 0f)
        }


        return super.onTouchEvent(event)
    }

    override fun onDown(motionEvent: MotionEvent): Boolean {
        mScroller.abortAnimation()
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent) {

    }

    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        isScrolling = true
        scrollEvt1 = e1
        scrollEvt2 = e2
        return false
    }

    override fun onLongPress(motionEvent: MotionEvent) {}

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.i("EpubWebView", "onFling")
        return handleFlick(e1, e2, velocityX, velocityY)
    }

    private fun handleFlick(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val swipeDeltaX = Math.round(Math.abs(e1.x - e2.x))
        val swipeThresholdPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MIN_DISTANCE_FOR_FLING.toFloat(), context.resources.displayMetrics).toInt()
        val isFlick = swipeDeltaX > swipeThresholdPx
        Log.i("EpubWebView", "handleFlick Delta X = " + swipeDeltaX + " threshold px = "
                + swipeThresholdPx + " isFlick: " + isFlick)
        val currentXPos = scrollX
        //val currentChunk = currentXPos / width// better to use the position that it would have been at
        var increment = 0
        if (isFlick) {
            if (e1.x > e2.x) {
                increment = 1
            } else {
                increment = -1
            }
        }

        val xDestination = (columnIndex + increment) * width
        val distanceX = xDestination - currentXPos

        //As per Android's own ViewPager.java see line 677
        // https://android.googlesource.com/platform/frameworks/support/+/jb-dev/v4/java/android/support/v4/view/ViewPager.java
        val duration = Math.min(
                Math.round(Math.abs(1000 * Math.abs(distanceX / velocityX))) * 4,
                MAX_SETTLE_DURATION)
        Log.i("EpubWebView", ": scroll duration $duration, distanceX = $distanceX, velocity = $velocityX")

        mScroller.startScroll(scrollX, 0, distanceX, 0, duration)
        columnIndex += increment
        ViewCompat.postInvalidateOnAnimation(this)
        return true
    }

    companion object {

        /**
         * Taken from ViewPager
         */
        private val MIN_DISTANCE_FOR_FLING = 25 // dips

        private val MAX_SETTLE_DURATION = 600 // ms
    }
}
