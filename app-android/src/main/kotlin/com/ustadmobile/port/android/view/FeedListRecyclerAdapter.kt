package com.ustadmobile.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.FeedListPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.FeedEntry
import java.util.*

/**
 * The Recycler Adapter for Feed Entries.
 */
internal class FeedListRecyclerAdapter(private val feedListFragment: FeedListFragment,
                                       diffCallback: DiffUtil.ItemCallback<FeedEntry>,
                                       var context: Context, var mPresenter: FeedListPresenter)
    : PagedListAdapter<FeedEntry, FeedListRecyclerAdapter.FeedViewHolder>(diffCallback) {
    var positionToFeedUid = HashMap<Int, Long>()


    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     *
     * @param parent        View given.
     * @param viewType      View Type not used here.
     * @return              New ViewHolder for the ClazzStudent type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val feedEntryListItem = LayoutInflater.from(feedListFragment.context).inflate(
                R.layout.item_feedlist_feed, parent, false)
        return FeedViewHolder(feedEntryListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {

        val feedEntry = getItem(position)
        if (!positionToFeedUid.containsKey(position)) {
            positionToFeedUid[position] = feedEntry!!.feedEntryUid
        }

        val feedTitle = holder.itemView
                .findViewById<TextView>(R.id.item_feedlist_feed_title)

        val feedText = holder.itemView
                .findViewById<TextView>(R.id.item_feedlist_feed_card_subtitle)

        val feedIcon = holder.itemView
                .findViewById<ImageView>(R.id.item_feedlist_feed_icon)

        val recordAttendanceButton = holder.itemView
                .findViewById<Button>(R.id.item_feedlist_attendance_record_attendance_button)

        assert(feedEntry != null)

        feedText.text = feedEntry!!.description
        feedTitle.text = feedEntry.title

        if (feedEntry.deadline > 0 && UMCalendarUtil.getDateInMilliPlusDays(0) > feedEntry.deadline) {
            feedText.setTextColor(ContextCompat.getColor(feedListFragment.context!!,
                    R.color.accent))
            feedText.text = feedEntry.description
        }

        if (feedEntry.link!!.startsWith(ClazzDetailView.VIEW_NAME)) {
            recordAttendanceButton.setText(R.string.view_class)
            //Change feedIcon as needed
        } else if (feedEntry.link!!.startsWith(PersonDetailView.VIEW_NAME)) {
            recordAttendanceButton.setText(R.string.view_student)
            //Change feedIcon as needed
        }
        recordAttendanceButton.setOnClickListener { v -> mPresenter.handleClickFeedEntry(feedEntry) }
        holder.itemView.setOnClickListener { v -> mPresenter.handleClickFeedEntry(feedEntry) }


    }


}
