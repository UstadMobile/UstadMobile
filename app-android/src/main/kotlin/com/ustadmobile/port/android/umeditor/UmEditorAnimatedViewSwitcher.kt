package com.ustadmobile.port.android.umeditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import androidx.annotation.NonNull
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Class which handles all animated view switching, we have BottomSheets and keyboard
 * to bring seamless experience these views has to be switched accordingly.
 *
 * **Operational Flow:**
 *
 *
 * Use [UmEditorAnimatedViewSwitcher.with]
 * to set content editor activity instance and listener for listening all animation
 * closing event,.
 *
 * Use [)][UmEditorAnimatedViewSwitcher.setViews] to set all animated views and root view
 * which will be used to listen for the keyboard events.
 *
 * Use [UmEditorAnimatedViewSwitcher.animateView] to send request to open a certain
 * animated view depending on the view key passed into it.
 *
 * Use [UmEditorAnimatedViewSwitcher.closeAnimatedView] to send request to close a
 * certain animated view depending on the view key passed into it.
 *
 * Use [UmEditorAnimatedViewSwitcher.closeActivity] to handle activity closing task
 * which will close all the activity views before shutting down.
 *
 *
 * @author kileha3
 */
class UmEditorAnimatedViewSwitcher {

    private var formattingBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null

    private var mediaSourceBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null

    private var contentOptionsBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>? = null

    private var editorView: WebView? = null

    private var closedListener: OnAnimatedViewsClosedListener? = null

    private var gestureDetector: GestureDetector? = null

    private var isKeyboardActive: Boolean = false

    private var openKeyboard = false

    private var openFormatPanel = false

    private var openContentPanel = false

    private var openMediaPanel = false

    private var activity: Activity? = null

    private var rootView: View? = null

    private var editorActivated = false


