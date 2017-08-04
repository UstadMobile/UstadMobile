package com.ustadmobile.port.android.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogEntryPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.ImageLoader;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class CatalogEntryActivity extends UstadBaseActivity implements CatalogEntryView, View.OnClickListener {

    private CatalogEntryPresenter mPresenter;

    private CollapsingToolbarLayout mCollapsingToolbar;

    private RecyclerView seeAlsoRecyclerView;

    private static Hashtable<Integer, Integer> BUTTON_ID_MAP =new Hashtable<>();

    private Vector<String[]> seeAlsoItems = new Vector<>();

    private SeeAlsoViewAdapter seeAlsoViewAdapter;

    private ImageViewLoadTarget headerLoadTarget;

    private ImageViewLoadTarget iconLoadTarget;


    static {
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_DOWNLOAD, R.id.activity_catalog_entry_download_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_OPEN, R.id.activity_catalog_entry_open_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_MODIFY, R.id.activity_catalog_entry_remove_modify_button);
    }


    private class SeeAlsoViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;

        private TextView titleView;

        public SeeAlsoViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView)itemView.findViewById(R.id.item_catalog_entry_see_also_imageview);
            titleView = (TextView)itemView.findViewById(R.id.item_catalog_entry_see_also_title);
        }
    }

    private class SeeAlsoViewAdapter extends RecyclerView.Adapter<SeeAlsoViewHolder> {

        @Override
        public SeeAlsoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CatalogEntryActivity.this).inflate(
                    R.layout.item_catalog_entry_see_also, parent, false);
            return new SeeAlsoViewHolder(view);
        }


        @Override
        public void onBindViewHolder(SeeAlsoViewHolder holder, int position) {
            holder.titleView.setText(CatalogEntryActivity.this.seeAlsoItems.get(0)
                    [UstadJSOPDSItem.ATTR_TITLE]);
        }

        @Override
        public int getItemCount() {
            return CatalogEntryActivity.this.seeAlsoItems.size();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_catalog_entry);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Enumeration<Integer> buttonIds = BUTTON_ID_MAP.keys();
        while(buttonIds.hasMoreElements()) {
            findViewById(BUTTON_ID_MAP.get(buttonIds.nextElement())).setOnClickListener(this);
        }

        //mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.activity_catalog_entry_collapsing_toolbar);


        seeAlsoRecyclerView = (RecyclerView)findViewById(R.id.activity_catalog_entry_see_also_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        seeAlsoRecyclerView.setLayoutManager(linearLayoutManager);
        seeAlsoViewAdapter = new SeeAlsoViewAdapter();
        seeAlsoRecyclerView.setAdapter(seeAlsoViewAdapter);
        seeAlsoRecyclerView.setNestedScrollingEnabled(false);

        headerLoadTarget = new ImageViewLoadTarget(this,
                (ImageView)findViewById(R.id.activity_catalog_entry_header_img));

        iconLoadTarget = new ImageViewLoadTarget(this,
                (ImageView)findViewById(R.id.activity_catalog_entry_icon_img));

        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        mPresenter = new CatalogEntryPresenter(this, this, args);
        mPresenter.onCreate();

        try{
            UstadJSOPDSFeed feed=new UstadJSOPDSFeed();
            feed.loadFromString(args.get(CatalogEntryPresenter.ARG_ENTRY_OPDS_STR).toString());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    private int getButtonIdFromViewId(int viewId) {
        Enumeration<Integer> enumeration = BUTTON_ID_MAP.keys();
        Integer buttonId;
        while(enumeration.hasMoreElements()) {
            buttonId = enumeration.nextElement();
            if(BUTTON_ID_MAP.get(buttonId).equals(viewId))
                return buttonId;
        }

        return -1;
    }

    @Override
    public void onClick(View v) {
        mPresenter.handleClickButton(getButtonIdFromViewId(v.getId()));
    }

    @Override
    public void setButtonDisplayed(final int buttonId, final boolean display) {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               findViewById(BUTTON_ID_MAP.get(buttonId)).setVisibility(display ? View.VISIBLE : View.GONE);
           }
       });
    }

    @Override
    public void setHeader(String headerUrl) {
        ImageLoader.getInstance().loadImage(headerUrl, headerLoadTarget, mPresenter);
    }

    @Override
    public void setIcon(String iconFileUri) {
        ImageLoader.getInstance().loadImage(iconFileUri, iconLoadTarget, mPresenter);
    }

    @Override
    public void setMode(int mode) {

    }


    @Override
    public void setProgress(float progress) {
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.activity_catalog_entry_progress_bar);
        if(progress == -1) {
            progressBar.setIndeterminate(true);
        }else {
            progressBar.setIndeterminate(false);
            final int progressSize = Math.round(progress * 100);
            progressBar.setProgress(progressSize);
        }

    }

    @Override
    public void setProgressStatusText(final String progressStatusText) {
        final TextView statusView = (TextView)findViewById(R.id.activity_catalog_entry_progress_status_text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText(progressStatusText);
            }
        });
    }

    @Override
    public void setSize(long downloadSize) {

    }

    @Override
    public void setDescription(String description, String contentType) {
        TextView descriptionTextView = ((TextView)findViewById(R.id.activity_catalog_entry_description));
        if(contentType.equals(UstadJSOPDSEntry.CONTENT_TYPE_XHTML)) {
            descriptionTextView.setText(Html.fromHtml(description));
        }else {
            descriptionTextView.setText(description);
        }
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        //mCollapsingToolbar.setTitle(title);
    }

    @Override
    public void setProgressVisible(boolean visible) {
        findViewById(R.id.activity_catalog_entry_download_status_layout).setVisibility(
                visible? View.VISIBLE : View.GONE);
    }

    @Override
    public void setLocallyAvailableStatus(int status) {
        ImageView statusIconView = (ImageView) findViewById(R.id.activity_catalog_entry_local_availability_icon);
        TextView statusTextView = (TextView)findViewById(R.id.activity_catalog_entry_local_availability_text);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(status) {
            case CatalogEntryView.LOCAL_STATUS_IN_PROGRESS:
                statusIconView.setVisibility(View.INVISIBLE);
                break;
            case CatalogEntryView.LOCAL_STATUS_AVAILABLE:
                statusIconView.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_done_black_16dp));
                statusIconView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.file_available_locally);
                break;
            case CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE:
                statusIconView.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_cloud_download_black_24dp));
                statusTextView.setText(R.string.file_unavailable_locally);
                statusIconView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void addSeeAlsoItem(String[] link) {
        seeAlsoItems.add(link);
        seeAlsoViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeSeeAlsoItem(String[] link) {
        seeAlsoItems.remove(link);
        seeAlsoViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void clearSeeAlsoItems() {
        seeAlsoItems.clear();
        seeAlsoViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void setLearnerProgress(CourseProgress progress) {
        ((LearnerProgressView)findViewById(R.id.activity_catalog_entry_learner_progress)).setProgress(
                progress);
    }

    @Override
    public void setLearnerProgressVisible(boolean visible) {
        findViewById(R.id.activity_catalog_entry_learner_progress).setVisibility(visible ?
                View.VISIBLE: View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }


}
