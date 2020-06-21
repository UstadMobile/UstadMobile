package com.ustadmobile.port.android.umeditor

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UMAndroidUtil.convertDpToPixel
import com.ustadmobile.port.android.view.ContentEditorActivity
import com.ustadmobile.port.android.view.ContentEditorActivity.UmFormatHelper.Companion.ACTIONS_TOOLBAR_INDEX


/**
 * Customized toolbar view which handles quick action menus on the editor.
 *
 * **Operational flow:**
 *
 *
 * Use [UmEditorActionView.inflateMenu] to inflate all your menus to be shown
 * as quick action menus.
 *
 * Use [UmEditorActionView.setQuickActionMenuItemClickListener]
 * to set listener which listens for quick action menu clicks.
 *
 *
 * @author kileha3
 */
class UmEditorActionView : Toolbar, UmFormatStateChangeListener {

    private var onQuickActionMenuItemClicked: OnQuickActionMenuItemClicked? = null

    private var isQuickAction = false

    private var formatList: List<UmFormat>? = null

    private var umFormatHelper: ContentEditorActivity.UmFormatHelper? = null

    private var deviceWidth = 0

    /**
     * Constructor to be used for Java instantiation.
     * @param context application context
     */
    constructor(context: Context) : super(context) {}

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     * @param defStyleAttr style sets
     */
    constructor(context: Context, @Nullable attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     */
    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    /**
     * Set formatting helper
     * @param umFormatHelper format helper instance
     */
    fun setUmFormatHelper(umFormatHelper: ContentEditorActivity.UmFormatHelper) {
        this.umFormatHelper = umFormatHelper
        this.umFormatHelper!!.setStateChangeListener(this)
        deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    }


    /**
     * Set quick action menu item click listener
     * @param clickListener Listener to be set
     */
    fun setQuickActionMenuItemClickListener(clickListener: OnQuickActionMenuItemClicked) {
        this.onQuickActionMenuItemClicked = clickListener
    }

    /**
     * Inflate menu to the toolbar
     * @param resId menu resource id to be inflated
     * @param isQuickAction True if toolbar will be used as quick actions otherwise false.
     */
    fun inflateMenu(resId: Int, isQuickAction: Boolean) {
        this.isQuickAction = isQuickAction
        formatList = if (isQuickAction)
            umFormatHelper!!.quickActions
        else
            umFormatHelper!!.getFormatListByType(ACTIONS_TOOLBAR_INDEX)
        inflateMenu(resId)
    }

    override fun inflateMenu(resId: Int) {
        super.inflateMenu(resId)

        for (format in formatList!!) {
            val menuItem = menu.getItem(formatList!!.indexOf(format))
            val rootView = menuItem.actionView as FrameLayout
            val formatIcon: ImageView = rootView.findViewById(R.id.format_icon)
            setImageSize(formatIcon)
            val formatHolder = rootView.findViewById<FrameLayout>(R.id.icon_holder)
            formatIcon.setImageResource(format.formatIcon)
            changeState(formatIcon, formatHolder, format.active)
            formatHolder.setOnClickListener {
                if (!isQuickAction) {
                    onQuickActionMenuItemClicked!!.onQuickMenuViewClicked(format.formatId)
                } else {
                    onQuickActionMenuItemClicked!!.onQuickMenuItemClicked(format.formatCommand)
                }
            }
        }
    }


    /**
     * Set toolbar icon size based on device resolution
     * @param image Toolbar icon.
     */
    private fun setImageSize(image: ImageView) {
        image.requestLayout()
        val dimen = convertDpToPixel(if (deviceWidth < DEFAULT_HIGH_RES_DEVICE_WIDTH) 18 else 22)
        image.layoutParams.height = dimen
        image.layoutParams.width = dimen
    }


    /**
     * Set state of the MenuItem
     * @param formatIcon ImageView as icon holder
     * @param formatHolder FrameLayout as menu holder
     * @param isActivated state which indicate whether the munu is activated ot not.
     */
    private fun changeState(formatIcon: ImageView,
                            formatHolder: FrameLayout, isActivated: Boolean) {
        formatIcon.setColorFilter(ContextCompat.getColor(context,
                if (isActivated || !isQuickAction) R.color.primaryTextColor else R.color.iconTintColor))

        if (isQuickAction) {
            formatHolder.setBackgroundColor(ContextCompat.getColor(context,
                    if (isActivated) R.color.iconTintColor else R.color.primaryTextColor))
        }
    }

    /**
     * Find exactly menu to be updated by its ID
     * @param itemId id to be found
     * @return MenuItem to be updated
     */
    private fun findById(itemId: Int): MenuItem? {
        var menuItem: MenuItem? = null
        for (i in 0 until menu.size()) {
            menuItem = menu.getItem(i)
            if (menuItem!!.itemId == itemId) {
                break
            }
        }
        return menuItem
    }

    override fun onStateChanged(formatList: List<UmFormat>) {
        for (umFormat in formatList) {
            val menuItem = findById(umFormat.formatId)
            if (menuItem != null) {
                val rootView = menuItem.actionView as FrameLayout
                val formatIcon: ImageView = rootView.findViewById(R.id.format_icon)
                setImageSize(formatIcon)
                val formatHolder = rootView.findViewById<FrameLayout>(R.id.icon_holder)
                changeState(formatIcon, formatHolder, umFormat.active && ContentEditorActivity.UmFormatHelper
                        .isTobeHighlighted(umFormat.formatCommand.toString()))
            }
        }
    }

    /**
     * Interface which listen for the clicks on inflated menu.
     */
    interface OnQuickActionMenuItemClicked {
        /**
         * Invoked when an quick action menu item is clicked.
         * @param command command to be executed.
         */
        fun onQuickMenuItemClicked(command: String?)

        /**
         * Invoked when menu view is clicked
         * @param itemId menu item id
         */
        fun onQuickMenuViewClicked(itemId: Int)
    }

    companion object {

        private const val DEFAULT_HIGH_RES_DEVICE_WIDTH = 1080
    }
}
