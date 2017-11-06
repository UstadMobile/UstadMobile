package com.ustadmobile.port.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
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

    private boolean isFling = false;


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
        mGestureDetector.onTouchEvent(event);
//        if(isFling)
//            return true;

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
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
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        mScroller.forceFinished(true);

        mScroller.startScroll(0, 0, 100, 0);
//        mScroller.fling(0, 0, 10, 0, 0, computeHorizontalScrollExtent(), 0, computeVerticalScrollExtent());
        ViewCompat.postInvalidateOnAnimation(this);

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        /*
        isFling = true;
        int width = computeHorizontalScrollRange();

        int vX = Math.round(v);
        int vY = Math.round(v1);


        mScroller.forceFinished(true);
        mScroller.fling(getScrollX(), getScrollY(), vX, vY, 0, 0, getWidth(), getHeight());

        ViewCompat.postInvalidateOnAnimation(this);
        return true;
        */
        return true;
    }
}
