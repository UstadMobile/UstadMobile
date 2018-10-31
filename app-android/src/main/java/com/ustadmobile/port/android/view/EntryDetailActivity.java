package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.EntryDetailPresenter;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

public class EntryDetailActivity extends UstadBaseActivity implements ContentEntryDetailView, ContentEntryLanguageAdapter.AdapterViewListener {

    private EntryDetailPresenter entryDetailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);


        setUMToolbar(R.id.entry_detail_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        entryDetailPresenter = new EntryDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        entryDetailPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }

    @Override
    public void setContentInfo(ContentEntry contentEntry) {

        TextView title = findViewById(R.id.entry_detail_title);
        title.setText(contentEntry.getTitle());

        TextView desc = findViewById(R.id.entry_detail_description);
        desc.setText(contentEntry.getDescription());

        new Handler(Looper.getMainLooper()).post(() -> Picasso.with(EntryDetailActivity.this)
                .load(contentEntry.getThumbnailUrl())
                .into((ImageView) findViewById(R.id.entry_detail_thumbnail)));


        getSupportActionBar().setTitle(contentEntry.getTitle());

        TextView license = findViewById(R.id.entry_detail_license);
        license.setText(contentEntry.getLicenseName());

        TextView author = findViewById(R.id.entry_detail_author);
        author.setText(contentEntry.getAuthor());

    }

    @Override
    public void setFileInfo(List<ContentEntryFile> contentEntryFileList) {

        if (contentEntryFileList.size() == 1) {

            ContentEntryFile contentEntryFile = contentEntryFileList.get(0);

            TextView downloadSize = findViewById(R.id.entry_detail_content_size);
            downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

        } else {

            // TODO

            ContentEntryFile contentEntryFile = contentEntryFileList.get(0);

            TextView downloadSize = findViewById(R.id.entry_detail_content_size);
            downloadSize.setText(UMFileUtil.formatFileSize(contentEntryFile.getFileSize()));

        }


    }

    @Override
    public void setLanguageContent(List<ContentEntry> result) {

        RecyclerView flexBox = findViewById(R.id.entry_detail_flex);

        if (result.size() == 0) {
            flexBox.setVisibility(View.GONE);
        } else {
            flexBox.setVisibility(View.VISIBLE);

            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
            flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
            flexBox.setLayoutManager(flexboxLayoutManager);

            ContentEntryLanguageAdapter adapter = new ContentEntryLanguageAdapter(result, this);
            flexBox.setAdapter(adapter);
        }


    }

    @Override
    public void selectLang(ContentEntry contentEntry) {
        entryDetailPresenter.openTranslation(contentEntry);
    }
}
