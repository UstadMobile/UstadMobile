package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzActivityListPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle

/**
 * The ClazzActivityList's recycler adapter.
 */
class ClazzActivityListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzActivityWithChangeTitle>,
        internal var theContext: Context, private val theFragment: Fragment,
        private val thePresenter: ClazzActivityListPresenter,
        private val showImage: Boolean?)
    : PagedListAdapter<ClazzActivityWithChangeTitle,
        ClazzActivityListRecyclerAdapter.ClazzActivityViewHolder>(diffCallback) {


    class ClazzActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzActivityViewHolder {
        val clazzLogListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_clazzlog_log, parent, false)
        return ClazzActivityViewHolder(clazzLogListItem)

    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * For every item part of the recycler adapter, this will be called and every item in it
     * will be set as per this function.
     *
     * @param holder            The holder
     * @param position          The position
     */
    override fun onBindViewHolder(holder: ClazzActivityViewHolder, position: Int) {
        val clazzActivity = getItem(position)!!
        val wasItGood = clazzActivity.isClazzActivityGoodFeedback


        val currentLocale = theFragment.resources.configuration.locale

        val prettyDate = UMCalendarUtil.getPrettyDateFromLong(
                clazzActivity.clazzActivityLogDate, currentLocale)
        val prettyShortDay = UMCalendarUtil.getSimpleDayFromLongDate(
                clazzActivity.clazzActivityLogDate, currentLocale)

        val statusTextView = holder.itemView
                .findViewById<TextView>(R.id.item_clazzlog_log_status_text)

        val secondaryTextImageView = holder.itemView.findViewById<View>(R.id.item_clazzlog_log_status_text_imageview)

        var verb = clazzActivity.changeTitle
        if (verb == null || verb.isEmpty()) {
            verb = "Increased group work by"
        }

        if (!wasItGood) {
            secondaryTextImageView.background = AppCompatResources.getDrawable(theContext, R.drawable.ic_thumb_down_black_24dp)
        } else {
            secondaryTextImageView.background = AppCompatResources.getDrawable(theContext, R.drawable.ic_thumb_up_black_24dp)
        }

        val desc = (verb + " " + clazzActivity.clazzActivityQuantity
                + " times")
        statusTextView.text = desc

        (holder.itemView
                .findViewById(R.id.item_clazzlog_log_date) as TextView)
                .setText(prettyDate as String)
        (holder.itemView
                .findViewById(R.id.item_clazzlog_log_day) as TextView)
                .setText(prettyShortDay as String)

        if ((!showImage!!)) {
            secondaryTextImageView.visibility = View.INVISIBLE

            //Change the constraint layout so that the hidden bits are not empty spaces.
            val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_clazzlog_log_cl)
            val constraintSet = ConstraintSet()
            constraintSet.clone(cl)

            constraintSet.connect(R.id.item_clazzlog_log_status_text,
                    ConstraintSet.START, R.id.item_clazzlog_log_calendar_image,
                    ConstraintSet.END, 16)

            constraintSet.applyTo(cl)


        } else {
            secondaryTextImageView.visibility = View.VISIBLE
        }


        holder.itemView.setOnClickListener { v ->
            thePresenter.goToNewClazzActivityEditActivity(clazzActivity.clazzActivityUid, FLAG_ACTIVITY_NEW_TASK) }
    }
}