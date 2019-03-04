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
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

public class ContentEntryDetailActivity extends UstadBaseActivity implements ContentEntryDetailView, ContentEntryDetailLanguageAdapter.AdapterViewListener {

    private ContentEntryDetailPresenter entryDetailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);


        setUMToolbar(R.id.entry_detail_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        entryDetailPresenter = new ContentEntryDetailPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        entryDetailPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
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
    public void setContentInfo(ContentEntry contentEntry, String licenseType) {

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
            license.setText(licenseType);

            TextView author = findViewById(R.id.entry_detail_author);
            author.setText(contentEntry.getAuthor());


        });


    }

    @Override
    public void setFileInfo(List<Container> contentEntryFileList) {
        runOnUiThread(() -> {
            Button button = findViewById(R.id.entry_detail_button);

            if (contentEntryFileList == null || contentEntryFileList.isEmpty()) {
                button.setEnabled(false);
                return;
            }

            button.setEnabled(true);
            if (contentEntryFileList.size() == 1) {

                Container contentEntryFile = contentEntryFileList.get(0);


                TextView downloadSize = findViewById(R.id.entry_detail_content_size);
                downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

            } else {

                // TODO manage multiple files

                Container contentEntryFile = contentEntryFileList.get(0);

                TextView downloadSize = findViewById(R.id.entry_detail_content_size);
                downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

            }
        });
    }

    @Override
    public void setTranslationsAvailable(List<ContentEntryRelatedEntryJoinWithLanguage> result, long entryUuid) {

        runOnUiThread(() -> {
            TextView label = findViewById(R.id.entry_detail_available_label);
            RecyclerView flexBox = findViewById(R.id.entry_detail_flex);

            if (result.size() == 0) {
                flexBox.setVisibility(View.GONE);
                label.setVisibility(View.GONE);
            } else {
                flexBox.setVisibility(View.VISIBLE);
                label.setVisibility(View.VISIBLE);

                FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
                flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
                flexBox.setLayoutManager(flexboxLayoutManager);

                ContentEntryDetailLanguageAdapter adapter = new ContentEntryDetailLanguageAdapter(result, this, entryUuid);
                flexBox.setAdapter(adapter);
            }
        });

    }

    @Override
    public void showProgress(float progressValue) {

        runOnUiThread(() -> {

            Button button = findViewById(R.id.entry_detail_button);
            DownloadProgressView downloadProgressView = findViewById(R.id.entry_detail_progress);
            button.setVisibility(View.GONE);
            downloadProgressView.setVisibility(View.VISIBLE);
            downloadProgressView.setProgress(progressValue);
            downloadProgressView.setStatusText("Downloading");


        });
    }

    @Override
    public void showButton(boolean isDownloaded) {
        runOnUiThread(() -> {

            Button button = findViewById(R.id.entry_detail_button);
            DownloadProgressView downloadProgressView = findViewById(R.id.entry_detail_progress);

            button.setVisibility(View.VISIBLE);
            downloadProgressView.setVisibility(View.GONE);

            button.setText(isDownloaded ? R.string.open : R.string.download);

            button.setOnClickListener(view ->
                    entryDetailPresenter.handleDownloadButtonClick(isDownloaded, entryDetailPresenter.getEntryUuid()));
        });

    }

    @Override
    public void handleFileOpenError() {
        runOnUiThread(() -> Toast.makeText((Context) getContext(), R.string.error, Toast.LENGTH_LONG).show());
    }

    @Override
    public void selectContentEntryOfLanguage(long uid) {
        entryDetailPresenter.handleClickTranslatedEntry(uid);
    }
}
