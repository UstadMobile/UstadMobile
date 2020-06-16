package com.ustadmobile.port.android.umeditor

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UMAndroidUtil.convertPixelsToDp
import com.ustadmobile.core.impl.UMAndroidUtil.getActionBarSize
import com.ustadmobile.core.impl.UMAndroidUtil.getDisplayWidth




/**
 * Customized PopUpWindow which handle both language directionality and content font size option.
 * It can handle any option with format as content list.
 *
 * **Operational flow:**
 *
 *
 * Use [UmEditorPopUpView.setMenuList] to to add list of all formats you want to
 * appear as popup options
 *
 * Use [UmEditorPopUpView.setWidthDimen] to set width of the popup view
 *
 * Use [UmEditorPopUpView.showIcons] to enable icons on the popup list items
 *
 * Use [UmEditorPopUpView.showIcons] to showWithListener the options
 *
 *
 * @author kileha3
 */

class UmEditorPopUpView
/**
 * Constrictor used to initialize PopUp view.
 * @param activity Application activity
 * @param anchor View where popup view will be anchored
 */
(private val activity: FragmentActivity, private val anchorView: View) {

    private var popupWindow: PopupWindow? = null

    private var popUpAdapter: PopUpAdapter? = null

    private var visible = true

    private var isToolBarAnchored = false

    private var listener: OnPopUpMenuClickListener? = null


    /**
     * Interface which is used to handle popup item clicks
     */
    interface OnPopUpMenuClickListener {
        /**
         * Invoked when an item is clicked
         * @param format selected format
         */
        fun onMenuClicked(format: UmFormat)
    }


    init {
        initializePopUpWindow()
    }

    @SuppressLint("ObsoleteSdkInt", "InflateParams")
    private fun initializePopUpWindow() {
        val layoutInflater = activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = layoutInflater.inflate(R.layout.popup_item_container_view, null)
        popupWindow = PopupWindow(rootView)
        popupWindow!!.height = ListPopupWindow.WRAP_CONTENT
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.isFocusable = true
        popupWindow!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow!!.elevation = 20f
        }

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.menu_items)
        popUpAdapter = PopUpAdapter(ArrayList())
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = popUpAdapter
    }

    /**
     * Set popup list items
     * @param menuList Lust of all items to be set on popup view
     * @return UmEditorPopUpView instance
     */
    fun setMenuList(menuList: List<UmFormat>): UmEditorPopUpView {
        popUpAdapter!!.setMenuList(menuList)
        return this
    }

    /**
     * Set width of the popup view
     * @param displayWidth dimension to be set
     * @param isToolBarAnchored True when popup will be anchored on toolbar view
     * otherwise false.
     * @return UmEditorPopUpView instance
     */
    fun setWidthDimen(displayWidth: Int, isToolBarAnchored: Boolean): UmEditorPopUpView {
        this.isToolBarAnchored = isToolBarAnchored
        val width = if (displayWidth <= 320)
            if (isToolBarAnchored) 0.77 else 0.57
        else
            if (isToolBarAnchored) 1.25 else 0.83
        popupWindow!!.width = (width * displayWidth).toInt()
        return this
    }


    /**
     * Enable view to showWithListener/hide icons on the list
     * @param visible Show when true otherwise hide.
     * @return UmEditorPopUpView instance
     */
    fun showIcons(visible: Boolean): UmEditorPopUpView {
        this.visible = visible
        return this
    }

    /**
     * Show popup window to the UI
     * @param listener Listen to listen for the popup list item clicks.
     */
    fun showWithListener(listener: OnPopUpMenuClickListener) {
        this.listener = listener
        if (!isToolBarAnchored) {
            popupWindow!!.showAtLocation(anchorView, Gravity.BOTTOM, -250, 100)
        } else {
            val toolbarSize = getActionBarSize(activity)
            val displayWith = getDisplayWidth(activity)
            val yOffset = convertPixelsToDp((toolbarSize * 2.9).toFloat()) * -1
            val xOffset = (0.03 * displayWith * -1).toInt()
            popupWindow!!.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)
        }
    }


    private inner class PopUpAdapter internal constructor(private var menuList: List<UmFormat>?) : RecyclerView.Adapter<PopUpItemViewHolder>() {

        internal fun setMenuList(menuList: List<UmFormat>) {
            this.menuList = menuList
            notifyDataSetChanged()
        }


        private fun changeState(formatIcon: ImageView,
                                formatHolder: RelativeLayout, formatTile: TextView, isActivated: Boolean) {
            formatIcon.setColorFilter(ContextCompat.getColor(activity,
                    if (isActivated) R.color.primaryTextColor else R.color.secondaryTextColor))
            formatTile.setTextColor(ContextCompat.getColor(activity,
                    if (isActivated) R.color.primaryTextColor else R.color.secondaryTextColor))
            formatHolder.setBackgroundColor(ContextCompat.getColor(activity,
                    if (isActivated) R.color.colorIconTint else R.color.primaryTextColor))
        }

        @NonNull
        override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): PopUpItemViewHolder {
            return PopUpItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_popup_formatting_view, parent, false))
        }

        override fun onBindViewHolder(@NonNull holder: PopUpItemViewHolder, position: Int) {
            val format = menuList!![position]
            holder.menuIcon.visibility = if (visible) View.VISIBLE else View.GONE
            holder.menuIcon.setImageResource(format.formatIcon)
            holder.meuTitle.setText(format.formatTitle)
            changeState(holder.menuIcon, holder.menuHolder, holder.meuTitle, format.active)

            holder.itemView.setOnClickListener {
                listener!!.onMenuClicked(format)
                popupWindow!!.dismiss()
            }
        }

        override fun getItemCount(): Int {
            return menuList!!.size
        }
    }

    private inner class PopUpItemViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var menuHolder: RelativeLayout = itemView.findViewById(R.id.icon_holder)

        internal var menuIcon: ImageView = itemView.findViewById(R.id.format_icon)

        internal var meuTitle: TextView = itemView.findViewById(R.id.format_title)

    }

}
