package com.ustadmobile.port.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Px;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.OverScroller;

/**
 * Created by mike on 11/5/17.
 */

public class EpubWebView extends WebView implements GestureDetector.OnGestureListener {

    private OverScroller mScroller;

    private GestureDetectorCompat mGestureDetector;

    private boolean isScrolling = false;

    /**
     * Taken from ViewPager
     */
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    private static final int MAX_SETTLE_DURATION = 600; // ms

    private MotionEvent scrollEvt1;

    private MotionEvent scrollEvt2;

    private float mDownY;


    public EpubWebView(Context context) {
        super(context);
        initScrolling(context);
    }

    public EpubWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScrolling(context);
    }

    public EpubWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScrolling(context);
    }

    @TargetApi(21)
    public EpubWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initScrolling(context);
    }

    private void initScrolling(Context context) {
        mScroller = new OverScroller(context);
        mGestureDetector = new GestureDetectorCompat(context, this);
        setVerticalScrollBarEnabled(false);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        mGestureDetector.onTouchEvent(event);

        boolean endOfScroll = false;

        if(isScrolling && event.getAction() == MotionEvent.ACTION_UP) {
            endOfScroll = true;
            isScrolling = false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownY = event.getY();
        }

        if(isScrolling) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP :
                    event.setLocation(event.getX(), mDownY);
                    break;
            }
        }



        Log.i("EpubWebView", "isScrolling: " + isScrolling + " end of scroll: " + endOfScroll);

        if(endOfScroll) {
            Log.i("EpubWebView", "End of scroll: no fling");
            return handleFlick(scrollEvt1, scrollEvt2, 1, 0);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        mScroller.abortAnimation();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isScrolling = true;
        scrollEvt1 = e1;
        scrollEvt2 = e2;
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i("EpubWebView", "onFling");
        return handleFlick(e1, e2, velocityX, velocityY);
    }

    private boolean handleFlick(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int swipeDeltaX = Math.round(Math.abs(e1.getX() - e2.getX()));
        int swipeThresholdPx =  (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MIN_DISTANCE_FOR_FLING, getContext().getResources().getDisplayMetrics());
        boolean isFlick = swipeDeltaX > swipeThresholdPx;
        Log.i("EpubWebView", "handleFlick Delta X = " + swipeDeltaX + " threshold px = "
                + swipeThresholdPx + " isFlick: " + isFlick);
        int currentXPos = getScrollX();
        int currentChunk = currentXPos  / getWidth();// better to use the position that it would have been at
        int increment = 0;
        if(isFlick) {
            if(e1.getX() > e2.getX()) {
                increment = 1;
            }else {
                increment = -1;
            }
        }

        int xDestination = (currentChunk + increment) * getWidth();
        int distanceX = xDestination - currentXPos;

        //As per Android's own ViewPager.java see line 677
        // https://android.googlesource.com/platform/frameworks/support/+/jb-dev/v4/java/android/support/v4/view/ViewPager.java
        int duration = Math.min(
                Math.round(Math.abs(1000 * Math.abs(distanceX / velocityX))) * 4,
                MAX_SETTLE_DURATION);
        Log.i("EpubWebView", ": scroll duration " + duration + ", distanceX = " + distanceX + ", velocity = " + velocityX);

        mScroller.startScroll(getScrollX(), 0, distanceX, 0, duration);
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }
}