    /**
     * Gesture listener to listen for long press an clicks on the webview
     * for the implicitly keyboard open
     */
    private val onGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            requestFocusOpenKeyboard()
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            requestFocusOpenKeyboard()
        }
    }


    private val isFormattingBottomSheetExpanded: Boolean
        get() = formattingBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED

    private val isContentOptionsBottomSheetExpanded: Boolean
        get() = contentOptionsBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED

    private val isMediaSourceBottomSheetExpanded: Boolean
        get() = mediaSourceBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED

    /**
     * Set activity instance to be used and listener to listen when all animated views are closed.
     * @param activity Activity under watcher
     * @param listener Listener to be set for listening animated view closing events.
     * @return UmEditorAnimatedViewSwitcher instance.
     */
    fun with(activity: Activity,
             listener: OnAnimatedViewsClosedListener): UmEditorAnimatedViewSwitcher {
        this.activity = activity
        this.closedListener = listener
        return this
    }


    /**
     * Set animated views to be monitored from the root activity
     * @param rootView Root view of the activity
     * @param insertContentSheet content option bottom sheet view
     * @param formatSheet Formats types bottom sheet view
     * @param mediaSheet Media sources bottom sheet view
     * @return UmEditorAnimatedViewSwitcher instance.
     */
    fun setViews(rootView: View, editorView: WebView,
                 insertContentSheet: BottomSheetBehavior<NestedScrollView>,
                 formatSheet: BottomSheetBehavior<NestedScrollView>,
                 mediaSheet: BottomSheetBehavior<NestedScrollView>): UmEditorAnimatedViewSwitcher {
        this.contentOptionsBottomSheetBehavior = insertContentSheet
        this.formattingBottomSheetBehavior = formatSheet
        this.mediaSourceBottomSheetBehavior = mediaSheet
        this.rootView = rootView
        this.editorView = editorView
        initializeSwitcher()
        return this
    }

    /**
     * Initialize all views and callback listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeSwitcher() {
        gestureDetector = GestureDetector(activity, onGestureListener)
        editorView!!.setOnTouchListener { v, event -> gestureDetector!!.onTouchEvent(event) }
        contentOptionsBottomSheetBehavior!!.setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            when {
                                openKeyboard -> handleSoftKeyboard(true)
                                openFormatPanel -> setFormattingBottomSheetBehavior(true)
                                openMediaPanel -> setMediaSourceBottomSheetBehavior(true)
                            }
                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            openContentPanel = false
                        }
                    }

                    override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {

                    }
                })

        formattingBottomSheetBehavior!!.setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            when {
                                openKeyboard -> handleSoftKeyboard(true)
                                openContentPanel -> setContentOptionBottomSheetBehavior(true)
                                openMediaPanel -> setMediaSourceBottomSheetBehavior(true)
                            }
                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            openFormatPanel = false
                        }
                    }

                    override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {

                    }
                })

        mediaSourceBottomSheetBehavior!!.setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            when {
                                openKeyboard -> handleSoftKeyboard(true)
                                openFormatPanel -> setFormattingBottomSheetBehavior(true)
                                openContentPanel -> setContentOptionBottomSheetBehavior(true)
                            }
                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            openMediaPanel = false
                        }
                    }

                    override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {

                    }
                })

        rootView!!.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView!!.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView!!.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardActive = keypadHeight > screenHeight * 0.15
            if (isKeyboardActive) {
                openKeyboard = false
                when {
                    isFormattingBottomSheetExpanded -> setFormattingBottomSheetBehavior(false)
                    isContentOptionsBottomSheetExpanded -> setContentOptionBottomSheetBehavior(false)
                    isMediaSourceBottomSheetExpanded -> setMediaSourceBottomSheetBehavior(false)
                }
            } else {
                when {
                    openFormatPanel -> setFormattingBottomSheetBehavior(true)
                    openMediaPanel -> setMediaSourceBottomSheetBehavior(true)
                    openContentPanel -> setContentOptionBottomSheetBehavior(true)
                }
            }
        }
    }

    /**
     * Animate specific animated view
     * @param currentKey Key of the view to be animated (opened)
     */
    fun animateView(currentKey: String) {

        when (currentKey) {
            ANIMATED_FORMATTING_PANEL -> if (isFormattingBottomSheetExpanded) {
                openKeyboard = true
                setFormattingBottomSheetBehavior(false)
            } else {
                when {
                    isKeyboardActive -> {
                        openFormatPanel = true
                        handleSoftKeyboard(true)
                    }
                    isContentOptionsBottomSheetExpanded -> {
                        openFormatPanel = true
                        setContentOptionBottomSheetBehavior(false)
                    }
                    isMediaSourceBottomSheetExpanded -> {
                        openFormatPanel = true
                        setMediaSourceBottomSheetBehavior(false)
                    }
                    else -> setFormattingBottomSheetBehavior(true)
                }
            }

            ANIMATED_CONTENT_OPTION_PANEL ->

                if (isContentOptionsBottomSheetExpanded) {
                    openKeyboard = true
                    setContentOptionBottomSheetBehavior(false)
                } else {
                    when {
                        isKeyboardActive -> {
                            openContentPanel = true
                            handleSoftKeyboard(true)
                        }
                        isFormattingBottomSheetExpanded -> {
                            openContentPanel = true
                            setFormattingBottomSheetBehavior(false)
                        }
                        isMediaSourceBottomSheetExpanded -> {
                            openContentPanel = true
                            setMediaSourceBottomSheetBehavior(false)
                        }
                        else -> setContentOptionBottomSheetBehavior(true)
                    }
                }
            ANIMATED_MEDIA_TYPE_PANEL ->

                if (isMediaSourceBottomSheetExpanded) {
                    openKeyboard = true
                    setMediaSourceBottomSheetBehavior(false)
                } else {
                    when {
                        isKeyboardActive -> {
                            openMediaPanel = true
                            handleSoftKeyboard(true)
                        }
                        isFormattingBottomSheetExpanded -> {
                            openContentPanel = true
                            setFormattingBottomSheetBehavior(false)
                        }
                        isContentOptionsBottomSheetExpanded -> {
                            openMediaPanel = true
                            setContentOptionBottomSheetBehavior(false)
                        }
                        else -> setMediaSourceBottomSheetBehavior(true)
                    }
                }
            ANIMATED_SOFT_KEYBOARD_PANEL -> if (isFormattingBottomSheetExpanded) {
                openKeyboard = true
                setFormattingBottomSheetBehavior(false)
            } else if (isContentOptionsBottomSheetExpanded) {
                openKeyboard = true
                setContentOptionBottomSheetBehavior(false)
            } else if (isMediaSourceBottomSheetExpanded) {
                openKeyboard = true
                setMediaSourceBottomSheetBehavior(false)
            } else {
                if (!isKeyboardActive) {
                    handleSoftKeyboard(true)
                }
            }
        }
    }

    /**
     * Set flag to indicate editing mode of the main editor
     * @param editorActivated True when editing mode is ON otherwise Editing mode will be OFF
     */
    fun setEditorActivated(editorActivated: Boolean) {
        this.editorActivated = editorActivated
    }

    /**
     * Close specific animated view.
     * @param viewKey Key of the animated view to be closed.
     */
    fun closeAnimatedView(viewKey: String) {
        when (viewKey) {
            ANIMATED_FORMATTING_PANEL -> setFormattingBottomSheetBehavior(false)

            ANIMATED_CONTENT_OPTION_PANEL -> setContentOptionBottomSheetBehavior(false)

            ANIMATED_MEDIA_TYPE_PANEL -> setMediaSourceBottomSheetBehavior(false)

            ANIMATED_SOFT_KEYBOARD_PANEL -> handleSoftKeyboard(false)
        }
    }

    /**
     * IMplicitly open and close soft keyboard.
     * @param show Open when true is passed otherwise close it.
     */
    private fun handleSoftKeyboard(show: Boolean) {
        if (show) {
            val imm = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        } else {
            val imm = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity!!.currentFocus
            if (view == null) {
                view = View(activity)
            }
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    /**
     * Close all animated view before destroying the activity.
     */
    fun closeActivity(close: Boolean) {

        if (isMediaSourceBottomSheetExpanded) {
            setMediaSourceBottomSheetBehavior(false)
        }

        if (isFormattingBottomSheetExpanded) {
            setFormattingBottomSheetBehavior(false)
        }

        if (isContentOptionsBottomSheetExpanded) {
            setContentOptionBottomSheetBehavior(false)
        }

        handleSoftKeyboard(false)

        android.os.Handler().postDelayed({
            if (!isMediaSourceBottomSheetExpanded && !isFormattingBottomSheetExpanded
                    && !isContentOptionsBottomSheetExpanded && !isKeyboardActive) {

                if (closedListener != null) {
                    editorActivated = false
                    closedListener!!.onAllAnimatedViewsClosed(close)
                }
            }
        }, MAX_SOFT_KEYBOARD_DELAY)
    }

    private fun setFormattingBottomSheetBehavior(expanded: Boolean) {
        formattingBottomSheetBehavior!!.state = if (expanded)
            BottomSheetBehavior.STATE_EXPANDED
        else
            BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setContentOptionBottomSheetBehavior(expanded: Boolean) {
        contentOptionsBottomSheetBehavior!!.state = if (expanded)
            BottomSheetBehavior.STATE_EXPANDED
        else
            BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setMediaSourceBottomSheetBehavior(expanded: Boolean) {
        mediaSourceBottomSheetBehavior!!.state = if (expanded)
            BottomSheetBehavior.STATE_EXPANDED
        else
            BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun requestFocusOpenKeyboard() {
        closedListener!!.onFocusRequested()
        editorView!!.postDelayed({
            if (editorActivated) {
                if (!isKeyboardActive) {
                    handleSoftKeyboard(true)
                }
            }
        }, MAX_SOFT_KEYBOARD_DELAY)

    }

    /**
     * Interface which listen for the closing event of all views
     * and webview focus on click, tap and press events.
     */
    interface OnAnimatedViewsClosedListener {
        /**
         * Invoked when all animated views are closed
         * @param finish flag to indicate whether action will result to finishing
         * the activity or not.
         */
        fun onAllAnimatedViewsClosed(finish: Boolean)

        /**
         * Invoked when WebView requests a focus.
         */
        fun onFocusRequested()
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var viewSwitcher: UmEditorAnimatedViewSwitcher? = null

        /**
         * Key which represent the formatting options BottomSheet
         */
        const val ANIMATED_FORMATTING_PANEL = "formatting_panel"

        /**
         * Key which represents the content option BottomSheet
         */
        const val ANIMATED_CONTENT_OPTION_PANEL = "content_option_panel"

        /**
         * Key which represents the media source BottomSheet
         */
        const val ANIMATED_MEDIA_TYPE_PANEL = "media_type_panel"

        /**
         * Key which represents the device soft keyboard
         */
        const val ANIMATED_SOFT_KEYBOARD_PANEL = "soft_keyboard"


        val MAX_SOFT_KEYBOARD_DELAY = TimeUnit.SECONDS.toMillis(1)

        /**
         * Get UmEditorAnimatedViewSwitcher singleton instance.
         * @return UmEditorAnimatedViewSwitcher instance
         */
        val instance: UmEditorAnimatedViewSwitcher
            get() {
                if (viewSwitcher == null) {
                    viewSwitcher = UmEditorAnimatedViewSwitcher()
                }
                return viewSwitcher as UmEditorAnimatedViewSwitcher
            }
    }


}
