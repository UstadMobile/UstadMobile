package com.ustadmobile.port.android.view;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogEntryPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.ImageLoader;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class CatalogEntryActivity extends UstadBaseActivity implements CatalogEntryView,
        View.OnClickListener, DownloadProgressView.OnStopDownloadListener,
        DialogResultListener {

    private CatalogEntryPresenter mPresenter;

    private CollapsingToolbarLayout mCollapsingToolbar;

    private RecyclerView seeAlsoRecyclerView;

    private static Hashtable<Integer, Integer> BUTTON_ID_MAP =new Hashtable<>();

    private Vector<String[]> seeAlsoItems = new Vector<>();

    private Vector<String> seeAlsoIcons = new Vector<>();

    private SeeAlsoViewAdapter seeAlsoViewAdapter;

    private ImageViewLoadTarget headerLoadTarget;

    private ImageViewLoadTarget iconLoadTarget;

    private HashMap<View, Integer> seeAlsoViewToIndexMap = new HashMap<>();

    private DownloadProgressView mDownloadProgressView;

    private boolean shareButtonVisible = false;

    static {
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_DOWNLOAD, R.id.activity_catalog_entry_download_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_OPEN, R.id.activity_catalog_entry_open_button);
        BUTTON_ID_MAP.put(CatalogEntryView.BUTTON_MODIFY, R.id.activity_catalog_entry_remove_button);
    }


    private static final int ALTERNATIVE_TRANSLATION_BASE_VIEW_ID = 2000;

    private ArrayList<View> alternativeTranlsationViews;

    private class SeeAlsoViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;

        private TextView titleView;

        private ImageViewLoadTarget imageLoadTarget;

        private int currentIndex;

        private View itemView;

        public SeeAlsoViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            iconView = (ImageView)itemView.findViewById(R.id.item_catalog_entry_see_also_imageview);
            titleView = (TextView)itemView.findViewById(R.id.item_catalog_entry_see_also_title);
            imageLoadTarget = new ImageViewLoadTarget(CatalogEntryActivity.this, iconView);
        }
    }

    private class SeeAlsoViewAdapter extends RecyclerView.Adapter<SeeAlsoViewHolder> {

        @Override
        public SeeAlsoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CatalogEntryActivity.this).inflate(
                    R.layout.item_catalog_entry_see_also, parent, false);
            view.setOnClickListener(CatalogEntryActivity.this);
            return new SeeAlsoViewHolder(view);
        }


        @Override
        public void onBindViewHolder(SeeAlsoViewHolder holder, int position) {
            String[] links = CatalogEntryActivity.this.seeAlsoItems.get(position);
            holder.titleView.setText(links[UstadJSOPDSItem.ATTR_TITLE]);
            if(seeAlsoIcons.elementAt(position) != null)
                ImageLoader.getInstance().loadImage(seeAlsoIcons.elementAt(position),
                        holder.imageLoadTarget, mPresenter);
            holder.currentIndex = position;
            seeAlsoViewToIndexMap.put(holder.itemView, position);
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

        mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.activity_catalog_entry_collapsing_toolbar);


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

        mDownloadProgressView = (DownloadProgressView)findViewById(R.id.activity_catalog_entry_download_progress);
        mDownloadProgressView.setOnStopDownloadListener(this);

        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        mPresenter = new CatalogEntryPresenter(this, this, args);
        mPresenter.onCreate();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.catalog_entry_presenter, menu);
        if(!shareButtonVisible) {
            menu.removeItem(R.id.menu_catalog_entry_presenter_share);
        }

        return true;
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
        if(v instanceof Button) {
            mPresenter.handleClickButton(getButtonIdFromViewId(v.getId()));
        }else if(seeAlsoViewToIndexMap.containsKey(v)){
            int seeAlsoPos = seeAlsoViewToIndexMap.get(v);
            mPresenter.handleClickSeeAlsoItem(seeAlsoItems.get(seeAlsoPos));
        }else if(alternativeTranlsationViews.contains(v)) {
            mPresenter.handleClickAlternativeTranslationLink(alternativeTranlsationViews.indexOf(v));
        }
    }

    @Override
    public void onClickStopDownload(DownloadProgressView view) {
        mPresenter.handleClickStopDownload();
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
    public void setShareButtonVisible(boolean shareButtonVisible) {
        this.shareButtonVisible = shareButtonVisible;
        invalidateOptionsMenu();
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
        mDownloadProgressView.setProgress(progress);
    }

    @Override
    public void setProgressStatusText(final String progressStatusText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadProgressView.setStatusText(progressStatusText);
            }
        });
    }

    @Override
    public void setSize(String entrySize) {
        TextView entrySizeTextView = (TextView)findViewById(R.id.activity_catalog_entry_size_text);
        entrySizeTextView.setText(entrySize != null ? entrySize : "");
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
        mCollapsingToolbar.setTitle(title);
    }

    @Override
    public void setProgressVisible(boolean visible) {
        mDownloadProgressView.setVisibility(visible? View.VISIBLE : View.GONE);
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
                        R.drawable.ic_nearby_black_24px));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_catalog_entry_presenter_share) {
            mPresenter.handleClickShare();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addSeeAlsoItem(String[] link, String iconUrl) {
        seeAlsoItems.add(link);
        seeAlsoIcons.add(iconUrl);
        seeAlsoViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeSeeAlsoItem(String[] link) {
        int linkIndex = seeAlsoItems.indexOf(link);
        if(linkIndex != -1) {
            seeAlsoItems.removeElementAt(linkIndex);
            seeAlsoIcons.removeElementAt(linkIndex);
            seeAlsoViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSeeAlsoVisible(boolean visible) {
        findViewById(R.id.activity_catalog_entry_see_also_cardview).setVisibility(visible
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setAlternativeTranslationLinks(String[] languages) {
        ViewGroup flowLayout = (ViewGroup)findViewById(R.id.activity_catalog_entry_also_available_in);
        flowLayout.removeAllViews();

        int paddingTop = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, 6,
                getResources().getDisplayMetrics()));
        int padding2 = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, 2,
                getResources().getDisplayMetrics()));


        TextView languageTv;
        TextView alsoAvailableView = new TextView(this);
        alsoAvailableView.setText(R.string.also_available_in);
        alsoAvailableView.setPadding(0, paddingTop, padding2, padding2);
        flowLayout.addView(alsoAvailableView);

        alternativeTranlsationViews = new ArrayList<>();


        for(int i = 0; i < languages.length; i++) {
            languageTv = new TextView(this);
            languageTv.setText(languages[i]);
            languageTv.setTypeface(null, Typeface.BOLD);
            languageTv.setTextColor(ContextCompat.getColor(this, R.color.primary));
            languageTv.setPadding(padding2, paddingTop, padding2, padding2);

            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
            languageTv.setBackgroundResource(outValue.resourceId);
            languageTv.setClickable(true);
            languageTv.setOnClickListener(this);
            alternativeTranlsationViews.add(languageTv);

            flowLayout.addView(languageTv);
        }
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

    @Override
    public void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args) {
        mPresenter.onDialogResult(commandId, dialog, args);
    }
}
