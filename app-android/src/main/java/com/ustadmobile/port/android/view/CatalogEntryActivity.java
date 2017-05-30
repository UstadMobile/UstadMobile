package com.ustadmobile.port.android.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class CatalogEntryActivity extends UstadBaseActivity implements CatalogEntryView, View.OnClickListener {

    private CatalogEntryPresenter mPresenter;

    private CollapsingToolbarLayout mCollapsingToolbar;

    private RecyclerView seeAlsoRecyclerView;

    private NetworkManagerAndroid managerAndroid;

    private static Hashtable<Integer, Integer> BUTTON_ID_MAP =new Hashtable<>();


    static {
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_DOWNLOAD, R.id.activity_catalog_entry_download_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_OPEN, R.id.activity_catalog_entry_open_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_REMOVE, R.id.activity_catalog_entry_remove_modify_button);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        managerAndroid= (NetworkManagerAndroid) UstadMobileSystemImpl.getInstance().getNetworkManager();
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

        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        mPresenter = new CatalogEntryPresenter(this, this, args);
        mPresenter.onCreate();

        try{
            UstadJSOPDSFeed feed=new UstadJSOPDSFeed();
            feed.loadFromString(args.get(CatalogEntryPresenter.ARG_ENTRY_OPDS_STR).toString());

            String entryId=feed.entries[0].id;

            List<String> entries=new ArrayList<>();
            entries.add(entryId);

            //managerAndroid.requestFileStatus(entries,this);



        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seeAlsoRecyclerView = (RecyclerView)findViewById(R.id.activity_catalog_entry_see_also_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        seeAlsoRecyclerView.setLayoutManager(linearLayoutManager);
        final Context ctx = this;
        seeAlsoRecyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(ctx).inflate(R.layout.item_catalog_entry_see_also, parent, false);
                return new SeeAlsoViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 5;
            }
        });
        seeAlsoRecyclerView.setNestedScrollingEnabled(false);
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
    public void setHeader(String headerFileUri) {
        ((ImageView)findViewById(R.id.activity_catalog_entry_header_img)).setImageBitmap(
            BitmapFactory.decodeFile(headerFileUri));
    }

    @Override
    public void setIcon(String iconFileUri) {
        ((ImageView)findViewById(R.id.activity_catalog_entry_icon_img)).setImageBitmap(
            BitmapFactory.decodeFile(iconFileUri));
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
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }


}
