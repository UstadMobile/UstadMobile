package com.ustadmobile.port.android.view;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.EntryDetailPresenter;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

public class EntryDetailActivity extends UstadBaseActivity implements ContentEntryDetailView {

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


        getUMToolbar().setTitle(contentEntry.getTitle());

        TextView license = findViewById(R.id.entry_detail_license);
        license.setText(contentEntry.getLicenseName());

        TextView author = findViewById(R.id.entry_detail_author);
        author.setText(contentEntry.getAuthor());

    }

    @Override
    public void setFileInfo(List<ContentEntryFile> contentEntryFileList) {

        if(contentEntryFileList.size() == 1){

            ContentEntryFile contentEntryFile = contentEntryFileList.get(0);

            TextView downloadSize = findViewById(R.id.entry_detail_content_size);
            downloadSize.setText(String.valueOf(contentEntryFile.getFileSize()));

        }else{

            ContentEntryFile contentEntryFile = contentEntryFileList.get(0);

            TextView downloadSize = findViewById(R.id.entry_detail_content_size);
            downloadSize.setText(String.valueOf(contentEntryFile.getFileSize()));


        }


    }
}
