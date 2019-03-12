package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.FeedListPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.FeedEntry;

import java.util.HashMap;
import java.util.Objects;

/**
 * The Recycler Adapter for Feed Entries.
 */
class FeedListRecyclerAdapter extends
        PagedListAdapter<FeedEntry,
                FeedListRecyclerAdapter.FeedViewHolder> {

    private FeedListFragment feedListFragment;
    FeedListPresenter mPresenter;
    Context context;
    HashMap<Integer, Long> positionToFeedUid = new HashMap<>();


    class FeedViewHolder extends RecyclerView.ViewHolder {
        FeedViewHolder(View itemView) {
            super(itemView);

        }
    }

    FeedListRecyclerAdapter(FeedListFragment feedListFragment, @NonNull DiffUtil.ItemCallback<FeedEntry> diffCallback,
                            Context context, FeedListPresenter presenter) {
        super(diffCallback);
        this.feedListFragment = feedListFragment;
        this.mPresenter = presenter;
        this.context = context;

    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     *
     * @param parent        View given.
     * @param viewType      View Type not used here.
     * @return              New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View feedEntryListItem =
                LayoutInflater.from(feedListFragment.getContext()).inflate(
                        R.layout.item_feedlist_feed, parent, false);
        return new FeedViewHolder(feedEntryListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {

        FeedEntry feedEntry = getItem(position);
        if(!positionToFeedUid.containsKey(position)){
            positionToFeedUid.put(position, feedEntry.getFeedEntryUid());
        }

        TextView feedTitle = holder.itemView
            .findViewById(R.id.item_feedlist_feed_title);

        TextView feedText = holder.itemView
                .findViewById(R.id.item_feedlist_feed_card_subtitle);

        ImageView feedIcon = holder.itemView
            .findViewById(R.id.item_feedlist_feed_icon);

        Button recordAttendanceButton = holder.itemView
                .findViewById(R.id.item_feedlist_attendance_record_attendance_button);

        assert feedEntry != null;

        feedText.setText(feedEntry.getDescription());
        feedTitle.setText(feedEntry.getTitle());

        if (feedEntry.getDeadline() > 0 &&
                UMCalendarUtil.getDateInMilliPlusDays(0) > feedEntry.getDeadline()){
            feedText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(feedListFragment.getContext()),
                    R.color.accent));
            feedText.setText(feedEntry.getDescription());
        }

        if(feedEntry.getLink().startsWith(ClassDetailView.VIEW_NAME)){
            recordAttendanceButton.setText(R.string.view_class);
            //Change feedIcon as needed
        }else if(feedEntry.getLink().startsWith(PersonDetailView.VIEW_NAME)){
            recordAttendanceButton.setText(R.string.view_student);
            //Change feedIcon as needed
        }
        recordAttendanceButton.setOnClickListener(v -> mPresenter.handleClickFeedEntry(feedEntry));
        holder.itemView.setOnClickListener(v -> mPresenter.handleClickFeedEntry(feedEntry));


    }


}
