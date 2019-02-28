package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEntryDetailPresenter;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.core.controller.ContentEntryDetailPresenter.LOCALLY_AVAILABLE_ICON;
import static com.ustadmobile.core.controller.ContentEntryDetailPresenter.LOCALLY_NOT_AVAILABLE_ICON;

public class ContentEntryDetailActivity extends UstadBaseActivity implements
        ContentEntryDetailView, ContentEntryDetailLanguageAdapter.AdapterViewListener,
        LocalAvailabilityMonitor , LocalAvailabilityListener {

    private ContentEntryDetailPresenter entryDetailPresenter;

    private NetworkManagerAndroidBle managerAndroidBle;

    HashMap<Integer,Integer> fileStatusIcon = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);


        setUMToolbar(R.id.entry_detail_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileStatusIcon.put(LOCALLY_AVAILABLE_ICON,R.drawable.ic_nearby_black_24px);
        fileStatusIcon.put(LOCALLY_NOT_AVAILABLE_ICON,R.drawable.ic_cloud_download_black_24dp);

        new Handler().postDelayed(() ->
                entryDetailPresenter.handleUpdateStatusIconAndText(new HashSet<>()),
                TimeUnit.SECONDS.toMillis(1));
    }


    @Override
    protected void onBleNetworkServiceBound(NetworkManagerBle networkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle);
        managerAndroidBle = (NetworkManagerAndroidBle) networkManagerBle;
        entryDetailPresenter = new ContentEntryDetailPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this,this);
        entryDetailPresenter.onCreate(UMAndroidUtil.bundleToHashtable(new Bundle()));
        entryDetailPresenter.onStart();
        managerAndroidBle.addLocalAvailabilityListener(this::onLocalAvailabilityChanged);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                clickUpNavigation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clickUpNavigation() {
        runOnUiThread(() -> {
            if (entryDetailPresenter != null) {
                entryDetailPresenter.handleUpNavigation();
            }
        });


    }

    @Override
    public void setContentInfo(ContentEntry contentEntry) {

        runOnUiThread(() -> {
            TextView title = findViewById(R.id.entry_detail_title);
            title.setText(contentEntry.getTitle());

            TextView desc = findViewById(R.id.entry_detail_description);
            desc.setText(contentEntry.getDescription());


            if (contentEntry.getThumbnailUrl() != null && !contentEntry.getThumbnailUrl().isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> Picasso.with(ContentEntryDetailActivity.this)
                        .load(contentEntry.getThumbnailUrl())
                        .into((ImageView) findViewById(R.id.entry_detail_thumbnail)));
            }


            getSupportActionBar().setTitle(contentEntry.getTitle());

            TextView license = findViewById(R.id.entry_detail_license);
            license.setText(contentEntry.getLicenseName());

            TextView author = findViewById(R.id.entry_detail_author);
            author.setText(contentEntry.getAuthor());
        });


    }

    @Override
    public void setFileInfo(List<ContentEntryFile> contentEntryFileList) {
        runOnUiThread(() -> {
            Button button = findViewById(R.id.entry_detail_button);

            if (contentEntryFileList == null || contentEntryFileList.isEmpty()) {
                button.setEnabled(false);
                return;
            }

            button.setEnabled(true);
            if (contentEntryFileList.size() == 1) {

                ContentEntryFile contentEntryFile = contentEntryFileList.get(0);


                TextView downloadSize = findViewById(R.id.entry_detail_content_size);
                downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

            } else {

                // TODO manage multiple files

                ContentEntryFile contentEntryFile = contentEntryFileList.get(0);

                TextView downloadSize = findViewById(R.id.entry_detail_content_size);
                downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

            }
        });
    }

    @Override
    public void setTranslationsAvailable(List<ContentEntryRelatedEntryJoinWithLanguage> result, long entryUuid) {

        runOnUiThread(() -> {
            RecyclerView flexBox = findViewById(R.id.entry_detail_flex);

            if (result.size() == 0) {
                flexBox.setVisibility(View.GONE);
            } else {
                flexBox.setVisibility(View.VISIBLE);

                FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
                flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
                flexBox.setLayoutManager(flexboxLayoutManager);

                ContentEntryDetailLanguageAdapter adapter = new ContentEntryDetailLanguageAdapter(result, this, entryUuid);
                flexBox.setAdapter(adapter);
            }
        });

    }

    @Override
    public void setDownloadProgress(ContentEntryStatus status) {
        runOnUiThread(() -> {

            Button button = findViewById(R.id.entry_detail_button);
            DownloadProgressView downloadProgressView = findViewById(R.id.entry_detail_progress);

            if (status != null) {
                if (status.getDownloadStatus() == 0 || status.getDownloadStatus() == JobStatus.COMPLETE) {
                    button.setVisibility(View.VISIBLE);
                    downloadProgressView.setVisibility(View.GONE);

                    boolean isDownloadComplete = status.getDownloadStatus() == JobStatus.COMPLETE;
                    button.setText(status.getDownloadStatus() == JobStatus.COMPLETE ? R.string.open : R.string.download);

                    button.setOnClickListener(view ->
                            entryDetailPresenter.handleDownloadButtonClick(isDownloadComplete));

                } else {
                    button.setVisibility(View.GONE);
                    downloadProgressView.setVisibility(View.VISIBLE);
                    if (status.getTotalSize() > 0) {
                        downloadProgressView.setProgress((float) status.getBytesDownloadSoFar() /
                                (float) status.getTotalSize());
                    }
                    downloadProgressView.setStatusText("Downloading");
                }
            } else {
                button.setVisibility(View.VISIBLE);
                downloadProgressView.setVisibility(View.GONE);
                button.setText(R.string.download);
                button.setOnClickListener(view ->
                        entryDetailPresenter.handleDownloadButtonClick(false));
            }

        });
    }

    @Override
    public void handleFileOpenError() {
        runOnUiThread(() -> Toast.makeText((Context) getContext(), R.string.error, Toast.LENGTH_LONG).show());
    }

    @Override
    public void updateStatusIconAndText(int icon, String status) {
        TextView statusText = findViewById(R.id.content_status_text);
        ImageView statusIcon = findViewById(R.id.content_status_icon);

        statusText.setVisibility(View.VISIBLE);
        statusIcon.setVisibility(View.VISIBLE);

        statusIcon.setImageResource(fileStatusIcon.get(icon));
        statusText.setText(status);
    }


    @Override
    public void selectContentEntryOfLanguage(long uid) {
        entryDetailPresenter.handleClickTranslatedEntry(uid);
    }

    @Override
    public void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor) {
        managerAndroidBle.startMonitoringAvailability(monitor,entryUidsToMonitor);
    }

    @Override
    public void stopMonitoringAvailability(Object monitor) {
        managerAndroidBle.stopMonitoringAvailability(monitor);
    }

    @Override
    public void onDestroy() {
        entryDetailPresenter.onDestroy();
        networkManagerBle.removeLocalAvailabilityListener(this::onLocalAvailabilityChanged);
        super.onDestroy();
    }

    @Override
    public void onLocalAvailabilityChanged(Set<Long> locallyAvailableEntries) {
        entryDetailPresenter.handleUpdateStatusIconAndText(locallyAvailableEntries);
    }
}
