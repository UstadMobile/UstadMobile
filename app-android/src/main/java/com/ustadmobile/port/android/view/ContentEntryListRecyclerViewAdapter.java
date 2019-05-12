package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.impl.UMLogger;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ContentEntryListRecyclerViewAdapter extends
        PagedListAdapter<ContentEntryWithStatusAndMostRecentContainerUid,
        ContentEntryListRecyclerViewAdapter.ViewHolder> implements LocalAvailabilityListener {

    private final AdapterViewListener listener;

    private LocalAvailabilityMonitor monitor;

    private Set<Long> containerUidsToMonitor = new HashSet<>();

    private WeakHashMap<Integer, ViewHolder> viewHolderWeakHashMap;

    private NetworkManagerAndroidBle managerAndroidBle;

    private FragmentActivity activity;

    ContentEntryListRecyclerViewAdapter(FragmentActivity activity, AdapterViewListener listener,
                                        LocalAvailabilityMonitor monitor) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.monitor = monitor;
        this.activity = activity;
        viewHolderWeakHashMap = new WeakHashMap<>();
    }

    void setNetworkManager(NetworkManagerAndroidBle managerAndroidBle){
        this.managerAndroidBle = managerAndroidBle;
        managerAndroidBle.addLocalAvailabilityListener(this);
    }

    @Override
    public void onLocalAvailabilityChanged(Set<Long> locallyAvailableEntries) {
        List<ViewHolder> viewHoldersToNotify = new ArrayList<>(viewHolderWeakHashMap.values());
        for(ViewHolder viewHolder : viewHoldersToNotify){
            boolean available = locallyAvailableEntries.contains(viewHolder.getContainerUid());
            UstadMobileSystemImpl.l(UMLogger.DEBUG,694,
                    "Entry status check received  " + available);
            activity.runOnUiThread(() -> viewHolder.updateLocallyAvailabilityStatus(available));
        }
    }

    protected interface AdapterViewListener {
        void contentEntryClicked(ContentEntry entry);

        void downloadStatusClicked(ContentEntry entry);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {

        for(Map.Entry<Integer,ViewHolder> holderMap : viewHolderWeakHashMap.entrySet()){
            if(holderMap.getValue().equals(holder)){
                viewHolderWeakHashMap.remove(holderMap.getKey());
                break;
            }
        }
        super.onViewRecycled(holder);

    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_content_entry, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if(monitor != null){
            containerUidsToMonitor.clear();
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ContentEntryWithStatusAndMostRecentContainerUid entry = getItem(position);

        if (entry == null) {
            holder.getEntryTitle().setText("");
            holder.getEntryDescription().setText("");
            holder.getThumbnailView().setImageDrawable(null);
            holder.getDownloadView().setProgress(0);
            holder.getDownloadView().setImageResource(R.drawable.ic_file_download_black_24dp);
            holder.getView().setOnClickListener(null);
            holder.getDownloadView().setOnClickListener(null);
            holder.getAvailabilityStatus().setText("");
            holder.getAvailabilityIcon().setImageDrawable(null);
        } else {

            boolean available = false;
            if(managerAndroidBle != null)
                available = managerAndroidBle.isEntryLocallyAvailable(
                        entry.getMostRecentContainer());

            if(entry.getLeaf()){
                holder.updateLocallyAvailabilityStatus(available);
            }

            holder.setContainerUid(entry.getMostRecentContainer());

            holder.getView().setTag(entry.getContentEntryUid());
            holder.getEntryTitle().setText(entry.getTitle());
            holder.getEntryDescription().setText(entry.getDescription());
            if (entry.getThumbnailUrl() == null || entry.getThumbnailUrl().isEmpty()) {
                holder.getThumbnailView().setImageDrawable(null);
            } else {
                Picasso.get()
                        .load(entry.getThumbnailUrl())
                        .into(holder.getThumbnailView());
            }


            String contentDescription = null;
            boolean showLocallyAvailabilityViews = true;
            if (entry.getContentEntryStatus() != null) {
                Context context = holder.getView().getContext();
                ContentEntryStatus status = entry.getContentEntryStatus();
                int dlStatus = status.getDownloadStatus();

                if (dlStatus > 0 && dlStatus <= JobStatus.RUNNING_MAX && status.getTotalSize() > 0) {
                    contentDescription = context.getString(R.string.download_entry_state_downloading);
                    holder.getDownloadView().setProgress((int) ((status.getBytesDownloadSoFar() * 100) / status.getTotalSize()));
                } else {
                    contentDescription = context.getString(R.string.download_entry_state_queued);
                    holder.getDownloadView().setProgress(0);
                }

                if (dlStatus > 0 && dlStatus < JobStatus.WAITING_MAX) {
                    holder.getDownloadView().setImageResource(R.drawable.ic_pause_black_24dp);
                    contentDescription = context.getString(R.string.download_entry_state_paused);
                } else if (dlStatus == JobStatus.COMPLETE) {
                    showLocallyAvailabilityViews = false;
                    holder.getDownloadView().setImageResource(R.drawable.ic_offline_pin_black_24dp);
                    contentDescription = context.getString(R.string.download_entry_state_downloaded);
                } else {
                    holder.getDownloadView().setImageResource(R.drawable.ic_file_download_black_24dp);
                }
            } else {
                holder.getDownloadView().setProgress(0);
                holder.getDownloadView().setImageResource(R.drawable.ic_file_download_black_24dp);
            }


            ImageView iconView = holder.getIconView();
            int iconFlag = entry.getContentTypeFlag();
            if (iconFlag == ContentEntry.EBOOK_TYPE) {
                iconView.setImageResource(R.drawable.ic_book_black_24dp);
            } else if (iconFlag == ContentEntry.AUDIO_TYPE) {
                iconView.setImageResource(R.drawable.ic_audiotrack_24px);
            } else if (iconFlag == ContentEntry.VIDEO_TYPE) {
                iconView.setImageResource(R.drawable.ic_video_library_24px);
            } else if (iconFlag == ContentEntry.COLLECTION_TYPE) {
                iconView.setImageResource(R.drawable.ic_collections_24px);
            } else if (iconFlag == ContentEntry.DOCUMENT_TYPE) {
                iconView.setImageResource(R.drawable.ic_file_24px);
            } else if (iconFlag == ContentEntry.INTERACTIVE_EXERICSE_TYPE) {
                iconView.setImageResource(R.drawable.ic_assignment_24px);
            } else if (iconFlag == ContentEntry.ARTICLE_TYPE){
                iconView.setImageResource(R.drawable.ic_newspaper);
            } else{
                iconView.setImageResource(R.drawable.ic_book_black_24dp);
            }

            if (iconFlag == ContentEntry.UNDEFINED_TYPE) {
                iconView.setVisibility(View.GONE);
            } else {
                iconView.setVisibility(View.VISIBLE);
            }

            int viewVisibility = showLocallyAvailabilityViews && entry.getLeaf()
                    ? View.VISIBLE: View.GONE;
            holder.getAvailabilityIcon().setVisibility(viewVisibility);
            holder.getAvailabilityStatus().setVisibility(viewVisibility);

            //add as a reference only if is a leaf entry
            if(entry.getLeaf()){
                viewHolderWeakHashMap.put(entry.hashCode(),holder);
            }

            List<Long> containerUidList = getUniqueContainerUidsListTobeMonitored();
            if(!containerUidList.isEmpty()){
                containerUidsToMonitor.addAll(containerUidList);
                monitor.startMonitoringAvailability(monitor,containerUidList);
            }

            holder.getDownloadView().getImageResource().setContentDescription(contentDescription);
            holder.getView().setOnClickListener(view -> listener.contentEntryClicked(entry));
            holder.getDownloadView().setOnClickListener(view -> listener.downloadStatusClicked(entry));
        }


    }

    /**
     * @return List of container uids that can be monitored (Requires status).
     */
    private List<Long> getUniqueContainerUidsListTobeMonitored() {
        List<ContentEntryWithStatusAndMostRecentContainerUid> currentDisplayedEntryList =
                getCurrentList() == null ? new ArrayList<>() : getCurrentList();
        List<Long> uidsToMonitor = new ArrayList<>();
        for (ContentEntryWithStatusAndMostRecentContainerUid entry : currentDisplayedEntryList) {

            boolean canBeMonitored = entry != null && (entry.getContentEntryStatus() == null ||
                    entry.getContentEntryStatus().getDownloadStatus() != JobStatus.COMPLETE)
                    && !containerUidsToMonitor.contains(entry.getMostRecentContainer())
                    && entry.getLeaf();
            if (canBeMonitored) {
                uidsToMonitor.add(entry.getMostRecentContainer());
            }

        }
        return uidsToMonitor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final TextView entryTitle;
        final TextView entryDescription;
        final TextView entrySize;
        final ImageView thumbnailView;
        final ImageView availabilityIcon;
        final TextView availabilityStatus;
        final DownloadStatusButton downloadView;
        final ImageView iconView;
        private long containerUid;

        ViewHolder(View view) {
            super(view);
            mView = view;
            entryTitle = view.findViewById(R.id.content_entry_item_title);
            entryDescription = view.findViewById(R.id.content_entry_item_description);
            entrySize = view.findViewById(R.id.content_entry_item_library_size);
            thumbnailView = view.findViewById(R.id.content_entry_item_thumbnail);
            downloadView = view.findViewById(R.id.content_entry_item_download);
            iconView = view.findViewById(R.id.content_entry_item_imageview);
            availabilityIcon = view.findViewById(R.id.content_entry_local_availability_icon);
            availabilityStatus = view.findViewById(R.id.content_entry_local_availability_status);
        }

        void updateLocallyAvailabilityStatus(boolean available){
            int icon = available ? R.drawable.ic_nearby_black_24px :
                    R.drawable.ic_cloud_download_black_24dp;
            int status = available ? R.string.download_locally_availability :
                    R.string.download_cloud_availability;
            getAvailabilityIcon().setImageResource(icon);
            getAvailabilityStatus().setText(getView().getContext().getString(status));
        }

        void setContainerUid(long containerUid) {
            this.containerUid = containerUid;
        }

        long getContainerUid() {
            return containerUid;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + entryDescription.getText() + "'";
        }

        public View getView() {
            return mView;
        }

        TextView getEntryTitle() {
            return entryTitle;
        }

        TextView getEntryDescription() {
            return entryDescription;
        }

        public TextView getEntrySize() {
            return entrySize;
        }

        ImageView getThumbnailView() {
            return thumbnailView;
        }

        public DownloadStatusButton getDownloadView() {
            return downloadView;
        }

        public ImageView getIconView() {
            return iconView;
        }

        public ImageView getAvailabilityIcon() {
            return availabilityIcon;
        }

        public TextView getAvailabilityStatus() {
            return availabilityStatus;
        }
    }

    private static final DiffUtil.ItemCallback<ContentEntryWithStatusAndMostRecentContainerUid> DIFF_CALLBACK = new DiffUtil.ItemCallback<ContentEntryWithStatusAndMostRecentContainerUid>() {
        @Override
        public boolean areItemsTheSame(@NonNull ContentEntryWithStatusAndMostRecentContainerUid oldItem,
                                       @NonNull ContentEntryWithStatusAndMostRecentContainerUid newItem) {
            return oldItem.getContentEntryUid() == newItem.getContentEntryUid();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ContentEntryWithStatusAndMostRecentContainerUid oldItem,
                                          @NonNull ContentEntryWithStatusAndMostRecentContainerUid newItem) {
            if (oldItem.getTitle() != null ? !oldItem.getTitle().equals(newItem.getTitle()) : newItem.getTitle() != null) {
                return false;
            }
            if (oldItem.getDescription() != null ? !oldItem.getDescription().equals(newItem.getDescription()) : newItem.getDescription() != null)
                return false;
            if (oldItem.getThumbnailUrl() != null ? !oldItem.getThumbnailUrl().equals(newItem.getThumbnailUrl()) : newItem.getThumbnailUrl() == null) {
                return false;
            }
            if (oldItem.getContentEntryStatus() != null && newItem.getContentEntryStatus() != null) {

                if (oldItem.getContentEntryStatus().getBytesDownloadSoFar() != newItem.getContentEntryStatus().getBytesDownloadSoFar()) {
                    return false;
                }

                if (oldItem.getContentEntryStatus().getDownloadStatus() != newItem.getContentEntryStatus().getDownloadStatus()) {
                    return false;
                }

                return oldItem.getContentEntryStatus().getTotalSize() == newItem.getContentEntryStatus().getTotalSize();

            } else return (newItem.getContentEntryStatus() == null && newItem.getContentEntryStatus() == null)
                    || newItem.getContentEntryStatus().equals(oldItem.getContentEntryStatus());
        }
    };
}
