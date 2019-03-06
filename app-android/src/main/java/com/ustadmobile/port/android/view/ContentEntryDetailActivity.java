package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
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
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static com.ustadmobile.core.controller.ContentEntryDetailPresenter.LOCALLY_AVAILABLE_ICON;
import static com.ustadmobile.core.controller.ContentEntryDetailPresenter.LOCALLY_NOT_AVAILABLE_ICON;

public class ContentEntryDetailActivity  extends UstadBaseActivity implements
        ContentEntryDetailView, ContentEntryDetailLanguageAdapter.AdapterViewListener,
        LocalAvailabilityMonitor, LocalAvailabilityListener {

    private ContentEntryDetailPresenter entryDetailPresenter;

    private NetworkManagerAndroidBle managerAndroidBle;

    private TextView localAvailabilityStatusText;

    private TextView entryDetailsTitle;

    private TextView entryDetailsDesc;

    private TextView entryDetailsLicense;

    private TextView entryDetailsAuthor;

    private TextView translationAvailableLabel;

    private TextView downloadSize;

    private ImageView localAvailabilityStatusIcon;

    private RecyclerView flexBox;

    private Button downloadButton;

    private DownloadProgressView downloadProgress;

    HashMap<Integer,Integer> fileStatusIcon = new HashMap<>();


    @Override
    protected void onBleNetworkServiceBound(NetworkManagerBle networkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle);
        managerAndroidBle = (NetworkManagerAndroidBle) networkManagerBle;
        entryDetailPresenter = new ContentEntryDetailPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this,this);
        entryDetailPresenter.onCreate(UMAndroidUtil.bundleToHashtable(new Bundle()));
        entryDetailPresenter.onStart();
        managerAndroidBle.addLocalAvailabilityListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        localAvailabilityStatusText = findViewById(R.id.content_status_text);
        localAvailabilityStatusIcon = findViewById(R.id.content_status_icon);
        downloadButton = findViewById(R.id.entry_detail_button);
        downloadProgress = findViewById(R.id.entry_detail_progress);
        entryDetailsTitle = findViewById(R.id.entry_detail_title);
        entryDetailsDesc = findViewById(R.id.entry_detail_description);
        entryDetailsLicense = findViewById(R.id.entry_detail_license);
        entryDetailsAuthor = findViewById(R.id.entry_detail_author);
        downloadSize = findViewById(R.id.entry_detail_content_size);
        translationAvailableLabel = findViewById(R.id.entry_detail_available_label);
        flexBox = findViewById(R.id.entry_detail_flex);

        fileStatusIcon.put(LOCALLY_AVAILABLE_ICON,R.drawable.ic_nearby_black_24px);
        fileStatusIcon.put(LOCALLY_NOT_AVAILABLE_ICON,R.drawable.ic_cloud_download_black_24dp);

        setUMToolbar(R.id.entry_detail_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void setContentEntryTitle(String title) {
        entryDetailsTitle.setText(title);
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setContentEntryDesc(String desc) {
        entryDetailsDesc.setText(desc);
    }

    @Override
    public void setContentEntryLicense(String license) {
        entryDetailsLicense.setText(license);
    }

    @Override
    public void setContentEntryAuthor(String author) {
        entryDetailsAuthor.setText(author);
    }

    @Override
    public void setDetailsButtonEnabled(boolean enabled) {
        downloadButton.setEnabled(enabled);
    }

    @Override
    public void setDownloadSize(long fileSize) {
        downloadSize.setText(UMFileUtil.formatFileSize(fileSize));
    }

    @Override
    public void loadEntryDetailsThumbnail(String thumbnailUrl) {

        Picasso.with(ContentEntryDetailActivity.this)
                .load(thumbnailUrl)
                .into((ImageView) findViewById(R.id.entry_detail_thumbnail));
    }

    @Override
    public void setAvailableTranslations(List<ContentEntryRelatedEntryJoinWithLanguage> result,
                                         long entryUuid) {

        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexBox.setLayoutManager(flexboxLayoutManager);

        ContentEntryDetailLanguageAdapter adapter = new ContentEntryDetailLanguageAdapter(result,
                this, entryUuid);
        flexBox.setAdapter(adapter);

    }

    @Override
    public void updateDownloadProgress(float progressValue) {
        downloadProgress.setProgress(progressValue);
    }

    @Override
    public void setDownloadButtonVisible(boolean visible) {
        downloadButton.setVisibility(visible ? View.VISIBLE : View.GONE);

    }



    @Override
    public void setButtonTextLabel(String textLabel) {
        downloadButton.setText(textLabel);
    }

    @Override
    public void showFileOpenError() {
        Toast.makeText((Context) getContext(), R.string.error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateLocalAvailabilityViews(int icon, String status) {
        localAvailabilityStatusText.setVisibility(View.VISIBLE);
        localAvailabilityStatusIcon.setVisibility(View.VISIBLE);
        localAvailabilityStatusIcon.setImageResource(fileStatusIcon.get(icon));
        localAvailabilityStatusText.setText(status);
    }

    @Override
    public void setLocalAvailabilityStatusViewVisible(boolean visible) {
        localAvailabilityStatusIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        localAvailabilityStatusText.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTranslationLabelVisible(boolean visible) {
        translationAvailableLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setFlexBoxVisible(boolean visible) {
        flexBox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setDownloadProgressVisible(boolean visible) {
        downloadProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setDownloadProgressLabel(String progressLabel) {
        downloadProgress.setStatusText(progressLabel);
    }

    @Override
    public void setDownloadButtonClickableListener(boolean isDownloadComplete) {
        downloadButton.setOnClickListener(view ->
                entryDetailPresenter.handleDownloadButtonClick(isDownloadComplete,
                        entryDetailPresenter.getEntryUuid()));
    }

    @Override
    public void showDownloadOptionsDialog(Hashtable args) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        runAfterGrantingPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                () -> impl.go("DownloadDialog", args, getContext()),
                impl.getString(MessageID.download_storage_permission_title,getContext()),
                impl.getString(MessageID.download_storage_permission_message,getContext()));
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
        networkManagerBle.removeLocalAvailabilityListener(this);
        super.onDestroy();
    }

    @Override
    public void onLocalAvailabilityChanged(Set<Long> locallyAvailableEntries) {
        entryDetailPresenter.handleLocalAvailabilityStatus(locallyAvailableEntries);
    }
}
