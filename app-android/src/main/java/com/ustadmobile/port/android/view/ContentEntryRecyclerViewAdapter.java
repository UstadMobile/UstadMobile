package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.lib.db.entities.ContentEntry;

public class ContentEntryRecyclerViewAdapter extends PagedListAdapter<ContentEntry, ContentEntryRecyclerViewAdapter.ViewHolder> {

    private final AdapterViewListener listener;

    protected ContentEntryRecyclerViewAdapter(AdapterViewListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    protected interface AdapterViewListener {
        void contentEntryClicked(ContentEntry entry);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_content_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ContentEntry entry = getItem(position);
        holder.entryTitle.setText(entry.getTitle());
        holder.entryDescription.setText(entry.getDescription());
        Picasso.with(holder.thumbnailView.getContext())
                .load(entry.getThumbnailUrl())
                .into(holder.thumbnailView);
        holder.mView.setOnClickListener(view -> listener.contentEntryClicked(entry));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView entryTitle;
        public final TextView entryDescription;
        public final TextView entrySize;
        public final ImageView thumbnailView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            entryTitle = view.findViewById(R.id.entry_title);
            entryDescription = view.findViewById(R.id.entry_description);
            entrySize = view.findViewById(R.id.entry_library_size);
            thumbnailView = view.findViewById(R.id.entry_thumbnail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + entryDescription.getText() + "'";
        }

    }

    private static final DiffUtil.ItemCallback<ContentEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<ContentEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull ContentEntry oldItem, @NonNull ContentEntry newItem) {
            return oldItem.getContentEntryUid() == newItem.getContentEntryUid();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ContentEntry oldItem, @NonNull ContentEntry newItem) {
            return oldItem.equals(newItem);
        }
    };
}
